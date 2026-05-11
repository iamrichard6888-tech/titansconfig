package com.ruoyi.project.system.checklist.service;


import com.ruoyi.project.system.checklist.domain.CheckList;

import java.util.List;
import java.util.Map;

/**
 * 用户 业务层
 * 
 * @author ruoyi
 */
public interface ICheckListService
{
    public List<CheckList> getCheckListByUnitid(String unitid);
    public List<Map> getUnitByUnitid(String unitid);
}
