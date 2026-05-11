package com.ruoyi.project.system.rule.mapper;

import java.util.List;
import com.ruoyi.project.system.rule.domain.ArchiveAppraisalRule;

/**
 * 档案鉴定规则知识库 Mapper接口
 * * @author ruoyi
 * @date 2026-05-04
 */
public interface ArchiveAppraisalRuleMapper {
    /**
     * 查询档案鉴定规则
     * * @param ruleId 档案鉴定规则ID
     * @return 档案鉴定规则
     */
    public ArchiveAppraisalRule selectRuleById(Long ruleId);

    /**
     * 查询档案鉴定规则列表
     * * @param rule 档案鉴定规则
     * @return 档案鉴定规则集合
     */
    public List<ArchiveAppraisalRule> selectRuleList(ArchiveAppraisalRule rule);

    /**
     * 新增档案鉴定规则
     * * @param rule 档案鉴定规则
     * @return 结果
     */
    public int insertRule(ArchiveAppraisalRule rule);

    /**
     * 修改档案鉴定规则
     * * @param rule 档案鉴定规则
     * @return 结果
     */
    public int updateRule(ArchiveAppraisalRule rule);

    /**
     * 删除档案鉴定规则
     * * @param ruleId 档案鉴定规则ID
     * @return 结果
     */
    public int deleteRuleById(Long ruleId);

    /**
     * 批量删除智能鉴定规则
     * * @param ruleIds 需要删除的数据ID数组
     * @return 影响行数
     */
    public int deleteRuleByIds(String[] ruleIds);
}