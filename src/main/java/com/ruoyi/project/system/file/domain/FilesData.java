package com.ruoyi.project.system.file.domain;

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
public class FilesData extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 部门ID */
    private String id;

    /** 父部门ID */
    private String qzcode;
    
    private String archiveCode;

    /** 门类名称 */
    private String title;
    
    /** 门类名称 */
    private String deptId;//extsort的id
    
    /** 门类名称 */
    private String parentId;
    
    /** 电子文件id */
    private String dzwj;
    
    private String isDelete;
    
    private String carriertype;
    private String strutstype;
    private String unitid;
    private String sortid;
    private String iscopy;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getQzcode() {
		return qzcode;
	}

	public void setQzcode(String qzcode) {
		this.qzcode = qzcode;
	}

	public String getArchiveCode() {
		return archiveCode;
	}

	public void setArchiveCode(String archiveCode) {
		this.archiveCode = archiveCode;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDeptId() {
		return deptId;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getDzwj() {
		return dzwj;
	}

	public void setDzwj(String dzwj) {
		this.dzwj = dzwj;
	}

	public String getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(String isDelete) {
		this.isDelete = isDelete;
	}

	public String getCarriertype() {
		return carriertype;
	}

	public void setCarriertype(String carriertype) {
		this.carriertype = carriertype;
	}

	public String getStrutstype() {
		return strutstype;
	}

	public void setStrutstype(String strutstype) {
		this.strutstype = strutstype;
	}

	public String getUnitid() {
		return unitid;
	}

	public void setUnitid(String unitid) {
		this.unitid = unitid;
	}

	public String getSortid() {
		return sortid;
	}

	public void setSortid(String sortid) {
		this.sortid = sortid;
	}

	public String getIscopy() {
		return iscopy;
	}

	public void setIscopy(String iscopy) {
		this.iscopy = iscopy;
	}
    
}
