package com.ruoyi.project.system.rule.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.text.Convert; // ✅ 修复了报错的导包
import com.ruoyi.project.system.rule.domain.ArchiveAppraisalRule;
import com.ruoyi.project.system.rule.mapper.ArchiveAppraisalRuleMapper;
import com.ruoyi.project.system.rule.service.IArchiveAppraisalRuleService;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ruoyi.project.system.rule.service.MilvusRuleService;
import com.ruoyi.project.system.rule.utils.BgeEmbeddingClient;
import com.ruoyi.project.system.titansort.domain.ArchiveCategory;
import com.ruoyi.project.system.titansort.mapper.ArchiveCategoryMapper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.project.system.rule.utils.MindIEClient;

@Service
public class ArchiveAppraisalRuleServiceImpl implements IArchiveAppraisalRuleService {

    @Autowired
    private ArchiveAppraisalRuleMapper ruleMapper;
    @Autowired
    private ArchiveCategoryMapper archiveCategoryMapper;

    @Autowired
    private MindIEClient mindIEClient;

    @Autowired
    private BgeEmbeddingClient bgeClient;

    @Autowired
    private MilvusRuleService milvusRuleService;

    // 识别末尾各种括号内的法定保管期限：如 (永久), （30年）, 永久, 10年 等
    private static final Pattern RETENTION_PATTERN = Pattern.compile("[(（]?\\s*(永久|\\d+年)\\s*[)）]?$");

    // ==================== 严格映射用户设计的文书档案 6 级正则表达式 ====================
    // L3: 中文大写括号标号，如 （一）会议文件
    private static final Pattern L3_PATTERN = Pattern.compile("^[(（][一二三四五六七八九十]+[)）]");
    // L4: 单数字标号，如 1 本馆内部会议
    private static final Pattern L4_PATTERN = Pattern.compile("^\\d+([、.\\s]|$)");
    // L5: 双层数字标号，如 1.1 党支部委员会会议记录
    private static final Pattern L5_PATTERN = Pattern.compile("^\\d+\\.\\d+([\\s]|$)");
    // L6: 三层数字标号，如 4.1.1 请示、批复、通知
    private static final Pattern L6_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+([\\s]|$)");
    // 识别末尾小括号内的法定保管期限：如 (永久), （30年）, (10年)
    // 🚀 新增：噪声黑名单过滤正则（严格屏蔽表头、大纲标题、落款等无编号系统行）

    private static final Pattern NOISE_BLACKLIST = Pattern.compile(
            "^(序号|归档范围|保管期限|备注|各门类文件材料|机关档案分类方案|.*档案馆办公室.*|.*印发)$"
    );
    @Override
    public List<ArchiveAppraisalRule> selectRuleList(ArchiveAppraisalRule rule) {
        return ruleMapper.selectRuleList(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importRuleDocument(String qzh, MultipartFile file, String createBy) throws Exception {
        String fileName = file.getOriginalFilename();
        if (StringUtils.isEmpty(fileName)) {
            throw new RuntimeException("上传文件名称为空");
        }

        // 1. 获取单位预置的一级分类底图列表
        ArchiveCategory queryParam = new ArchiveCategory();
        queryParam.setUnitId(qzh);
        queryParam.setCategoryLevel(1);
        List<ArchiveCategory> categories = archiveCategoryMapper.selectArchiveCategoryTreeList(queryParam);

        if (categories == null || categories.isEmpty()) {
            throw new RuntimeException("该单位尚未在系统中初始化任何一级分类底图，无法自动映射归属门类");
        }

        // 2. 抽取底层文档内容流
        List<String> rawLines = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            String lowerName = fileName.toLowerCase();
            if (lowerName.endsWith(".doc")) {
                rawLines = extractDocText(is);
            } else if (lowerName.endsWith(".docx")) {
                rawLines = extractDocxText(is);
            } else if (lowerName.endsWith(".xls") || lowerName.endsWith(".xlsx")) {
                rawLines = extractExcelText(is, lowerName.endsWith(".xlsx"));
            } else {
                throw new RuntimeException("不支持的文件格式，仅允许传入 Word 或 Excel 文件");
            }
        }

        if (rawLines.isEmpty()) {
            throw new RuntimeException("未能从文件中解析出有效规则内容，请核对原件排版规律");
        }

        // 3. 执行强覆盖前置防御：清除目标全宗下所有旧的混合数据
        ruleMapper.deleteRulesByQzh(qzh);

        // 4. 🧠 严格匹配 6 级体系架构的状态机处理核心
        List<ArchiveAppraisalRule> readyToInsertList = new ArrayList<>();
        // Key为节点深度(2-6)，Value为对应深度的最新规则对象快照
        Map<Integer, ArchiveAppraisalRule> contextStack = new HashMap<>();

        // 核心标记位：是否已越过前置非业务内容区
        boolean startParsing = false;
        String currentCategoryCode = "";
        int currentSort = 1;

        ArchiveAppraisalRule pendingStitchRule = null;

        for (String line : rawLines) {
            if (StringUtils.isBlank(line)) { continue; }
            String cleanLine = line.trim();

            // -----------------------------------------------------------------
            // 闸门探测：寻找开始解析的绝对标志
            // -----------------------------------------------------------------
            if (!startParsing) {
                // 命中图示要求的开始标志
                if (cleanLine.contains("各门类文件材料归档范围和档案保管期限")) {
                    startParsing = true;
                }
                continue; // 标志行自身及之前的内容直接过滤跳过
            }

            // -----------------------------------------------------------------
            // 强力去噪过滤层
            // -----------------------------------------------------------------
            String compactLine = cleanLine.replaceAll("\\s+", "");
            if (NOISE_BLACKLIST.matcher(compactLine).find() || compactLine.startsWith("序号归档范围")) {
                continue;
            }

            // -----------------------------------------------------------------
            // 探针一：一级分类识别感知 (L1 顶级父节点)
            // -----------------------------------------------------------------
            boolean isL1CategoryHeader = false;
            for (ArchiveCategory cat : categories) {
                // 比对类似 "（一）文书档案" 文本
                if (cleanLine.contains(cat.getCategoryName()) || cleanLine.contains(cat.getCategoryCode())) {
                    currentCategoryCode = cat.getCategoryCode();
                    isL1CategoryHeader = true;
                    contextStack.clear(); // 切换大类，清空栈上下文
                    pendingStitchRule = null;
                    break;
                }
            }
            // 顶级分类标题行不作为独立规则实体落盘
            if (isL1CategoryHeader) { continue; }

            // 防御拦截：如果尚无法推算当前所属大类，暂时略过不规范行
            if (StringUtils.isEmpty(currentCategoryCode)) { continue; }

            // -----------------------------------------------------------------
            // 探针二：判定当前行在定制架构图中的绝对层级深度 (Level 2 ~ 6)
            // -----------------------------------------------------------------
            int currentLevel = resolveWSLevel(cleanLine);
            String clauseNo = extractWSClauseNo(cleanLine, currentLevel);

            // -----------------------------------------------------------------
            // 探针三：跨页/断行向前自动缝合修复 (Forward Stitching)
            // -----------------------------------------------------------------
            // 如果被归入默认的 L2 适用部门层，但实际上它没有大类通用特征，且前序存在待补全叶子节点
            if (currentLevel == 2 && pendingStitchRule != null) {
                Matcher retMatcher = RETENTION_PATTERN.matcher(cleanLine);
                if (retMatcher.find()) {
                    pendingStitchRule.setRetentionPeriod(retMatcher.group(1));
                    cleanLine = cleanLine.substring(0, retMatcher.start()).trim();
                }

                String updatedClause = pendingStitchRule.getClauseText() + " " + cleanLine;
                pendingStitchRule.setClauseText(updatedClause);

                String pPath = pendingStitchRule.getParentPathText();
                if (pPath != null && pPath.contains(" / ")) {
                    String baseParentPath = pPath.substring(0, pPath.lastIndexOf(" / "));
                    pendingStitchRule.setParentPathText(baseParentPath + " / " + updatedClause);
                    pendingStitchRule.setFullMergedText(baseParentPath + " / " + updatedClause);
                } else {
                    pendingStitchRule.setFullMergedText(updatedClause);
                }
                continue;
            }

            // 常规期限与正文抽取
            String retentionPeriod = "";
            Matcher retMatcher = RETENTION_PATTERN.matcher(cleanLine);
            if (retMatcher.find()) {
                retentionPeriod = retMatcher.group(1);
                cleanLine = cleanLine.substring(0, retMatcher.start()).trim();
            }

            String clauseText = cleanLine;
            if (StringUtils.isNotEmpty(clauseNo) && cleanLine.startsWith(clauseNo)) {
                clauseText = cleanLine.substring(clauseNo.length()).trim();
                clauseText = clauseText.replaceAll("^[、.\\s]+", "");
            }

            // 构建新规则实体
            ArchiveAppraisalRule rule = new ArchiveAppraisalRule();
            rule.setRuleId(UUID.randomUUID().toString());
            rule.setQzh(qzh);
            rule.setCategoryCode(currentCategoryCode);
            rule.setClauseNo(clauseNo);
            rule.setClauseText(clauseText);
            rule.setRetentionPeriod(retentionPeriod);
            rule.setSortOrder(currentSort++);
            rule.setProcessStatus(0);
            rule.setCreateBy(createBy);
            rule.setUpdateBy(createBy);

            // -----------------------------------------------------------------
            // 🧠 核心祖先关联树绑定逻辑：严格沿 L6 -> L2 链路向上寻找直系血缘
            // -----------------------------------------------------------------
            ArchiveAppraisalRule parentRule = null;
            for (int pLevel = currentLevel - 1; pLevel >= 2; pLevel--) {
                if (contextStack.containsKey(pLevel)) {
                    parentRule = contextStack.get(pLevel);
                    break;
                }
            }

            if (parentRule != null) {
                rule.setParentId(parentRule.getRuleId());
                String pPath = parentRule.getParentPathText();
                String inheritedPath = StringUtils.isEmpty(pPath) ? parentRule.getClauseText()
                        : pPath + " / " + parentRule.getClauseText();
                rule.setParentPathText(inheritedPath);
            } else {
                rule.setParentId("0"); // 无前置匹配则自动挂载为顶层 L2 适用部门根级
                rule.setParentPathText("");
            }

            String fullMerged = StringUtils.isEmpty(rule.getParentPathText()) ? rule.getClauseText()
                    : rule.getParentPathText() + " / " + rule.getClauseText();
            rule.setFullMergedText(fullMerged);

            readyToInsertList.add(rule);

            // 刷新活动上下文层级栈
            contextStack.put(currentLevel, rule);
            for (int clearLevel = currentLevel + 1; clearLevel <= 6; clearLevel++) {
                contextStack.remove(clearLevel);
            }

            // 叶子缝合标记追踪
            if (StringUtils.isEmpty(retentionPeriod) && currentLevel >= 4) {
                pendingStitchRule = rule;
            } else {
                pendingStitchRule = null;
            }
        }

        if (!readyToInsertList.isEmpty()) {
            return ruleMapper.batchInsertRules(readyToInsertList);
        }
        return 0;
    }

    @Override
    public int deleteRuleByIds(String ids) {
        String[] ruleIds = Convert.toStrArray(ids);
        int count = 0;
        for (String id : ruleIds) {
            count += ruleMapper.deleteRuleById(id);
        }
        return count;
    }

    /**
     * 精准判定当前行符合定制架构的哪一层级 (Level 2 ~ 6)
     */
    private int resolveWSLevel(String text) {
        // L6: 4.1.1 结构
        if (L6_PATTERN.matcher(text).find()) return 6;
        // L5: 1.1 结构
        if (L5_PATTERN.matcher(text).find()) return 5;
        // L4: 1 结构
        if (L4_PATTERN.matcher(text).find()) return 4;
        // L3: （一） 结构
        if (L3_PATTERN.matcher(text).find()) return 3;

        // 没有任何上述明显的标号特征，判定为 L2 适用部门分类节点（如“各部门共有部分”）
        return 2;
    }

    /**
     * 提取匹配层级的前缀标号
     */
    private String extractWSClauseNo(String text, int level) {
        Matcher m = null;
        switch (level) {
            case 6: m = L6_PATTERN.matcher(text); break;
            case 5: m = L5_PATTERN.matcher(text); break;
            case 4: m = L4_PATTERN.matcher(text); break;
            case 3: m = L3_PATTERN.matcher(text); break;
            case 2: return ""; // L2 层无特定数字标号前缀
        }
        if (m != null && m.find()) {
            return m.group(0).trim();
        }
        return "";
    }

    // =========================================================================
    // 🗂️ Word/Excel 抽取器适配实现 (保持平铺智能表格遍历)
    // =========================================================================

    private List<String> extractDocxText(InputStream is) throws Exception {
        List<String> lines = new ArrayList<>();
        XWPFDocument doc = new XWPFDocument(is);

        // 先读取普通段落文本
        for (XWPFParagraph p : doc.getParagraphs()) {
            String text = p.getText().trim();
            if (StringUtils.isNotBlank(text)) {
                lines.add(text);
            }
        }

        // 读取文档内嵌的表格矩阵
        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                StringBuilder rowStr = new StringBuilder();
                boolean isHeaderRow = false;

                for (XWPFTableCell cell : row.getTableCells()) {
                    String cellText = cell.getText().trim();
                    cellText = cellText.replaceAll("[\\r\\n]+", " ");
                    rowStr.append(cellText).append(" ");
                }

                String finalRowStr = rowStr.toString().trim();
                String compact = finalRowStr.replaceAll("\\s+", "");
                if (compact.startsWith("序号归档范围") || compact.equals("序号归档范围保管期限备注")) {
                    isHeaderRow = true;
                }

                if (!isHeaderRow && StringUtils.isNotBlank(finalRowStr)) {
                    lines.add(finalRowStr);
                }
            }
        }
        return lines;
    }

    private List<String> extractDocText(InputStream is) throws Exception {
        List<String> lines = new ArrayList<>();
        HWPFDocument doc = new HWPFDocument(is);
        Range range = doc.getRange();
        for (int i = 0; i < range.numParagraphs(); i++) {
            String text = range.getParagraph(i).text();
            if (StringUtils.isNotBlank(text)) {
                lines.add(text.replaceAll("[\\r\\n\\u0007]", "").trim());
            }
        }
        return lines;
    }

    private List<String> extractExcelText(InputStream is, boolean isXlsx) throws Exception {
        List<String> lines = new ArrayList<>();
        Workbook wb = isXlsx ? new XSSFWorkbook(is) : new HSSFWorkbook(is);
        Sheet sheet = wb.getSheetAt(0);

        for (Row row : sheet) {
            StringBuilder rowStr = new StringBuilder();
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellType(CellType.STRING);
                String val = cell.getStringCellValue().trim();
                if (StringUtils.isNotBlank(val)) {
                    rowStr.append(val).append(" ");
                }
            }
            if (StringUtils.isNotBlank(rowStr.toString())) {
                lines.add(rowStr.toString().trim());
            }
        }
        wb.close();
        return lines;
    }

    /**
     * 查询规则单条实体
     */
    @Override
    public ArchiveAppraisalRule selectArchiveAppraisalRuleById(String ruleId) {
        // 调度底层 Mapper 的精准主键查询
        return ruleMapper.selectArchiveAppraisalRuleById(ruleId);
    }

    /**
     * 新增保存单条规则
     */
    @Override
    public int insertArchiveAppraisalRule(ArchiveAppraisalRule rule) {
        // 若依标准单表插入透传
        return ruleMapper.insertArchiveAppraisalRule(rule);
    }

    /**
     * 更新覆盖单条规则
     */
    @Override
    public int updateArchiveAppraisalRule(ArchiveAppraisalRule rule) {
        // 若依标准单表动态更新透传 (仅更新非空字段)
        return ruleMapper.updateArchiveAppraisalRule(rule);
    }

    /**
     * 🚀 阶段二：开启全异步消费通道，静默浓缩提取语义特征
     */
    @Async // 挂载进后台批处理线程池，释放前台调用链
    @Override
    public void startAsyncBatchLlmEnhancement(String qzh, String categoryCode) {
        // 抓取该门类下所有处于代码原生拆解态 (process_status = 0) 的条款
        ArchiveAppraisalRule param = new ArchiveAppraisalRule();
        param.setQzh(qzh);
        param.setCategoryCode(categoryCode);
        param.setProcessStatus(0);
        List<ArchiveAppraisalRule> pendingRules = ruleMapper.selectRuleList(param);
        System.out.println("本次提交给ai的条目："+pendingRules.size());
        for (ArchiveAppraisalRule r : pendingRules) {
            // 如果是纯结构目录，直接标记为人工核验态跳过大模型消耗
            if (StringUtils.isEmpty(r.getRetentionPeriod())) {
                r.setProcessStatus(2);
                ruleMapper.updateArchiveAppraisalRule(r);
                continue;
            }

            long start = System.currentTimeMillis();
            boolean success = false;
            String errorMsg = "";

            // 建立防线：允许短时重试 1 次补偿网络抖动
            for (int retry = 0; retry < 2; retry++) {
                try {
                    // 🚀 终极对齐：将层级血脉与正文双轨同时灌入引擎
                    JSONObject aiMeta = mindIEClient.enhanceRuleMetadata(r.getParentPathText(), r.getClauseText());

                    if (aiMeta != null) {
                        String docType = aiMeta.getString("文种");
                        String eventKey = aiMeta.getString("事由");

                        r.setDocumentTypes(StringUtils.isNotEmpty(docType) ? docType : "综合材料");
                        r.setEventKeywords(StringUtils.isNotEmpty(eventKey) ? eventKey : r.getClauseText());
                        r.setProcessStatus(1); // 稳健标记为 AI 增强完毕态
                        r.setAiCostTimeMs(System.currentTimeMillis() - start);
                        r.setAiErrorLog("");

                        // 确保同步维持大盘全景联合载荷更新
                        String fullMerged = StringUtils.isEmpty(r.getParentPathText()) ? r.getClauseText()
                                : r.getParentPathText() + " / " + r.getClauseText();
                        r.setFullMergedText(fullMerged);

                        ruleMapper.updateArchiveAppraisalRule(r);
                        // ==========================================
                        // 🚀 阶段三新增魔法：实时向量化并沉淀至 Milvus 库！
                        // 提取的优质 eventKey 组合上下文生成最高密度的特征表示
                        // ==========================================
                        String embedText = "文种：" + docType + "，事由：" + eventKey + "，业务归属：" + r.getParentPathText();
                        List<Float> vector = bgeClient.getEmbedding(embedText);
                        milvusRuleService.upsertRuleVector(r.getRuleId(), r.getCategoryCode(), vector);
                        success = true;
                        break;
                    }
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {} // 短暂缓冲避让
                }
            }

            // 记录特例异常，绝不卡死下游消费
            if (!success) {
                r.setAiErrorLog("AI解析异常: " + errorMsg);
                ruleMapper.updateArchiveAppraisalRule(r);
            }
        }
    }

    /**
     * 📊 为前端 image_bd4c5a.png 提供实时处理进度大盘数据
     */
    @Override
    public Map<String, Object> getBatchEnhancementProgress(String qzh, String categoryCode) {
        Map<String, Object> progress = new HashMap<>();
        ArchiveAppraisalRule param = new ArchiveAppraisalRule();
        param.setQzh(qzh);
        param.setCategoryCode(categoryCode);

        List<ArchiveAppraisalRule> all = ruleMapper.selectRuleList(param);
        long total = all.size();
        long completed = 0;
        long failed = 0;

        for (ArchiveAppraisalRule r : all) {
            // processStatus 大于 0 代表已度过原始代码态
            if (r.getProcessStatus() != null && r.getProcessStatus() > 0) completed++;
            if (StringUtils.isNotEmpty(r.getAiErrorLog())) failed++;
        }

        progress.put("total", total);
        progress.put("completed", completed);
        progress.put("failed", failed);
        progress.put("percent", total > 0 ? (int) ((completed * 100) / total) : 100);
        return progress;
    }
}