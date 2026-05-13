package com.ruoyi.project.system.rule.service;

import com.ruoyi.project.system.rule.domain.ArchiveAppraisalRule;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IArchiveAppraisalRuleService {
    /** 查询规则大盘数据 */
    public List<ArchiveAppraisalRule> selectRuleList(ArchiveAppraisalRule rule);

    /**
     * 🚀 终极版：直接读取上传的 Word/Excel 文件流，提取内容并执行智能血缘解析
     * @param qzh 全宗号
     * @param file 物理上传文件流
     * @param createBy 当前操作人
     */
    public int importRuleDocument(String qzh, MultipartFile file, String createBy) throws Exception;

    /** 物理删除规则 */
    public int deleteRuleByIds(String ids);
    /**
     * 根据规则主键ID精准查询单条规则实体 (用于编辑界面的数据穿透回显)
     * * @param ruleId 规则ID
     * @return 规则实体对象
     */
    public ArchiveAppraisalRule selectArchiveAppraisalRuleById(String ruleId);

    /**
     * 新增单条鉴定规则 (支撑前台手工添加规则落地)
     * * @param rule 规则实体信息
     * @return 结果 (返回受影响的行数，大于0代表成功)
     */
    public int insertArchiveAppraisalRule(ArchiveAppraisalRule rule);

    /**
     * 修改更新鉴定规则 (支撑编辑界面的数据安全覆盖)
     * * @param rule 规则实体信息
     * @return 结果 (返回受影响的行数，大于0代表成功)
     */
    public int updateArchiveAppraisalRule(ArchiveAppraisalRule rule);
}