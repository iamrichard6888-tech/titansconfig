package com.ruoyi.project.system.seperatetable.service;

import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.project.system.seperatetable.mapper.SeperateTableMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SeperateTableServiceImpl implements ISeperateTableService {

    private static final Logger log = LoggerFactory.getLogger(SeperateTableServiceImpl.class);

    @Autowired
    private SeperateTableMapper seperateMapper;

    // 全局状态控制
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isInterrupted = new AtomicBoolean(false);

    // 进度统计
    private AtomicInteger totalUnits = new AtomicInteger(0);
    private AtomicInteger finishedUnits = new AtomicInteger(0);
    private long taskStartTime = 0;

    // 【核心新增】线程注册表：用于绑定 PID 和真实线程，支持精准追踪和停止
    private ConcurrentHashMap<String, Thread> runningTasksRegistry = new ConcurrentHashMap<>();

    private String currentSchema;

    @Override
    public void startDevideTables() {
        currentSchema = seperateMapper.getCurrentSchema();
        if (isRunning.get()) return;

        List<Map<String, String>> units = seperateMapper.selectAllUnits();

        isRunning.set(true);
        isInterrupted.set(false);
        totalUnits.set(units.size());
        finishedUnits.set(0);
        taskStartTime = System.currentTimeMillis();
        runningTasksRegistry.clear(); // 清空历史注册表

        log.info("开始分表任务，共需要处理 {} 个全宗", totalUnits.get());

        new Thread(() -> {
            ExecutorService executor = Executors.newFixedThreadPool(8);

            for (Map<String, String> unit : units) {
                executor.submit(() -> {
                    // 如果全局收到停止指令，后续队列里的任务直接放弃
                    if (isInterrupted.get()) return;

                    String qzh = unit.get("QZH");
                    String unitid = unit.get("UNITID");

                    if (qzh != null) {
                        // 【核心新增】生成当前任务的唯一流水号，并与线程名绑定
                        String batchId = "BATCH-" + IdUtils.simpleUUID().substring(0, 8).toUpperCase();
                        String pid = Thread.currentThread().getName() + "_" + batchId;

                        // 【核心新增】将当前线程注册到管理表中，表示“该线程正在独占处理这个 PID 的任务”
                        runningTasksRegistry.put(pid, Thread.currentThread());

                        try {
                            // 将 PID 传递下去，贯穿整个任务生命周期
                            processSingleUnit(qzh.toUpperCase(), unitid, pid);
                            finishedUnits.incrementAndGet();
                        } finally {
                            // 【核心新增】不管成功、失败还是回滚，上一个任务处理彻底完成了，才从注册表中移除，释放线程去接下一个任务
                            runningTasksRegistry.remove(pid);
                        }
                    }
                });
            }

            executor.shutdown();
            try {
                while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    if (isInterrupted.get()) {
                        log.warn("全局停止指令生效，正在中断所有进行中的子线程...");
                        executor.shutdownNow();
                        break;
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }

            isRunning.set(false);
            log.info("分表任务执行周期结束！");
        }).start();
    }

    /**
     * 全局停止方法
     */
    @Override
    public void stopDevideTables() {
        if (isRunning.get()) {
            isInterrupted.set(true);
        }
    }

    /**
     * 【新增功能】如果你想停止指定的某个任务/线程，可调用此方法
     */
    public void stopSpecificTask(String pid) {
        Thread targetThread = runningTasksRegistry.get(pid);
        if (targetThread != null && targetThread.isAlive()) {
            log.warn("正在定向中断并停止任务 PID: {}", pid);
            targetThread.interrupt(); // 向该线程发送中断信号，触发底层 SQL 异常并进入 rollback
        } else {
            log.info("任务 PID: {} 已完成或不存在，无需停止", pid);
        }
    }

    @Override
    public Map<String, Object> getProgressStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isRunning", isRunning.get());
        status.put("total", totalUnits.get());
        status.put("finished", finishedUnits.get());

        // 计算预计剩余时间 (ETA)
        String etaStr = "--:--:--";
        if (isRunning.get() && finishedUnits.get() > 0) {
            long currentTime = System.currentTimeMillis();
            long timeSpent = currentTime - taskStartTime;
            long estimatedTotalTime = (timeSpent / finishedUnits.get()) * totalUnits.get();
            long remainingTimeMillis = estimatedTotalTime - timeSpent;

            long hours = (remainingTimeMillis / (1000 * 60 * 60)) % 24;
            long minutes = (remainingTimeMillis / (1000 * 60)) % 60;
            long seconds = (remainingTimeMillis / 1000) % 60;
            etaStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else if (!isRunning.get() && finishedUnits.get() == totalUnits.get() && totalUnits.get() > 0) {
            etaStr = "已完成";
        }

        status.put("eta", etaStr);
        return status;
    }

    /**
     * 【改造】接收 PID，全程使用该 PID 记录日志和执行操作
     */
    private void processSingleUnit(String qzh, String unitid, String pid) {
        String srcTableName = "T_ER_SRCLIST_FOR_" + qzh;
        String storeTableName = "T_ER_STOREITEM_FOR_" + qzh;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentOperatingTable = srcTableName;

        try {
            // ==========================================
            // 1. 处理 SRCLIST
            // ==========================================
            String srcBeginTime = sdf.format(new Date());
            // 写入【创建中】，强制绑定当前生成的唯一 PID
            seperateMapper.insertLog(qzh, srcTableName, 0, "创建中", srcBeginTime, null, pid);

            if (seperateMapper.checkTableExists(srcTableName,currentSchema) == 0) {
                seperateMapper.createSrcListTable(srcTableName, unitid);
                seperateMapper.createIndex("IDX_SRC_ERID_" + qzh, srcTableName, "ERID");
                seperateMapper.createIndex("IDX_SRC_STOREITEMID_" + qzh, srcTableName, "STOREITEMID");
                seperateMapper.createIndex("IDX_SRC_ERID_STOREITEMID_" + qzh, srcTableName, "ERID, STOREITEMID");
                int volume = seperateMapper.getTableVolume(srcTableName);
                seperateMapper.updateLog(qzh, srcTableName, volume, "创建成功", sdf.format(new Date()));
            } else {
                int volume = seperateMapper.getTableVolume(srcTableName);
                seperateMapper.updateLog(qzh, srcTableName, volume, "表已存在", sdf.format(new Date()));
            }

            if (isInterrupted.get() || Thread.currentThread().isInterrupted()) {
                rollback(qzh, srcTableName, storeTableName, pid);
                return;
            }

            // ==========================================
            // 2. 处理 STOREITEM
            // ==========================================
            currentOperatingTable = storeTableName;
            String storeBeginTime = sdf.format(new Date());
            // 写入【创建中】，强制绑定当前生成的唯一 PID
            seperateMapper.insertLog(qzh, storeTableName, 0, "创建中", storeBeginTime, null, pid);

            if (seperateMapper.checkTableExists(storeTableName,currentSchema) == 0) {
                seperateMapper.createStoreItemTable(storeTableName, srcTableName);
                seperateMapper.addCopyUrlColumn(storeTableName);
                seperateMapper.updateStoreid(storeTableName, "691DF241214C2CE3785CBBB4D47CA768");
                seperateMapper.createIndex("IDX_STO_ID_" + qzh, storeTableName, "ID");
                seperateMapper.createIndex("IDX_STO_STOREID_" + qzh, storeTableName, "STOREID");
                seperateMapper.createIndex("IDX_STO_ID_STOREID_" + qzh, storeTableName, "ID, STOREID");
                int volume = seperateMapper.getTableVolume(storeTableName);
                seperateMapper.updateLog(qzh, storeTableName, volume, "创建成功", sdf.format(new Date()));
            } else {
                int volume = seperateMapper.getTableVolume(storeTableName);
                seperateMapper.updateLog(qzh, storeTableName, volume, "表已存在", sdf.format(new Date()));
            }

        } catch (Exception e) {
            // 【核心修复】：清除线程的中断标志！
            // 否则 Druid 连接池会因为线程带有 interrupt 标记，而拒绝为后续的回滚操作分配数据库连接
            Thread.interrupted();

            log.error("创建全宗 {} 失败，准备使用 PID: {} 进行精准回滚: ", qzh, pid, e.getMessage()); // e.getMessage() 让控制台清爽一点，不打几百行栈

            String errorMsg = "失败: " + e.getMessage();
            String finalErrorMsg = errorMsg.length() > 200 ? errorMsg.substring(0, 200) : errorMsg;

            // 记录失败状态
            seperateMapper.updateLog(qzh, currentOperatingTable, 0, finalErrorMsg, sdf.format(new Date()));

            // 执行精准回滚 (删表、删日志)
            rollback(qzh, srcTableName, storeTableName, pid);
        }
    }

    /**
     * 【改造】带着 PID 去执行精准回滚，绝不误伤历史成功数据
     */
    private void rollback(String qzh, String srcTableName, String storeTableName, String pid) {
        log.warn("任务 PID: {} 被中断或异常，开始物理表和日志回滚...", pid);
        try {
            seperateMapper.dropTable(srcTableName);
            seperateMapper.dropTable(storeTableName);

            // 【核心变化】只删除当前 PID 产生的脏日志，哪怕这个全宗昨天成功过一次，也不会被误删
            seperateMapper.deleteLogByPid(pid);

            log.info("任务 PID: {} (全宗 {}) 精准回滚清理完成。", pid, qzh);
        } catch (Exception e) {
            log.error("任务 PID: {} 回滚失败: {}", pid, e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getLogList() {
        return seperateMapper.selectLogList();
    }
}