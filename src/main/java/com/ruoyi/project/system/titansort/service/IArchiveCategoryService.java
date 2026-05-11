package com.ruoyi.project.system.titansort.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.framework.web.domain.Ztree;
import com.ruoyi.project.system.titansort.domain.ArchiveCategory;

public interface IArchiveCategoryService
{
    public List<ArchiveCategory> selectArchiveCategoryTreeList(ArchiveCategory archiveCategoryTree);
    public ArchiveCategory selectArchiveCategoryTreeById(Long categoryId);
    public int insertArchiveCategoryTree(ArchiveCategory archiveCategoryTree);
    public int updateArchiveCategoryTree(ArchiveCategory archiveCategoryTree);
    public int deleteArchiveCategoryTreeByIds(String ids);

    /** 查询树结构并转换为若依前端识别的 Ztree 节点集合 */
    public List<Ztree> selectCategoryTree(ArchiveCategory archiveCategoryTree);

    /** 解析 Word 文档中的分类表并入库 */
    public void importWordCategoryTable(MultipartFile file, String unitId) throws Exception;
}