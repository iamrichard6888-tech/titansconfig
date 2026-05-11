package com.ruoyi.project.system.rule.service;

import java.util.List;
import com.ruoyi.project.system.rule.domain.ArchiveAppraisalRule;
import org.springframework.web.multipart.MultipartFile;

public interface IArchiveAppraisalRuleService {
    /** 查询规则列表 */
    public List<ArchiveAppraisalRule> selectRuleList(ArchiveAppraisalRule rule);

    /** 根据ID查询单条规则 */
    public ArchiveAppraisalRule selectRuleById(Long ruleId);

    /** 新增规则 */
    public int insertRule(ArchiveAppraisalRule rule);

    /** 修改规则 */
    public int updateRule(ArchiveAppraisalRule rule);

    /** 智能导入规则数据 */
    public String importRuleData(MultipartFile file, String unitId) throws Exception;

    /**
     * 批量删除智能鉴定规则
     * * @param ids 需要删除的数据ID以逗号拼接
     * @return 结果
     */
    public int deleteRuleByIds(String ids);
}