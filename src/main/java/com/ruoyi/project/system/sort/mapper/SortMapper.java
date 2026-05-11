package com.ruoyi.project.system.sort.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.ruoyi.project.system.dept.domain.Dept;
import com.ruoyi.project.system.sort.domain.Sort;

/**
 * 部门管理 数据层
 * 
 * @author ruoyi
 */
public interface SortMapper
{
	/**
     * 查询部门管理数据
     * 
     * @param dept 部门信息
     * @return 部门信息集合
     */
    public List<Sort> selectSortList(Sort sort);
    
    @Select("Select id,table_name,table_id from sys_sort_backup")
    public List<Sort> selectSortList2(Sort sort);
    
    @Select("Select id,table_name,table_id from sys_sort")
    public Sort selectSortVo(String id);
    
    @Delete("DELETE FROM SYS_SORT WHERE id !='1'")
    public boolean delAll();
    
    public List<Sort> selectUnitVo();
    
    public boolean insertSort(Sort sort);
    
    public List<Sort> selectStrutstypeVo(Sort sort);
    
    public List<Sort> selectCarriertypeVo(Sort sort);
    
    public List<Sort> selectExtsortVo(Sort sort);
    
    public List<Map> selectDataVo(String filter);
}
