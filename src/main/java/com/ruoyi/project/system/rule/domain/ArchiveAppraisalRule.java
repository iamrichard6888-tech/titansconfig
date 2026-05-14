package com.ruoyi.project.system.rule.domain;

import com.ruoyi.framework.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 档案保管期限鉴定规则对象 archive_appraisal_rule
 * * @author ruoyi
 * @date 2026-05-12
 */
public class ArchiveAppraisalRule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 规则唯一主键UUID */
    private String ruleId;

    /** 全宗号 */
    private String qzh;

    /** 档案门类代码 */
    private String categoryCode;

    /** 父节点ID (支撑左侧Ztree无缝挂载) */
    private String parentId;

    /** 条款最终编号 */
    private String clauseNo;

    /** 完整祖先血缘路径 */
    private String parentPathText;

    /** 本级核心条款原文/目录名称 */
    private String clauseText;

    /** 智能引擎专供全景融合文本 */
    private String fullMergedText;

    /** 保管期限 (永久/30年/10年，非叶子节点严格为空字符串) */
    private String retentionPeriod;

    /** 适用文种数组 */
    private String documentTypes;

    /** 核心事由关键词 */
    private String eventKeywords;

    /** 处理状态: 0-代码原生拆解态, 1-AI深度增强完毕, 2-人工终审交割态 */
    private Integer processStatus;

    /** 物理顺位号 */
    private Integer sortOrder;
    /** 单次调用 AI 消耗的底层响应时间 (毫秒)，用于核算 ROI 资产 */
    private Long aiCostTimeMs;

    /** 记录大模型解析异常原因，供监控大盘亮红灯追溯 */
    private String aiErrorLog;

    public Long getAiCostTimeMs() {
        return aiCostTimeMs;
    }

    public void setAiCostTimeMs(Long aiCostTimeMs) {
        this.aiCostTimeMs = aiCostTimeMs;
    }

    public String getAiErrorLog() {
        return aiErrorLog;
    }

    public void setAiErrorLog(String aiErrorLog) {
        this.aiErrorLog = aiErrorLog;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getQzh() {
        return qzh;
    }

    public void setQzh(String qzh) {
        this.qzh = qzh;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getClauseNo() {
        return clauseNo;
    }

    public void setClauseNo(String clauseNo) {
        this.clauseNo = clauseNo;
    }

    public String getParentPathText() {
        return parentPathText;
    }

    public void setParentPathText(String parentPathText) {
        this.parentPathText = parentPathText;
    }

    public String getClauseText() {
        return clauseText;
    }

    public void setClauseText(String clauseText) {
        this.clauseText = clauseText;
    }

    public String getFullMergedText() {
        return fullMergedText;
    }

    public void setFullMergedText(String fullMergedText) {
        this.fullMergedText = fullMergedText;
    }

    public String getRetentionPeriod() {
        return retentionPeriod;
    }

    public void setRetentionPeriod(String retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
    }

    public String getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(String documentTypes) {
        this.documentTypes = documentTypes;
    }

    public String getEventKeywords() {
        return eventKeywords;
    }

    public void setEventKeywords(String eventKeywords) {
        this.eventKeywords = eventKeywords;
    }

    public Integer getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(Integer processStatus) {
        this.processStatus = processStatus;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String toString() {
        return "ArchiveAppraisalRule{" +
                "ruleId='" + ruleId + '\'' +
                ", qzh='" + qzh + '\'' +
                ", categoryCode='" + categoryCode + '\'' +
                ", parentId='" + parentId + '\'' +
                ", clauseNo='" + clauseNo + '\'' +
                ", parentPathText='" + parentPathText + '\'' +
                ", clauseText='" + clauseText + '\'' +
                ", fullMergedText='" + fullMergedText + '\'' +
                ", retentionPeriod='" + retentionPeriod + '\'' +
                ", documentTypes='" + documentTypes + '\'' +
                ", eventKeywords='" + eventKeywords + '\'' +
                ", processStatus=" + processStatus +
                ", sortOrder=" + sortOrder +
                ", aiCostTimeMs=" + aiCostTimeMs +
                ", aiErrorLog='" + aiErrorLog + '\'' +
                '}';
    }
}