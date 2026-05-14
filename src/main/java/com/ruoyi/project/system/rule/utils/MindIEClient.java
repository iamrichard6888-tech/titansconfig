package com.ruoyi.project.system.rule.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MindIEClient {

    @Value("${mindie.api-url:https://onlineai.cc/v1/chat/completions}")
    private String apiUrl;

    @Value("${mindie.api-key:sk-6iRhVcYyRvbMcEvh0zJlZLQNL9kEJZx9Qt8oR3un9BbsJUOn}") // 鉴权配置保留
    private String apiKey;

    @Value("${mindie.model-name:gpt-5.5}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("^```json\\s*([\\s\\S]*?)\\s*```$", Pattern.MULTILINE);

    /**
     * 🧠 引入层级路径的全景元数据增强引擎 (调试期通用兼容版)
     * @param parentPathText 祖先路径前缀 (如 "各部门共有部分 / 会议文件 / 全区行业性会议")
     * @param rawClauseText  底层条款正文文本
     */
    public JSONObject enhanceRuleMetadata(String parentPathText, String rawClauseText) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotEmpty(apiKey)) {
            headers.setBearerAuth(apiKey);
        }

        // 组装极具推导引导性的 System 提示词
        String systemPrompt = "你是一个专业的档案元数据与语义特征提炼专家。\n" +
                "任务：请结合提供的【所属层级路径】和【条款正文】，精准归纳出真实的核心事由与包含的公文文种。\n" +
                "【输出强制规范】：\n" +
                "1. 必须仅输出合法的JSON对象或JSON数组，绝对不要包含任何解释性文字或Markdown外壳。\n" +
                "2. 提取的JSON键名严格固定为 \"事由\" 和 \"文种\"。\n" +
                "3. 若条款正文包含多项不同性质的条目，请输出JSON数组格式分别提取；若内容紧密单一，输出单个JSON对象。";

        // 拼接带有前因后果的用户载荷
        String cleanPath = StringUtils.isNotEmpty(parentPathText) ? parentPathText.trim() : "独立基础业务";
        String userPromptPayload = "所属层级路径：" + cleanPath + "\n条款正文：" + rawClauseText;

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", modelName);
        payload.put("temperature", 0.1); // 维持绝对稳定的解码轨道
        payload.put("max_tokens", 300);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMsg("system", systemPrompt));
        messages.add(createMsg("user", userPromptPayload));
        payload.put("messages", messages);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            System.out.println("=============Response:"+response.getBody());
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject resObj = JSON.parseObject(response.getBody());
                String rawContent = resObj.getJSONArray("choices")
                        .getJSONObject(0).getJSONObject("message").getString("content");

                // 1. 强力剥离外壳
                String cleanJson = cleanJsonString(rawContent);

                // 2. 智能去重降维合并机制：有效消融模型生成的重复数据
                if (cleanJson.startsWith("[")) {
                    JSONArray arr = JSON.parseArray(cleanJson);
                    // 采用有序去重集合，屏蔽如 "材料、材料、简报" 的冗余噪音
                    Set<String> events = new LinkedHashSet<>();
                    Set<String> types = new LinkedHashSet<>();

                    for (int i = 0; i < arr.size(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        String e = extractKey(item, "事由", "核心事由", "event");
                        String t = extractKey(item, "文种", "公文文种", "type");
                        if (StringUtils.isNotEmpty(e)) events.add(e);
                        if (StringUtils.isNotEmpty(t)) types.add(t);
                    }
                    JSONObject mergedObj = new JSONObject();
                    mergedObj.put("事由", StringUtils.join(events, "、"));
                    mergedObj.put("文种", StringUtils.join(types, "、"));
                    return mergedObj;
                } else {
                    JSONObject singleObj = JSON.parseObject(cleanJson);
                    JSONObject standardObj = new JSONObject();
                    standardObj.put("事由", extractKey(singleObj, "事由", "核心事由", "event"));
                    standardObj.put("文种", extractKey(singleObj, "文种", "公文文种", "type"));
                    return standardObj;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("大模型推理结果集成中断: " + e.getMessage());
        }
        return null;
    }

    private String cleanJsonString(String raw) {
        if (StringUtils.isEmpty(raw)) return "";
        String clean = raw.trim();
        Matcher matcher = JSON_BLOCK_PATTERN.matcher(clean);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return clean.replaceAll("^```json", "").replaceAll("```$", "").trim();
    }

    private String extractKey(JSONObject obj, String... possibleKeys) {
        for (String k : possibleKeys) {
            if (obj.containsKey(k) && StringUtils.isNotEmpty(obj.getString(k))) {
                return obj.getString(k).trim();
            }
        }
        return "";
    }

    private Map<String, String> createMsg(String role, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }
}