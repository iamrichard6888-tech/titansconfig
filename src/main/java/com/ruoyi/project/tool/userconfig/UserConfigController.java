package com.ruoyi.project.tool.userconfig;

import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.system.userconfig.domain.UserImportTemp;
import com.ruoyi.project.system.userconfig.mapper.UserImportTempMapper;
import com.ruoyi.project.system.userconfig.service.UserConfigServiceImpl;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.ModelMap;
import java.util.List;

@Controller
@RequestMapping("/tool/userconfig")
public class UserConfigController extends BaseController {
    private String prefix = "tool/userconfig";

    @Autowired
    private UserConfigServiceImpl userConfigService;

    @Autowired
    private UserImportTempMapper tempMapper;

    @RequiresPermissions("tool:userconfig:config")
    @GetMapping()
    public String userImport() {
        // 这代表会去寻找 src/main/resources/templates/system/userconfig/importTest.html 文件
        return prefix+"/userImport";
    }

    @RequiresPermissions("tool:userconfig:config")
    @PostMapping("/listTemp")
    @ResponseBody
    public TableDataInfo listTemp(UserImportTemp temp) {
        startPage();
        List<UserImportTemp> list = userConfigService.selectTempList(temp);
        return getDataTable(list);
    }

    @RequiresPermissions("tool:userconfig:config")
    @PostMapping("/importWord")
    @ResponseBody
    public AjaxResult importWord(@RequestParam("files") MultipartFile[] files) {
        try {
            int count = userConfigService.importAndParseWord(files);
            return AjaxResult.success("解析成功！共提取了 " + count + " 条数据。");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("解析失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("tool:userconfig:config")
    @PostMapping("/saveToFormal")
    @ResponseBody
    public AjaxResult saveToFormal(String ids) { // 增加 ids 参数接收
        try {
            String resultMsg = userConfigService.saveToFormal(ids);
            return AjaxResult.success(resultMsg);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @RequiresPermissions("tool:userconfig:config")
    @GetMapping("/add")
    public String add(ModelMap mmap) {
        // 把查出来的全宗列表塞给前端
        mmap.put("organList", userConfigService.selectOrganList());
        return prefix + "/add";
    }

    /**
     * 校验登录账号是否唯一（供前端实时调用）
     */
    @PostMapping("/checkLoginAccountUnique")
    @ResponseBody
    public String checkLoginAccountUnique(UserImportTemp temp) {
        String s = userConfigService.checkLoginAccountExist(temp);
        return s;
    }

    @RequiresPermissions("tool:userconfig:config")
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(UserImportTemp temp) {
        return toAjax(userConfigService.insertUserImportTemp(temp));
    }

    @RequiresPermissions("tool:userconfig:config")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, ModelMap mmap) {
        mmap.put("userTemp", userConfigService.selectUserImportTempById(id));
        // 修改页面同样需要这个下拉列表
        mmap.put("organList", userConfigService.selectOrganList());
        return prefix + "/edit";
    }

    @RequiresPermissions("tool:userconfig:config")
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(UserImportTemp temp) {
        return toAjax(userConfigService.updateUserImportTemp(temp));
    }

    @RequiresPermissions("tool:userconfig:config")
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) throws Exception {
        return toAjax(userConfigService.deleteUserImportTempByIds(ids));
    }

    /**
     * 审核暂存条目：将状态从 1(需核对) 转为 0(待确认)
     */
    @RequiresPermissions("tool:userconfig:config")
    @PostMapping("/audit")
    @ResponseBody
    public AjaxResult audit(String ids) {
        try {
            return toAjax(userConfigService.auditTemp(ids));
        } catch (Exception e) {
            return AjaxResult.error("审核失败：" + e.getMessage());
        }
    }



}
