package com.ruoyi.project.system.titansort.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.domain.Ztree;
import com.ruoyi.project.system.titansort.domain.ArchiveCategory;
import com.ruoyi.project.system.titansort.service.IArchiveCategoryService;

/**
 * 档案分类树 Controller
 */
@Controller
@RequestMapping("/system/categoryTree")
public class ArchiveCategoryController extends BaseController
{
    @Autowired
    private IArchiveCategoryService archiveCategoryTreeService;

    /**
     * 提供给前端加载左侧 Ztree 树结构的接口
     */
    @PostMapping("/treeData")
    @ResponseBody
    public List<Ztree> treeData(ArchiveCategory archiveCategoryTree) {
        return archiveCategoryTreeService.selectCategoryTree(archiveCategoryTree);
    }

    /**
     * 导入 Word 分类表接口
     */
    @PostMapping("/importWord")
    @ResponseBody
    public AjaxResult importWord(@RequestParam("file") MultipartFile file, @RequestParam("unitId") String unitId) {
        try {
            archiveCategoryTreeService.importWordCategoryTable(file, unitId);
            return success("分类表结构成功导入！");
        } catch (Exception e) {
            return error("导入解析失败: " + e.getMessage());
        }
    }
}