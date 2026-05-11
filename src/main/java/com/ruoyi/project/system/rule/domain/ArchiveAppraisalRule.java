package com.ruoyi.project.system.rule.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 档案鉴定规则知识库 对象 archive_appraisal_rule
 */
public class ArchiveAppraisalRule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 规则ID (雪花算法ID) */
    private Long ruleId;

    /** 关联 t_DA_UNIT 单位ID */
    @Excel(name = "单位ID")
    private String unitId;

    /** 全宗号 */
    @Excel(name = "全宗号")
    private String unitCode;

    /** 一级门类 (如 WS) */
    @Excel(name = "一级门类")
    private String categoryL1;

    /** 二级门类 (如 会议文件) */
    @Excel(name = "二级门类")
    private String categoryL2;

    /** 条款号 (如 1.1) */
    @Excel(name = "条款号")
    private String clauseNo;

    /** 条款原文 */
    @Excel(name = "条款原文")
    private String clauseText;

    /** 保管期限 (永久/30年/10年) */
    @Excel(name = "保管期限")
    private String retentionPeriod;

    /** 适用文种 (逗号分隔或JSON) */
    @Excel(name = "适用文种")
    private String documentTypes;

    /** 事由关键词 (逗号分隔或JSON) */
    @Excel(name = "事由关键词")
    private String eventKeywords;

    /** 排除文种 */
    @Excel(name = "排除文种")
    private String excludedTypes;

    /** 排除事由 */
    @Excel(name = "排除事由")
    private String excludedKeywords;

    /** 匹配优先级 (1-10) */
    @Excel(name = "优先级")
    private Integer priority;

    /** 状态 (0正常 1停用) */
    @Excel(name = "状态")
    private String status;

    // ----- Getters 和 Setters -----

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

    public String getUnitId() { return unitId; }
    public void setUnitId(String unitId) { this.unitId = unitId; }

    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }

    public String getCategoryL1() { return categoryL1; }
    public void setCategoryL1(String categoryL1) { this.categoryL1 = categoryL1; }

    public String getCategoryL2() { return categoryL2; }
    public void setCategoryL2(String categoryL2) { this.categoryL2 = categoryL2; }

    public String getClauseNo() { return clauseNo; }
    public void setClauseNo(String clauseNo) { this.clauseNo = clauseNo; }

    public String getClauseText() { return clauseText; }
    public void setClauseText(String clauseText) { this.clauseText = clauseText; }

    public String getRetentionPeriod() { return retentionPeriod; }
    public void setRetentionPeriod(String retentionPeriod) { this.retentionPeriod = retentionPeriod; }

    public String getDocumentTypes() { return documentTypes; }
    public void setDocumentTypes(String documentTypes) { this.documentTypes = documentTypes; }

    public String getEventKeywords() { return eventKeywords; }
    public void setEventKeywords(String eventKeywords) { this.eventKeywords = eventKeywords; }

    public String getExcludedTypes() { return excludedTypes; }
    public void setExcludedTypes(String excludedTypes) { this.excludedTypes = excludedTypes; }

    public String getExcludedKeywords() { return excludedKeywords; }
    public void setExcludedKeywords(String excludedKeywords) { this.excludedKeywords = excludedKeywords; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("ruleId", getRuleId())
                .append("unitId", getUnitId())
                .append("unitCode", getUnitCode())
                .append("categoryL1", getCategoryL1())
                .append("categoryL2", getCategoryL2())
                .append("clauseNo", getClauseNo())
                .append("clauseText", getClauseText())
                .append("retentionPeriod", getRetentionPeriod())
                .append("documentTypes", getDocumentTypes())
                .append("eventKeywords", getEventKeywords())
                .append("excludedTypes", getExcludedTypes())
                .append("excludedKeywords", getExcludedKeywords())
                .append("priority", getPriority())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}