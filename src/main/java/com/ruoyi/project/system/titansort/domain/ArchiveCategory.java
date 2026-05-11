package com.ruoyi.project.system.titansort.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 档案分类架构树对象 archive_category_tree
 * * @author ruoyi
 */
public class ArchiveCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分类类目主键ID */
    private Long categoryId;

    /** 所属全宗号/单位ID */
    private String unitId;

    /** 父类目ID (0表示最顶层一级门类) */
    private Long parentId;

    /** 祖级路径列表 */
    private String ancestors;

    /** 层级深度: 1-一级门类, 2-二级类目, 3-三级类目 */
    private Integer categoryLevel;

    /** 原表序号 */
    private String orderNum;

    /** 类目代码 */
    private String categoryCode;

    /** 类目名称 */
    private String categoryName;

    /** 同级展示排序号 */
    private Integer sortOrder;

    /** 状态: 0-正常启用, 1-停用 */
    private String status;

    // ==================== Getters 和 Setters ====================

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getUnitId() { return unitId; }
    public void setUnitId(String unitId) { this.unitId = unitId; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getAncestors() { return ancestors; }
    public void setAncestors(String ancestors) { this.ancestors = ancestors; }

    public Integer getCategoryLevel() { return categoryLevel; }
    public void setCategoryLevel(Integer categoryLevel) { this.categoryLevel = categoryLevel; }

    public String getOrderNum() { return orderNum; }
    public void setOrderNum(String orderNum) { this.orderNum = orderNum; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("categoryId", getCategoryId())
                .append("unitId", getUnitId())
                .append("parentId", getParentId())
                .append("ancestors", getAncestors())
                .append("categoryLevel", getCategoryLevel())
                .append("orderNum", getOrderNum())
                .append("categoryCode", getCategoryCode())
                .append("categoryName", getCategoryName())
                .append("sortOrder", getSortOrder())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}