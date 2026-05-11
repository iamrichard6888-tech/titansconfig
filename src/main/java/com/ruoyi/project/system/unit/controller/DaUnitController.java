package com.ruoyi.project.system.unit.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.Ztree;
import com.ruoyi.project.system.unit.domain.DaUnit;
import com.ruoyi.project.system.unit.service.IDaUnitService;

@Controller
@RequestMapping("/system/unit")
public class DaUnitController extends BaseController {

    @Autowired
    private IDaUnitService daUnitService;

    /**
     * 加载全宗单位的树形列表数据 (适配扁平表结构 + String主键 + 全宗号拼接)
     */
    @GetMapping("/treeData")
    @ResponseBody
    public List<Map<String, Object>> treeData() {
        List<DaUnit> unitList = daUnitService.selectUnitList(new DaUnit());
        List<Map<String, Object>> ztrees = new ArrayList<>();

        // 虚拟根节点
        Map<String, Object> root = new HashMap<>();
        root.put("id", "0");
        root.put("pId", "-1");
        root.put("name", "全部监控单位");
        root.put("title", "全部监控单位");
        root.put("open", true);
        ztrees.add(root);

        // 遍历拼接
        for (DaUnit unit : unitList) {
            Map<String, Object> ztree = new HashMap<>();

            ztree.put("id", unit.getUnitid());
            ztree.put("pId", "0");

            // 【核心修改 1】：安全获取全宗号并处理 Null 或空字符串
            String qzhStr = (unit.getQzh() != null && !unit.getQzh().trim().isEmpty()) ? unit.getQzh().trim() : "";

            // 【核心修改 2】：拼接 "全宗号 + 单位名称" (如果没有全宗号则只显示名称)
            String displayName = qzhStr.isEmpty() ? unit.getName() : qzhStr + " " + unit.getName();
            ztree.put("name", displayName);

            // 鼠标悬停提示词保持专业感
            ztree.put("title", unit.getName() + " (" + (qzhStr.isEmpty() ? "无全宗号" : qzhStr) + ")");

            ztrees.add(ztree);
        }

        return ztrees;
    }
}