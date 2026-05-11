package com.ruoyi.project.system.titansort.mapper;

import java.util.List;
import com.ruoyi.project.system.titansort.domain.ArchiveCategory;

/**
 * 档案分类架构树 Mapper接口
 */
public interface ArchiveCategoryMapper
{
    /** 查询档案分类架构树列表 */
    public List<ArchiveCategory> selectArchiveCategoryTreeList(ArchiveCategory archiveCategoryTree);

    /** 通过ID查询分类 */
    public ArchiveCategory selectArchiveCategoryTreeById(Long categoryId);

    /** 新增档案分类 */
    public int insertArchiveCategoryTree(ArchiveCategory archiveCategoryTree);

    /** 修改档案分类 */
    public int updateArchiveCategoryTree(ArchiveCategory archiveCategoryTree);

    /** 批量删除档案分类 */
    public int deleteArchiveCategoryTreeByIds(String[] categoryIds);
}