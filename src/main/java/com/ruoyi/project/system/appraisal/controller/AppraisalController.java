package com.ruoyi.project.system.appraisal.controller;

import com.ruoyi.framework.web.controller.BaseController;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 参数配置 信息操作处理
 * 
 * @author ruoyi
 */
@Controller
@RequestMapping("/system/appraisal")
public class AppraisalController extends BaseController
{
    private String prefix = "system/appraisal";

    @GetMapping("")
    public String toAppraisal()
    {
        return prefix + "/monitor";
    }

    @GetMapping("/log")
    public String toAppraisalLog()
    {
        return prefix + "/statistics";
    }

    @GetMapping("/rule")
    public String toAppraisalRule()
    {
        return "system/rule" + "/rule";
    }
}
