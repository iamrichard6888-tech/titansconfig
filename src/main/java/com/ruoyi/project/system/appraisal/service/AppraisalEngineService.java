package com.ruoyi.project.system.appraisal.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import com.ruoyi.project.system.rule.domain.ArchiveAppraisalRule;
import com.ruoyi.project.system.rule.mapper.ArchiveAppraisalRuleMapper;

@Service
public class AppraisalEngineService {

    @Autowired
    private ArchiveAppraisalRuleMapper ruleMapper;

    /**
     * 智能鉴定主干流程 (核心业务逻辑)
     * @param title 档案题名 (如："关于召开2026年全区档案工作会议的通知")
     * @param unitId 请求单位的ID
     * @return 鉴定结果对象 (包含期限和命中的规则)
     */
    public AppraisalResult fastMatch(String title, String unitId) {

        // 1. 获取该单位的所有可用规则 (注意：SQL里我们已经按 PRIORITY DESC 倒序排好了)
        ArchiveAppraisalRule query = new ArchiveAppraisalRule();
        query.setUnitId(unitId);
        query.setStatus("0"); // 只查启用状态的
        List<ArchiveAppraisalRule> rules = ruleMapper.selectRuleList(query);

        // 2. 规则遍历匹配引擎 (速度极快，纯内存操作)
        for (ArchiveAppraisalRule rule : rules) {

            // [拦截门槛 A]：排除词检测 (神仙设计生效)
            // 如果题名包含"通知"，但本规则的排除文种是"通知"，直接跳过本规则！
            if (isHitKeywords(title, rule.getExcludedTypes()) ||
                    isHitKeywords(title, rule.getExcludedKeywords())) {
                continue;
            }

            // [拦截门槛 B]：正向命中检测
            // 必须同时命中"事由"和"文种" (如果规则里配了的话)
            boolean hitEvent = isHitKeywords(title, rule.getEventKeywords());
            boolean hitType = isHitKeywords(title, rule.getDocumentTypes());

            if (hitEvent && hitType) {
                // 完美命中！直接返回结果，不再往下走了，也不用调大模型了！
                return new AppraisalResult(
                        rule.getRetentionPeriod(),
                        rule.getRuleId(),
                        "正则引擎极速命中",
                        "命中条款: " + rule.getClauseNo() + " - " + rule.getClauseText()
                );
            }
        }

        // 3. 所有本地规则都没拦截住？
        // 触发降级策略：调用 Milvus 向量检索 -> 最后兜底调 Qwen 14B 大模型
        return callLlmFallback(title, unitId);
    }

    /**
     * 辅助方法：判断题名是否包含逗号分隔的关键词列表中的任意一个
     * (比如 keywords = "纪要,记录,决议"，只要题名包含其一即为 true)
     */
    private boolean isHitKeywords(String text, String keywords) {
        if (StringUtils.isBlank(keywords)) {
            return true; // 如果规则没配关键词，默认这项校验通过
        }
        if (StringUtils.isBlank(text)) {
            return false;
        }

        // 按逗号切分关键词，遍历匹配
        String[] keywordArray = keywords.split(",");
        for (String kw : keywordArray) {
            if (StringUtils.isNotBlank(kw) && text.contains(kw.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 大模型兜底接口 (伪代码框架)
     */
    private AppraisalResult callLlmFallback(String title, String unitId) {
        // TODO: 1. 组装 Prompt
        // TODO: 2. 通过 HttpUtil 或专用 SDK 发送请求给你的 Qwen 14B (context: 4K)
        // TODO: 3. 解析大模型返回的 JSON
        return new AppraisalResult("30年", null, "大模型兜底", "AI深度推理意图: ...");
    }

    // (内部类：返回结果封装)
    public class AppraisalResult {
        public String period;       // 保管期限 (永久/30年/10年)
        public Long hitRuleId;      // 命中的规则ID
        public String matchPath;    // 匹配路径
        public String reasoning;    // 推理依据

        public AppraisalResult(String p, Long id, String path, String r) {
            this.period = p; this.hitRuleId = id; this.matchPath = path; this.reasoning = r;
        }
    }


}