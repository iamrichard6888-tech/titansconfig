package com.ruoyi.project.system.checklist.mapper;

import com.ruoyi.project.system.checklist.domain.CheckList;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 用户表 数据层
 * 
 * @author ruoyi
 */
public interface CheckListMapper
{
    /**
     * 根据条件分页查询用户列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */

	@Select("SELECT ROWNUM as serial,t.* FROM (SELECT unitid,oldSysFunction,oldSysSort,oldSysDataValues,oldSysFileValues,newSysFunction,newSysSort,newSysDataValues,newSysFileValues FROM checklistbyunitid WHERE unitid = '${unitid}' ORDER BY ORDERNUM ASC ,OLDSYSSORT ASC ) t")
	public List<CheckList> selectCheckListByUnitid(@Param("unitid") String unitid);
}
