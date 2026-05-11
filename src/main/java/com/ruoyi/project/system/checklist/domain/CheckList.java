package com.ruoyi.project.system.checklist.domain;

import com.ruoyi.framework.web.domain.BaseEntity;

public class CheckList extends BaseEntity
{
    private static final long serialVersionUID = 1L;

	private Integer serial;
	private String unitid;
	private String oldSysFunction;
	private String oldSysSort;
	private String oldSysDataValues;
	private String oldSysFileValues;
	private String newSysFunction;
	private String newSysSort;
	private String newSysDataValues;
	private String newSysFileValues;


	public String getUnitid() {
		return unitid;
	}

	public void setUnitid(String unitid) {
		this.unitid = unitid;
	}

	public Integer getSerial() {
		return serial;
	}

	public void setSerial(Integer serial) {
		this.serial = serial;
	}

	public String getOldSysFunction() {
		return oldSysFunction;
	}

	public void setOldSysFunction(String oldSysFunction) {
		this.oldSysFunction = oldSysFunction;
	}

	public String getOldSysSort() {
		return oldSysSort;
	}

	public void setOldSysSort(String oldSysSort) {
		this.oldSysSort = oldSysSort;
	}

	public String getOldSysDataValues() {
		return oldSysDataValues;
	}

	public void setOldSysDataValues(String oldSysDataValues) {
		this.oldSysDataValues = oldSysDataValues;
	}

	public String getNewSysFunction() {
		return newSysFunction;
	}

	public void setNewSysFunction(String newSysFunction) {
		this.newSysFunction = newSysFunction;
	}

	public String getNewSysSort() {
		return newSysSort;
	}

	public void setNewSysSort(String newSysSort) {
		this.newSysSort = newSysSort;
	}

	public String getNewSysDataValues() {
		return newSysDataValues;
	}

	public void setNewSysDataValues(String newSysDataValues) {
		this.newSysDataValues = newSysDataValues;
	}

	public String getOldSysFileValues() {
		return oldSysFileValues;
	}

	public void setOldSysFileValues(String oldSysFileValues) {
		this.oldSysFileValues = oldSysFileValues;
	}

	public String getNewSysFileValues() {
		return newSysFileValues;
	}

	public void setNewSysFileValues(String newSysFileValues) {
		this.newSysFileValues = newSysFileValues;
	}

	@Override
	public String toString() {
		return "CheckList{" +
				"serial=" + serial +
				", unitid='" + unitid + '\'' +
				", oldSysFunction='" + oldSysFunction + '\'' +
				", oldSysSort='" + oldSysSort + '\'' +
				", oldSysDataValues='" + oldSysDataValues + '\'' +
				", oldSysFileValues='" + oldSysFileValues + '\'' +
				", newSysFunction='" + newSysFunction + '\'' +
				", newSysSort='" + newSysSort + '\'' +
				", newSysDataValues='" + newSysDataValues + '\'' +
				", newSysFileValues='" + newSysFileValues + '\'' +
				'}';
	}
}
