package com.ruoyi.project.system.rule.controller;

import java.util.List;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.system.rule.domain.ArchiveAppraisalRule;
import com.ruoyi.project.system.rule.service.IArchiveAppraisalRuleService;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/system/rule")
public class ArchiveAppraisalRuleController extends BaseController {

    private String prefix = "system/rule";

    @Autowired
    private IArchiveAppraisalRuleService ruleService;

    @RequiresPermissions("system:rule:view")
    @GetMapping()
    public String rule() {
        return prefix + "/rule"; // 对应我们写的 Apple 风 UI
    }

    /**
     * 前端核心数据源接口：查询规则列表
     */
    @RequiresPermissions("system:rule:view")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ArchiveAppraisalRule rule) {
        System.out.println(rule.toString());
        startPage();
        List<ArchiveAppraisalRule> list = ruleService.selectRuleList(rule);
        return getDataTable(list);
    }

    /**
     * 新增保存规则
     */
    @RequiresPermissions("system:rule:add")
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(ArchiveAppraisalRule rule) {
        return toAjax(ruleService.insertRule(rule));
    }

    /**
     * 删除智能鉴定规则
     * 支持单条删除或逗号拼接批量删除
     */
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        try {
            // 调用 Service 层执行删除，toAjax 是若依内置方法，根据影响行数返回成功或失败
            return toAjax(ruleService.deleteRuleByIds(ids));
        } catch (Exception e) {
            return error(e.getMessage());
        }
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
     * 2. 接收上传的文件并进行智能解析
     */
    @Log(title = "规则库智能解析", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    @ResponseBody
    public AjaxResult importData(MultipartFile file, String unitId) {
        try {
            // 调用 Service 层处理文件
            String message = ruleService.importRuleData(file, unitId);
            return AjaxResult.success(message);
        } catch (Exception e) {
            return AjaxResult.error("智能解析失败: " + e.getMessage());
        }
    }
}