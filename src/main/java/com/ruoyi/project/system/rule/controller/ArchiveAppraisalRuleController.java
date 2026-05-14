package com.ruoyi.project.system.rule.controller;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.common.utils.security.ShiroUtils; // 若依单机版标准用户上下文工具
import com.ruoyi.project.system.rule.domain.ArchiveAppraisalRule;
import com.ruoyi.project.system.rule.service.IArchiveAppraisalRuleService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ruoyi.project.system.titansort.service.IArchiveCategoryService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/system/rule")
public class ArchiveAppraisalRuleController extends BaseController {
    private String prefix = "system/rule";
    @Autowired
    private IArchiveAppraisalRuleService ruleService;

    @Autowired
    private IArchiveCategoryService categoryService;


    @RequiresPermissions("system:rule:view")
    @GetMapping()
    public String rule() {
        return prefix + "/rule"; // 对应我们写的 Apple 风 UI
    }

    /**
     * 1. 打开智能导入弹窗 (带上左侧选中的单位ID)
     */
    @GetMapping("/import/{unitId}")
    public String importPage(@PathVariable("unitId") String unitId, ModelMap mmap) {
        // 将单位ID塞入Model，传给前端HTML
        mmap.put("unitId", unitId);
        return prefix + "/import";
    }

    /**
     * 2. 接收前端终端日志界面的真实文件上传与自动路由请求
     * 对应 import.html 里的 url: ctx + "system/rule/importData"
     */
    @PostMapping("/importData")
    @ResponseBody
    public AjaxResult importData(@RequestParam("unitId") String unitId,
                                 @RequestParam("file") MultipartFile file) {
        try {
            // 获取当前登录用户
            String createBy = ShiroUtils.getLoginName();
            // 启动后端全自动游标路由引擎
            int rows = ruleService.importRuleDocument(unitId, file, createBy);
            ruleService.startAsyncBatchLlmEnhancement(unitId, "");
            return AjaxResult.success("解析大满贯！系统已自动分拣并为您构建了 " + rows + " 条具备树形血缘关系的立体规则节点。");
        } catch (Exception e) {
            return AjaxResult.error("导入失败: " + e.getMessage());
        }
    }

    /** 列表加载 */
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ArchiveAppraisalRule rule) {
        startPage();
        List<ArchiveAppraisalRule> list = ruleService.selectRuleList(rule);
        return getDataTable(list);
    }


    /** 删除 */
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(ruleService.deleteRuleByIds(ids));
    }

    /**
     * 1. 渲染新增UI弹窗 (智能继承当前选中的全宗与门类)
     */
    @GetMapping("/add/{unitId}")
    public String add(@PathVariable("unitId") String unitId,
                      @RequestParam(value = "categoryCode", required = false) String categoryCode,
                      ModelMap mmap) {
        mmap.put("unitId", unitId);
        mmap.put("categoryCode", categoryCode);
        return prefix + "/add";
    }

    /**
     * 2. 执行新增落地保存
     */
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(ArchiveAppraisalRule rule) {
        // 注入主键与操作人上下文
        rule.setRuleId(UUID.randomUUID().toString());
        rule.setCreateBy(ShiroUtils.getLoginName());
        rule.setUpdateBy(ShiroUtils.getLoginName());

        // 自动组装大模型专用的全景融合文本载荷
        String fullMerged = StringUtils.isEmpty(rule.getParentPathText()) ? rule.getClauseText()
                : rule.getParentPathText() + " / " + rule.getClauseText();
        rule.setFullMergedText(fullMerged);

        // 默认标记为人工手动创建态 (状态码可自定义，此处设为2代表已核验成品)
        rule.setProcessStatus(2);

        return toAjax(ruleService.insertArchiveAppraisalRule(rule));
    }

    /**
     * 3. 渲染修改UI弹窗 (数据穿透回显)
     */
    @GetMapping("/edit/{ruleId}")
    public String edit(@PathVariable("ruleId") String ruleId, ModelMap mmap) {
        ArchiveAppraisalRule rule = ruleService.selectArchiveAppraisalRuleById(ruleId);
        mmap.put("rule", rule);
        return prefix + "/edit";
    }

    /**
     * 4. 执行修改落地保存
     */
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(ArchiveAppraisalRule rule) {
        rule.setUpdateBy(ShiroUtils.getLoginName());

        // 同步刷新特征融合载荷
        String fullMerged = StringUtils.isEmpty(rule.getParentPathText()) ? rule.getClauseText()
                : rule.getParentPathText() + " / " + rule.getClauseText();
        rule.setFullMergedText(fullMerged);

        return toAjax(ruleService.updateArchiveAppraisalRule(rule));
    }

    /**
     * 专属支撑：为编辑界面的“层级结构调整”框提供目标门类下纯净的【非叶子节点树】
     * 强过滤条件：retention_period 必须为空（纯结构目录）
     */
    @PostMapping("/folderTree")
    @ResponseBody
    public AjaxResult folderTree(@RequestParam("qzh") String qzh,
                                 @RequestParam("categoryCode") String categoryCode,
                                 @RequestParam(value = "excludeRuleId", required = false) String excludeRuleId) {
        ArchiveAppraisalRule param = new ArchiveAppraisalRule();
        param.setQzh(qzh);
        param.setCategoryCode(categoryCode);
        // 调度 Service 查询大盘数据
        List<ArchiveAppraisalRule> allRules = ruleService.selectRuleList(param);

        // 内存级清洗：仅保留非叶子节点，且物理剔除当前正在编辑的节点及其下属分支以防死循环
        List<ArchiveAppraisalRule> pureFolders = new ArrayList<>();
        Set<String> invalidIds = new HashSet<>();
        if (StringUtils.isNotEmpty(excludeRuleId)) {
            invalidIds.add(excludeRuleId);
            // 找出所有直系子孙ID存入黑名单
            collectDescendantIds(allRules, excludeRuleId, invalidIds);
        }

        for (ArchiveAppraisalRule r : allRules) {
            boolean isFolder = StringUtils.isEmpty(r.getRetentionPeriod());
            if (isFolder && !invalidIds.contains(r.getRuleId())) {
                pureFolders.add(r);
            }
        }
        return AjaxResult.success(pureFolders);
    }

    /** 辅助递归搜集失效子孙ID */
    private void collectDescendantIds(List<ArchiveAppraisalRule> list, String parentId, Set<String> ids) {
        for (ArchiveAppraisalRule r : list) {
            if (parentId.equals(r.getParentId())) {
                ids.add(r.getRuleId());
                collectDescendantIds(list, r.getRuleId(), ids);
            }
        }
    }

    /**
     * 1. 响应前端大盘右上角实时轮询进度的诉求
     */
    @PostMapping("/enhancementProgress")
    @ResponseBody
    public AjaxResult enhancementProgress(@RequestParam("qzh") String qzh,
                                          @RequestParam("categoryCode") String categoryCode) {
        return AjaxResult.success(ruleService.getBatchEnhancementProgress(qzh, categoryCode));
    }


    /**
     * 手动触发批量 AI 语义浓缩（用于调用失败数据的重试或断点续传）
     */
    @PostMapping("/batchEnhance")
    @ResponseBody
    public AjaxResult batchEnhance(@RequestParam("qzh") String qzh,
                                   @RequestParam(value = "categoryCode", required = false) String categoryCode) {
        if (StringUtils.isEmpty(qzh)) {
            return AjaxResult.error("请先选择目标归属单位");
        }
        // 启动后台大模型异步消费线程池进行重试提取
        ruleService.startAsyncBatchLlmEnhancement(qzh, StringUtils.trimToEmpty(categoryCode));
        return AjaxResult.success("增强重试指令已下发！后台正为您静默提炼未浓缩的语义特征。");
    }
}