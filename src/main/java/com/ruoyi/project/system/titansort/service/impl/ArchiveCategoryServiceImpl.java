package com.ruoyi.project.system.titansort.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.utils.text.Convert;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.framework.web.domain.Ztree;
import com.ruoyi.project.system.titansort.domain.ArchiveCategory;
import com.ruoyi.project.system.titansort.mapper.ArchiveCategoryMapper;
import com.ruoyi.project.system.titansort.service.IArchiveCategoryService;

@Service
public class ArchiveCategoryServiceImpl implements IArchiveCategoryService
{
    @Autowired
    private ArchiveCategoryMapper archiveCategoryTreeMapper;

    @Override
    public List<ArchiveCategory> selectArchiveCategoryTreeList(ArchiveCategory archiveCategoryTree) {
        return archiveCategoryTreeMapper.selectArchiveCategoryTreeList(archiveCategoryTree);
    }

    @Override
    public ArchiveCategory selectArchiveCategoryTreeById(Long categoryId) {
        return archiveCategoryTreeMapper.selectArchiveCategoryTreeById(categoryId);
    }

    @Override
    public int insertArchiveCategoryTree(ArchiveCategory archiveCategoryTree) {
        archiveCategoryTree.setCreateBy(ShiroUtils.getLoginName());
        archiveCategoryTree.setCreateTime(DateUtils.getNowDate());
        return archiveCategoryTreeMapper.insertArchiveCategoryTree(archiveCategoryTree);
    }

    @Override
    public int updateArchiveCategoryTree(ArchiveCategory archiveCategoryTree) {
        archiveCategoryTree.setUpdateBy(ShiroUtils.getLoginName());
        archiveCategoryTree.setUpdateTime(DateUtils.getNowDate());
        return archiveCategoryTreeMapper.updateArchiveCategoryTree(archiveCategoryTree);
    }

    @Override
    public int deleteArchiveCategoryTreeByIds(String ids) {
        return archiveCategoryTreeMapper.deleteArchiveCategoryTreeByIds(Convert.toStrArray(ids));
    }

    /**
     * 转换为前端识别的 Ztree 结构
     */
    @Override
    public List<Ztree> selectCategoryTree(ArchiveCategory archiveCategoryTree) {
        List<ArchiveCategory> list = archiveCategoryTreeMapper.selectArchiveCategoryTreeList(archiveCategoryTree);
        List<Ztree> ztrees = new ArrayList<>();
        for (ArchiveCategory category : list) {
            Ztree ztree = new Ztree();
            ztree.setId(category.getCategoryId());
            ztree.setpId(category.getParentId());
            ztree.setName(category.getCategoryName() + " (" + category.getCategoryCode() + ")");
            ztree.setTitle(category.getCategoryName());
            ztrees.add(ztree);
        }
        return ztrees;
    }

    /**
     * 解析 Word 文档中的《档案分类表》并入库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importWordCategoryTable(MultipartFile file, String unitId) throws Exception {
        try (InputStream is = file.getInputStream(); XWPFDocument doc = new XWPFDocument(is)) {
            List<XWPFTable> tables = doc.getTables();
            XWPFTable targetTable = null;

            // 1. 寻找带有 "档案分类表" 特征的表格（通常第一列是序号，含有“类目代码”）
            for (XWPFTable table : tables) {
                String fullText = table.getText();
                if (fullText.contains("类目代码") || fullText.contains("门类代码")) {
                    targetTable = table;
                    break;
                }
            }
            if (targetTable == null) throw new Exception("未能找到包含《档案分类表》的结构化表格");

            List<XWPFTableRow> rows = targetTable.getRows();

            // 状态游标：记忆每一层的当前活动 ID
            Long currentL1Id = 0L;
            Long currentL2Id = 0L;

            int sortCounter = 1;
            // 跳过表头（通常第1行是标题）
            for (int i = 1; i < rows.size(); i++) {
                XWPFTableRow row = rows.get(i);
                List<XWPFTableCell> cells = row.getTableCells();
                if (cells.size() < 3) continue;

                String orderNum = cells.get(0).getText().trim();
                String code = cells.get(1).getText().trim();
                // 智能寻找名称（如果一级类目名称在第2列，三级在第4列等）
                String nameL1 = cells.size() > 2 ? cells.get(2).getText().trim() : "";
                String nameL2 = cells.size() > 3 ? cells.get(3).getText().trim() : "";
                String nameL3 = cells.size() > 4 ? cells.get(4).getText().trim() : "";

                // 过滤页码和重复表头噪音
                if (StringUtils.isBlank(orderNum) || orderNum.contains("序号")) continue;

                ArchiveCategory node = new ArchiveCategory();
                node.setUnitId(unitId);
                node.setOrderNum(orderNum);
                node.setCategoryCode(code);
                node.setSortOrder(sortCounter++);
                node.setStatus("0");
                node.setCreateBy(ShiroUtils.getLoginName());
                node.setCreateTime(DateUtils.getNowDate());

                // 规则判定：如果是纯数字（如 1, 2, 3），说明是一级门类
                if (!orderNum.contains(".")) {
                    node.setCategoryName(nameL1);
                    node.setCategoryLevel(1);
                    node.setParentId(0L);
                    node.setAncestors("0");

                    archiveCategoryTreeMapper.insertArchiveCategoryTree(node);
                    currentL1Id = node.getCategoryId(); // 更新一级游标
                    currentL2Id = 0L; // 清空二级游标
                }
                // 规则判定：如果包含1个小数点（如 3.1），说明是二级类目
                else if (orderNum.indexOf(".") == orderNum.lastIndexOf(".")) {
                    node.setCategoryName(nameL2);
                    node.setCategoryLevel(2);
                    node.setParentId(currentL1Id);
                    node.setAncestors("0," + currentL1Id);

                    archiveCategoryTreeMapper.insertArchiveCategoryTree(node);
                    currentL2Id = node.getCategoryId(); // 更新二级游标
                }
                // 规则判定：包含2个以上小数点（如 3.1.1），说明是三级类目
                else {
                    node.setCategoryName(nameL3);
                    node.setCategoryLevel(3);
                    node.setParentId(currentL2Id);
                    node.setAncestors("0," + currentL1Id + "," + currentL2Id);

                    archiveCategoryTreeMapper.insertArchiveCategoryTree(node);
                }
            }
        }
    }
}