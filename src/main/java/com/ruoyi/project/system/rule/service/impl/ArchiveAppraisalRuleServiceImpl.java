package com.ruoyi.project.system.rule.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.system.titansort.service.IArchiveCategoryService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.common.utils.text.Convert;
import com.ruoyi.project.system.rule.domain.ArchiveAppraisalRule;
import com.ruoyi.project.system.rule.mapper.ArchiveAppraisalRuleMapper;
import com.ruoyi.project.system.rule.service.IArchiveAppraisalRuleService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ArchiveAppraisalRuleServiceImpl implements IArchiveAppraisalRuleService {

    @Autowired
    private ArchiveAppraisalRuleMapper ruleMapper;

    @Autowired
    private IArchiveCategoryService archiveCategoryService;

    @Override
    public List<ArchiveAppraisalRule> selectRuleList(ArchiveAppraisalRule rule) {
        return ruleMapper.selectRuleList(rule);
    }

    @Override
    public ArchiveAppraisalRule selectRuleById(Long ruleId) {
        return ruleMapper.selectRuleById(ruleId);
    }

    @Override
    public int insertRule(ArchiveAppraisalRule rule) {
        rule.setCreateTime(DateUtils.getNowDate());
        rule.setCreateBy(ShiroUtils.getLoginName());
        return ruleMapper.insertRule(rule);
    }

    @Override
    public int updateRule(ArchiveAppraisalRule rule) {
        rule.setUpdateTime(DateUtils.getNowDate());
        rule.setUpdateBy(ShiroUtils.getLoginName());
        return ruleMapper.updateRule(rule);
    }

    /**
     * 批量删除智能鉴定规则
     * * @param ids 需要删除的数据ID以逗号拼接
     * @return 影响行数
     */
    @Override
    public int deleteRuleByIds(String ids) {
        // Convert.toStrArray 会将 "id1,id2" 转换成 String[] {"id1", "id2"}
        return ruleMapper.deleteRuleByIds(Convert.toStrArray(ids));
    }


    /**
     * 核心业务：全文档智能分块解析与入库 (无伪代码生产版)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importRuleData(MultipartFile file, String unitId) throws Exception {
/*
        if (file == null || file.isEmpty()) throw new Exception("上传的文件不能为空！");
        String fileName = file.getOriginalFilename();

        String fullText = extractTextFromFile(file);
        if (StringUtils.isBlank(fullText)) throw new Exception("未能提取到有效文字。");

        // ====== 1. 数据清洗与精准截取 (你的神仙思路) ======
        int startIndex = fullText.indexOf("二、各门类文件材料归档范围和档案保管期限");
        int endIndex = fullText.indexOf("三、附则");

        if (startIndex != -1) {
            if (endIndex != -1 && endIndex > startIndex) {
                fullText = fullText.substring(startIndex, endIndex);
            } else {
                fullText = fullText.substring(startIndex);
            }
        }

        // ====== 2. 文本分块 (缩小切块，降低单次推理压力) ======
        List<String> textChunks = new ArrayList<>();
        String[] lines = fullText.split("\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String line : lines) {
            currentChunk.append(line).append("\n");
            // 【调优点】：字数降到 1000 左右一块，保证大模型能在 60 秒内迅速吐出结果
            if (currentChunk.length() >= 1000) {
                textChunks.add(currentChunk.toString());
                currentChunk.setLength(0);
            }
        }
        if (currentChunk.length() > 50) {
            textChunks.add(currentChunk.toString());
        }

        // ====== 3. 稳健的串行调用 (Serial Execution) ======
        List<ArchiveAppraisalRule> allParsedRules = new ArrayList<>(); // 串行不需要线程安全的集合了
        int currentChunkIndex = 1;

        for (String chunk : textChunks) {
            System.out.println(">>> 正在串行发送第 " + currentChunkIndex + "/" + textChunks.size() + " 块给大模型...");

            // 串行调用大模型提取规则
            processTextChunk(chunk, allParsedRules);

            // 【大白话】：每处理完一块，让 Java 睡 1 秒，给大模型服务器喘口气
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            currentChunkIndex++;
        }

        // ====== 4. 汇总并批量入库 ======
        int successCount = 0;
        for (ArchiveAppraisalRule rule : allParsedRules) {
            rule.setRuleId(System.nanoTime() + successCount);
            rule.setUnitId(unitId);
            rule.setStatus("0");

            if(StringUtils.isBlank(rule.getCategoryL1())) rule.setCategoryL1("未分类");
            if(StringUtils.isBlank(rule.getRetentionPeriod())) rule.setRetentionPeriod("10年");
            if(rule.getPriority() == null) rule.setPriority(5);

            this.insertRule(rule);
            successCount++;
        }*/

        if (file == null || file.isEmpty()) {
            throw new Exception("导入文件不能为空！");
        }

        // 转发文件流和所属单位ID，交由专业的分类引擎进行多级自关联解析与入库
        archiveCategoryService.importWordCategoryTable(file, unitId);

        return "解析完毕！";//文件核心内容共切分为 " + textChunks.size() + " 块串行处理，成功入库 " + successCount + " 条真实规则。
    }

    /**
     * 工具方法：处理单个文本块，调用大模型并解析 JSON
     */
    private void processTextChunk(String textChunk, List<ArchiveAppraisalRule> allParsedRules) {
        // 严格的 Prompt 设计，告诉 AI 忽略目录、序言等无关废话
        String systemPrompt = "你是一个档案业务专家。请从以下文本中提取保管期限条款。如果文本中包含真实的条款（如“永久”、“30年”、“10年”），请务必严格按JSON数组格式返回；如果该段文本是序言、目录或无关内容，请直接返回空的数组 []。字段要求：categoryL1(一级门类,如WS/KU), categoryL2(二级门类), clauseNo(条款号), clauseText(条款原文), retentionPeriod(保管期限:永久/30年/10年), documentTypes(适用文种,用逗号分隔), eventKeywords(事由关键词,用逗号分隔), priority(优先级数字1-10)。";

        try {
            String jsonResponse = callQwenModel(systemPrompt, textChunk);

            if (StringUtils.isNotBlank(jsonResponse) && !jsonResponse.trim().equals("[]")) {
                // 将大模型返回的 JSON 数组转化为实体类集合
                List<ArchiveAppraisalRule> chunkRules = JSON.parseArray(jsonResponse, ArchiveAppraisalRule.class);
                if (chunkRules != null) {
                    allParsedRules.addAll(chunkRules);
                }
            }
        } catch (Exception e) {
            // 【专业术语】：容错隔离。某个块解析失败（如大模型抽风返回了非JSON），记录日志但跳过，不阻断整个文件的解析。
            System.err.println("该文本块大模型解析失败，跳过: " + e.getMessage());
        }
    }

    // ================= 以下为底层工具方法 (当前留空等待你的信息) =================

    /**
     * 工具方法 1：从文件中抽取纯文本 (优化版)
     */
    private String extractTextFromFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename().toLowerCase();
        StringBuilder text = new StringBuilder();

        try (InputStream is = file.getInputStream()) {
            if (fileName.endsWith(".pdf")) {
                // PDF 解析逻辑保持不变
                PDDocument document = PDDocument.load(is);
                PDFTextStripper stripper = new PDFTextStripper();
                text.append(stripper.getText(document));
                document.close();
            }
            else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                // 【高级写法】：Word 全系大一统解析
                // 交给 POI 的提取器工厂自动判断并解析
                POITextExtractor extractor = ExtractorFactory.createExtractor(is);
                text.append(extractor.getText());
                extractor.close();
            }
            else {
                throw new Exception("不支持的文件格式，仅支持 PDF、DOCX、DOC");
            }
        }
        return text.toString();
    }

    /**
     * 工具方法：调用 OpenAI 标准接口 (保留你的高容忍度配置)
     */
    private String callQwenModel(String systemPrompt, String documentText) throws Exception {
        String apiUrl = "http://172.23.16.126:80/v1/chat/completions";
//        String apiUrl = "http://127.0.0.1:8045/v1/chat/completions";
        String apiKey = "sk-c149638004e04ecc85b9f35abe0db78e";
//        String modelName = "gemini-3.1-pro-high";//claude-opus-4-6-thinking
        String modelName = "qwen";

        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(180000);
        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);

        JSONObject payload = new JSONObject();
        payload.put("model", modelName);
        payload.put("temperature", 0.0); // 降为 0.0，追求绝对的规则严谨性

        JSONArray messages = new JSONArray();
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", "文本内容如下：\n" + documentText);

        messages.add(systemMsg);
        messages.add(userMsg);
        payload.put("messages", messages);

        System.out.println("本次调用ai内容如下：\n"+payload.toString());

        HttpEntity<String> requestEntity = new HttpEntity<>(payload.toJSONString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        System.out.println("ai响应结果：\n"+response.getBody());
        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject resJson = JSON.parseObject(response.getBody());
            String aiContent = resJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            aiContent = aiContent.replace("```json", "").replace("```", "").trim();
            return aiContent;
        } else {
            throw new Exception("HTTP 请求失败");
        }
    }
}