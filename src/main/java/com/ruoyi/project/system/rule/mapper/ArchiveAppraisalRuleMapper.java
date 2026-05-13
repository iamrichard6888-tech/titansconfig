package com.ruoyi.project.system.rule.mapper;

import com.ruoyi.project.system.rule.domain.ArchiveAppraisalRule;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 档案保管期限鉴定规则Mapper接口
 */
public interface ArchiveAppraisalRuleMapper {
    /** 查询规则列表 */
    public List<ArchiveAppraisalRule> selectRuleList(ArchiveAppraisalRule rule);

    /** 批量插入结构化规则森林 */
    public int batchInsertRules(List<ArchiveAppraisalRule> list);

    /** 幂等操作：按全宗和门类物理清理旧数据 */
    public int deleteRulesByQzhAndCategory(@Param("qzh") String qzh, @Param("categoryCode") String categoryCode);

    /** 物理删除单条规则 */
    public int deleteRuleById(String ruleId);

    /** 幂等保障：物理清理某单位下所有的旧混合规则数据 */
    public int deleteRulesByQzh(String qzh);
    // 按ID查询
    public ArchiveAppraisalRule selectArchiveAppraisalRuleById(String ruleId);

    // 插入数据
    public int insertArchiveAppraisalRule(ArchiveAppraisalRule rule);

    // 更新数据
    public int updateArchiveAppraisalRule(ArchiveAppraisalRule rule);
}