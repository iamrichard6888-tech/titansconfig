package com.ruoyi.project.system.rule.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BgeEmbeddingClient {

    // 💡 核心开关：当前处于什么环境？可选值："ollama" 或 "mindie"
    // 你可以在 application.yml 中配置 bge.api-mode，默认走本地 ollama
    @Value("${bge.api-mode:ollama}")
    private String apiMode;

    // API 地址
    @Value("${bge.api-url:http://localhost:11434/api/embed}")
    private String apiUrl;

    // 模型名称
    @Value("${bge.model-name:quentinz/bge-large-zh-v1.5}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 👑 统一路由入口：Service层只需调用这个方法，内部自动根据环境分发
     */
    public List<Float> getEmbedding(String text) {
        if ("mindie".equalsIgnoreCase(apiMode)) {
            return getEmbeddingFromMindIE(text);
        } else {
            return getEmbeddingFromOllama(text);
        }
    }

    /**
     * 1. 针对【现场 MindIE 环境】的解析引擎
     */
    public List<Float> getEmbeddingFromMindIE(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", text); // 注意：MindIE 要求是 inputs (带s)
        payload.put("model", modelName);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 💡 MindIE 直接返回原生二维数组： [[0.05, 0.04, ...]]
                JSONArray rootArr = JSON.parseArray(response.getBody());
                if (rootArr != null && !rootArr.isEmpty()) {
                    JSONArray vectorArr = rootArr.getJSONArray(0);
                    List<Float> vector = new ArrayList<>();
                    for (int i = 0; i < vectorArr.size(); i++) {
                        vector.add(vectorArr.getFloat(i));
                    }
                    return vector;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("MindIE 向量服务调用失败: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * 2. 针对【本地 Ollama 环境】的解析引擎
     */
    public List<Float> getEmbeddingFromOllama(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("input", text); // 注意：Ollama 要求是 input (不带s)
        payload.put("model", modelName);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 💡 Ollama 返回包装对象： {"embeddings": [[0.05, ...]]}
                JSONObject resObj = JSON.parseObject(response.getBody());
                JSONArray dataArr = resObj.getJSONArray("embeddings");
                if (dataArr != null && !dataArr.isEmpty()) {
                    JSONArray vectorArr = dataArr.getJSONArray(0);
                    List<Float> vector = new ArrayList<>();
                    for (int i = 0; i < vectorArr.size(); i++) {
                        vector.add(vectorArr.getFloat(i));
                    }
                    return vector;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Ollama 向量服务调用失败: " + e.getMessage());
        }
        return new ArrayList<>();
    }
}