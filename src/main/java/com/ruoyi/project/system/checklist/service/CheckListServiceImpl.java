package com.ruoyi.project.system.checklist.service;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.project.system.checklist.domain.CheckList;
import com.ruoyi.project.system.checklist.mapper.CheckListMapper;
import com.ruoyi.project.system.file.mapper.FilesDataMapper;
import com.ruoyi.project.system.sort.mapper.SortMapper;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户 业务层处理
 * 
 * @author ruoyi
 */
@Service
public class CheckListServiceImpl implements ICheckListService
{
    private static final Logger log = LoggerFactory.getLogger(CheckListServiceImpl.class);

    @Autowired
    private CheckListMapper checkListMapper;

    @Autowired
    private SortMapper sortMapper;

    @Autowired
    private FilesDataMapper filesDataMapper;

    public List<CheckList> getCheckListByUnitid(String unitid) {
        List<CheckList> checkLists = checkListMapper.selectCheckListByUnitid(unitid);
        System.out.println(checkLists.toString());
        return checkLists;
    }

    public List<Map> getUnitByUnitid(String unitid){
        List<Map> maps = filesDataMapper.selectUnitList(unitid);
        return maps;
    };

}
