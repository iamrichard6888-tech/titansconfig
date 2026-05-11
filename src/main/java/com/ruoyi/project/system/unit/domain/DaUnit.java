package com.ruoyi.project.system.unit.domain;

import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 档案全宗单位 对象 t_da_unit
 */
public class DaUnit extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 单位ID */
    private String unitid;

    /** 单位代码 */
    @Excel(name = "单位代码")
    private String code;

    /** 单位名称 */
    @Excel(name = "单位名称")
    private String name;

    /** 描述 */
    @Excel(name = "描述")
    private String description;

    /** 全宗号 */
    @Excel(name = "全宗号")
    private String qzh;

    /** 单位编码 */
    private String unitcode;

    /** 单位全称 */
    @Excel(name = "单位全称")
    private String unitfullname;

    /** 是否叶子节点 */
    private String isleaf;

    /** 单位类型 */
    private String unittype;

    /** 档案馆代码 */
    private String dagcode;

    /** IP网段 */
    private String ipsection;

    /** 是否限制 */
    private String isrestrict;

    /** 业务单位名称 */
    private String businessunitname;

    /** * 显示排序号 (新增字段)
     */
    @Excel(name = "排序号")
    private Integer levelord;

    // ----- 请在底部追加 Getter 和 Setter -----

    public Integer getLevelord() {
        return levelord;
    }

    public void setLevelord(Integer levelord) {
        this.levelord = levelord;
    }

    // ----- Getters 和 Setters -----

    public String getUnitid() { return unitid; }
    public void setUnitid(String unitid) { this.unitid = unitid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getQzh() { return qzh; }
    public void setQzh(String qzh) { this.qzh = qzh; }

    public String getUnitcode() { return unitcode; }
    public void setUnitcode(String unitcode) { this.unitcode = unitcode; }

    public String getUnitfullname() { return unitfullname; }
    public void setUnitfullname(String unitfullname) { this.unitfullname = unitfullname; }

    public String getIsleaf() { return isleaf; }
    public void setIsleaf(String isleaf) { this.isleaf = isleaf; }

    public String getUnittype() { return unittype; }
    public void setUnittype(String unittype) { this.unittype = unittype; }

    public String getDagcode() { return dagcode; }
    public void setDagcode(String dagcode) { this.dagcode = dagcode; }

    public String getIpsection() { return ipsection; }
    public void setIpsection(String ipsection) { this.ipsection = ipsection; }

    public String getIsrestrict() { return isrestrict; }
    public void setIsrestrict(String isrestrict) { this.isrestrict = isrestrict; }

    public String getBusinessunitname() { return businessunitname; }
    public void setBusinessunitname(String businessunitname) { this.businessunitname = businessunitname; }
}