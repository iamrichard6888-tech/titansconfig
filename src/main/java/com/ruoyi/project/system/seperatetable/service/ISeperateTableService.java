package com.ruoyi.project.system.seperatetable.service;

import java.util.List;
import java.util.Map;

public interface ISeperateTableService {
    void startDevideTables();
    void stopDevideTables();
    Map<String, Object> getProgressStatus();
    // 新增查询日志的方法
    List<Map<String, Object>> getLogList();
}