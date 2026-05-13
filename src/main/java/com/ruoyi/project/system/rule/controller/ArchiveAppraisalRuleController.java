package com.ruoyi.project.system.rule.controller;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.common.utils.security.ShiroUtils; // 若依单机版标准用户上下文工具
import com.ruoyi.project.system.rule.domain.ArchiveAppraisalRule;
import com.ruoyi.project.system.rule.service.IArchiveAppraisalRuleService;
import java.util.List;

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
}