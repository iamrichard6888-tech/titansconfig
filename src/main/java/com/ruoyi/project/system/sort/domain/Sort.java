package com.ruoyi.project.system.sort.domain;

import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 部门表 sys_sort
 * 
 * @author ruoyi
 */
public class Sort extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 部门ID */
    private String id;

    /** 父部门ID */
    private String parentId;

    /** 门类名称 */
    private String groupName;
    
    /** 代码 */
    private String code;

    /** 显示顺序 */
    private Integer showOrder;

    /** 显示顺序 */
    private Integer level;

    /** 表名 */
    private String tableName;
    
    /** 表名 */
    private String tableId;
    
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getShowOrder() {
		return showOrder;
	}

	public void setShowOrder(Integer showOrder) {
		this.showOrder = showOrder;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	
}
