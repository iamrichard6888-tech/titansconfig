package com.ruoyi.project.system.seperatetable.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SeperateTableMapper {
    // 获取所有需要分表的全宗和单位ID
    List<Map<String, String>> selectAllUnits();

    // 检查表是否存在
    int checkTableExists(@Param("tableName") String tableName,@Param("currentSchema") String currentSchema);

    // 优化：使用 JOIN 创建 SRCLIST 分表
    void createSrcListTable(@Param("tableName") String tableName, @Param("unitid") String unitid);

    // 优化：使用 JOIN 创建 STOREITEM 分表
    void createStoreItemTable(@Param("tableName") String tableName, @Param("srcTableName") String srcTableName);

    // 添加 COPYURL 字段
    void addCopyUrlColumn(@Param("tableName") String tableName);

    // 创建索引
    void createIndex(@Param("indexName") String indexName, @Param("tableName") String tableName, @Param("columns") String columns);

    /**
     * 获取指定表的数据量 (VOLUME)
     */
    int getTableVolume(@Param("tableName") String tableName);


    void dropTable(@Param("tableName")String tableName);

    /**
     * 删除指定全宗的日志 (用于任务中断或失败时的回滚)
     * @param qzh 全宗号
     */
    void deleteLog(@Param("qzh") String qzh);

    /**
     * 查询分表日志列表 (供前端表格展示)
     */
    List<Map<String, Object>> selectLogList();

    /**
     * 写入全宗分表日志到 aaa 表 (任务开始时调用)
     */
    void insertLog(@Param("qzh") String qzh,
                   @Param("tableName") String tableName,
                   @Param("volume") Integer volume,
                   @Param("status") String status,
                   @Param("beginTime") String beginTime,
                   @Param("endTime") String endTime,
                   @Param("pid") String pid); // 新增线程标识

    /**
     * 更新分表日志状态 (任务完成或失败时调用)
     */
    void updateLog(@Param("qzh") String qzh,
                   @Param("tableName") String tableName,
                   @Param("volume") Integer volume,
                   @Param("status") String status,
                   @Param("endTime") String endTime);

    /**
     * 根据线程绑定的唯一批次号精准删除脏日志
     */
    void deleteLogByPid(@Param("pid") String pid);

    String getCurrentSchema();

    @Update("update ${storeitemTableName} set storeid = '${storeid}'")
    void updateStoreid(@Param("storeitemTableName") String storeitemTableName,@Param("storeid") String storeid);
}