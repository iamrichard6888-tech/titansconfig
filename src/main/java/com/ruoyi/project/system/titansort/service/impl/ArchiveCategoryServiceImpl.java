package com.ruoyi.project.system.titansort.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ruoyi.common.utils.text.Convert;
import com.ruoyi.framework.web.domain.Ztree;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.project.system.titansort.domain.ArchiveCategory;
import com.ruoyi.project.system.titansort.mapper.ArchiveCategoryMapper;
import com.ruoyi.project.system.titansort.service.IArchiveCategoryService;

@Service
public class ArchiveCategoryServiceImpl implements IArchiveCategoryService {

    @Autowired
    private ArchiveCategoryMapper archiveCategoryMapper;

    @Override
    public List<ArchiveCategory> selectArchiveCategoryTreeList(ArchiveCategory archiveCategoryTree) {
        return archiveCategoryMapper.selectArchiveCategoryTreeList(archiveCategoryTree);
    }

    @Override
    public ArchiveCategory selectArchiveCategoryTreeById(Long categoryId) {
        return archiveCategoryMapper.selectArchiveCategoryTreeById(categoryId);
    }

    @Override
    public int insertArchiveCategoryTree(ArchiveCategory archiveCategoryTree) {
        archiveCategoryTree.setCreateBy(ShiroUtils.getLoginName());
        archiveCategoryTree.setCreateTime(DateUtils.getNowDate());
        return archiveCategoryMapper.insertArchiveCategoryTree(archiveCategoryTree);
    }

    @Override
    public int updateArchiveCategoryTree(ArchiveCategory archiveCategoryTree) {
        archiveCategoryTree.setUpdateBy(ShiroUtils.getLoginName());
        archiveCategoryTree.setUpdateTime(DateUtils.getNowDate());
        return archiveCategoryMapper.updateArchiveCategoryTree(archiveCategoryTree);
    }

    @Override
    public int deleteArchiveCategoryTreeByIds(String ids) {
        return archiveCategoryMapper.deleteArchiveCategoryTreeByIds(Convert.toStrArray(ids));
    }

    /**
     * 转换为前端识别的 Ztree 结构
     */
    @Override
    public List<Ztree> selectCategoryTree(ArchiveCategory archiveCategoryTree) {
        List<ArchiveCategory> list = archiveCategoryMapper.selectArchiveCategoryTreeList(archiveCategoryTree);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importWordCategoryTable(MultipartFile file, String unitId) throws Exception {
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            throw new Exception("导入失败：文件名为空！");
        }

        System.out.println("--- 🚀 开始执行红头文件智能双引擎扫描 (" + fileName + ") ---");

        // =================================================================
        // 🔀 核心路由：根据文件类型分发到不同引擎
        // =================================================================
        if (fileName.toLowerCase().endsWith(".doc")) {
            // 激活旧版二进制引擎 (HWPF)
            parseDocFormat(file.getInputStream(), unitId);
        } else if (fileName.toLowerCase().endsWith(".docx")) {
            // 激活新版 XML 引擎 (XWPF)
            parseDocxFormat(file.getInputStream(), unitId);
        } else {
            throw new Exception("导入失败：仅支持 .doc 或 .docx 格式的 Word 文档！");
        }
    }

    /**
     * 🟢 引擎 A：专门解析老式 .doc (OLE2 二进制格式)
     */
    private void parseDocFormat(InputStream is, String unitId) throws Exception {
        HWPFDocument doc = new HWPFDocument(is);
        Range range = doc.getRange(); // 获取文档全部内容游标

        Long currentL1Id = 0L;
        Long currentL2Id = 0L;
        String currentL1Ancestors = "0";
        String currentL2Ancestors = "0";

        boolean isInsideTargetInterval = false;
        boolean hasProcessedAnyRow = false;
        Table targetTable = null;

        // 1. 扫描段落，寻找开关锁及挂载的目标表格
        for (int i = 0; i < range.numParagraphs(); i++) {
            Paragraph para = range.getParagraph(i);
            String paraText = para.text().replaceAll("[\\s\\u00A0\\u3000（）()<>《》]", "");

            // 开锁
            if (!isInsideTargetInterval && paraText.contains("档案分类表")) {
                isInsideTargetInterval = true;
                System.out.println("🔓 [HWPF引擎] 捕获开始特征，采集锁【已开启】!");
                continue;
            }
            // 关锁
            if (isInsideTargetInterval && (paraText.contains("各门类文件材料归档范围") || paraText.contains("保管期限表"))) {
                if (hasProcessedAnyRow) {
                    System.out.println("🔒 [HWPF引擎] 捕获结束特征，安全拉闸退出。");
                    break;
                }
            }

            // 如果当前游标处于开锁状态，且遇到表格，提取它
            if (isInsideTargetInterval && para.isInTable()) {
                targetTable = range.getTable(para);
                break; // 锁定目标表格，直接跳出寻找
            }
        }

        if (targetTable == null) {
            throw new Exception("智能导入失败：未能在 .doc 文档的目标区间内捕获到《档案分类表》！");
        }

        System.out.println("🎯 [HWPF引擎] 成功锁定目标表格，共 " + targetTable.numRows() + " 行，正在深度提取...");

        // 2. 解析 .doc 的表格数据
        for (int r = 0; r < targetTable.numRows(); r++) {
            TableRow row = targetTable.getRow(r);
            if (row.numCells() == 0) continue;

            String rawCol0 = row.getCell(0).text().trim();
            // 清洗二进制文本末尾自带的特殊控制符 (如 \u0007)
            rawCol0 = cleanDocText(rawCol0);

            if (StringUtils.isBlank(rawCol0) || rawCol0.contains("序号") || rawCol0.contains("门类")) {
                continue;
            }

            // 调试天眼输出
            StringBuilder rowDebugInfo = new StringBuilder("[HWPF天眼] 内容: ");
            for (int c = 0; c < row.numCells(); c++) {
                rowDebugInfo.append("列").append(c).append("='").append(cleanDocText(row.getCell(c).text())).append("' | ");
            }
            System.out.println(rowDebugInfo.toString());

            String orderNum = rawCol0;
            String categoryCode = "";
            String categoryName = "";

            // 贪婪提取代码 (寻找大写英文字母串)
            for (int c = 1; c < Math.min(4, row.numCells()); c++) {
                String txt = cleanDocText(row.getCell(c).text());
                if (txt.matches("^[A-Za-z\\u00B7]+$")) {
                    categoryCode = txt;
                    break;
                }
            }

            // 贪婪提取名字 (寻找中文列)
            for (int c = 1; c < row.numCells(); c++) {
                String txt = cleanDocText(row.getCell(c).text());
                if (txt.equals(categoryCode) || txt.length() < 2) continue;
                if (txt.matches(".*[\\u4e00-\\u9fa5]+.*")) {
                    categoryName = txt;
                    break;
                }
            }

            if (StringUtils.isBlank(categoryName)) continue;

            // 层级推断
            int level = 1;
            ArchiveCategory node = new ArchiveCategory();
            node.setUnitId(unitId);
            node.setStatus("0");
            node.setCreateBy(ShiroUtils.getLoginName());
            node.setCreateTime(DateUtils.getNowDate());

            if (!orderNum.contains(".")) {
                level = 1;
                node.setParentId(0L);
                node.setAncestors("0");
            } else if (orderNum.indexOf(".") == orderNum.lastIndexOf(".")) {
                level = 2;
                node.setParentId(currentL1Id);
                node.setAncestors(currentL1Ancestors);
            } else {
                level = 3;
                node.setParentId(currentL2Id);
                node.setAncestors(currentL2Ancestors);
            }

            node.setOrderNum(orderNum);
            node.setCategoryCode(StringUtils.isNotBlank(categoryCode) ? categoryCode : "DEFAULT");
            node.setCategoryName(categoryName);
            node.setCategoryLevel(level);

            archiveCategoryMapper.insertArchiveCategoryTree(node);
            hasProcessedAnyRow = true;

            System.out.println("✅ [HWPF入库] -> 序号:" + orderNum + " | 代码:" + node.getCategoryCode() + " | 名称:" + categoryName + " | 深度:" + level);

            Long generatedId = node.getCategoryId();
            if (level == 1) {
                currentL1Id = generatedId;
                currentL1Ancestors = "0," + generatedId;
            } else if (level == 2) {
                currentL2Id = generatedId;
                currentL2Ancestors = currentL1Ancestors + "," + generatedId;
            }
        }

        if (!hasProcessedAnyRow) {
            throw new Exception("智能导入失败：.doc 文档解析完毕，但未提取到有效数据行！");
        }
    }

    /**
     * 🔵 引擎 B：专门解析新式 .docx (OOXML 格式) - 完全保持上一轮完美逻辑
     */
    private void parseDocxFormat(InputStream is, String unitId) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(is)) {
            Long currentL1Id = 0L;
            Long currentL2Id = 0L;
            String currentL1Ancestors = "0";
            String currentL2Ancestors = "0";

            boolean isInsideTargetInterval = false;
            boolean hasProcessedAnyRow = false;

            for (IBodyElement element : doc.getBodyElements()) {
                String cleanElementText = "";

                if (element.getElementType() == BodyElementType.PARAGRAPH) {
                    cleanElementText = ((XWPFParagraph) element).getText();
                } else if (element.getElementType() == BodyElementType.TABLE) {
                    cleanElementText = ((XWPFTable) element).getText();
                }

                String normalizedText = cleanElementText.replaceAll("[\\s\\u00A0\\u3000（）()<>《》]", "");

                if (!isInsideTargetInterval && normalizedText.contains("档案分类表")) {
                    isInsideTargetInterval = true;
                    System.out.println("🔓 [XWPF引擎] 成功捕获开始特征，采集锁【已开启】!");
                    if (element.getElementType() == BodyElementType.PARAGRAPH) continue;
                }

                if (isInsideTargetInterval && (normalizedText.contains("各门类文件材料归档范围") || normalizedText.contains("保管期限表"))) {
                    if (hasProcessedAnyRow) {
                        System.out.println("🔒 [XWPF引擎] 捕获结束特征，安全退出扫描。");
                        break;
                    }
                }

                if (element.getElementType() == BodyElementType.TABLE && isInsideTargetInterval) {
                    XWPFTable table = (XWPFTable) element;
                    System.out.println("🎯 [XWPF引擎] 锁定目标表格区块，正在深度提取...");

                    for (XWPFTableRow row : table.getRows()) {
                        List<XWPFTableCell> cells = row.getTableCells();
                        if (cells.isEmpty()) continue;

                        String rawCol0 = cells.get(0).getText().trim();
                        if (StringUtils.isBlank(rawCol0) || rawCol0.contains("序号") || rawCol0.contains("类目")) {
                            continue;
                        }

                        StringBuilder rowDebugInfo = new StringBuilder("[XWPF天眼] 内容: ");
                        for (int c = 0; c < cells.size(); c++) {
                            rowDebugInfo.append("列").append(c).append("='").append(cells.get(c).getText().trim()).append("' | ");
                        }
                        System.out.println(rowDebugInfo.toString());

                        String orderNum = rawCol0;
                        String categoryCode = "";
                        String categoryName = "";

                        for (int c = 1; c < Math.min(4, cells.size()); c++) {
                            String txt = cells.get(c).getText().trim();
                            if (txt.matches("^[A-Za-z\\u00B7]+$")) {
                                categoryCode = txt;
                                break;
                            }
                        }

                        for (int c = 1; c < cells.size(); c++) {
                            String txt = cells.get(c).getText().trim();
                            if (txt.equals(categoryCode) || txt.length() < 2) continue;
                            if (txt.matches(".*[\\u4e00-\\u9fa5]+.*")) {
                                categoryName = txt;
                                break;
                            }
                        }

                        if (StringUtils.isBlank(categoryName) && cells.size() > 1) {
                            categoryName = cells.get(cells.size() - 1).getText().trim();
                        }

                        if (StringUtils.isBlank(categoryName) || categoryName.contains("代码")) {
                            continue;
                        }

                        int level = 1;
                        ArchiveCategory node = new ArchiveCategory();
                        node.setUnitId(unitId);
                        node.setStatus("0");
                        node.setCreateBy(ShiroUtils.getLoginName());
                        node.setCreateTime(DateUtils.getNowDate());

                        if (!orderNum.contains(".")) {
                            level = 1;
                            node.setParentId(0L);
                            node.setAncestors("0");
                        } else if (orderNum.indexOf(".") == orderNum.lastIndexOf(".")) {
                            level = 2;
                            node.setParentId(currentL1Id);
                            node.setAncestors(currentL1Ancestors);
                        } else {
                            level = 3;
                            node.setParentId(currentL2Id);
                            node.setAncestors(currentL2Ancestors);
                        }

                        node.setOrderNum(orderNum);
                        node.setCategoryCode(StringUtils.isNotBlank(categoryCode) ? categoryCode : "DEFAULT");
                        node.setCategoryName(categoryName);
                        node.setCategoryLevel(level);

                        archiveCategoryMapper.insertArchiveCategoryTree(node);
                        hasProcessedAnyRow = true;

                        System.out.println("✅ [XWPF入库] -> 序号:" + orderNum + " | 代码:" + node.getCategoryCode() + " | 名称:" + categoryName + " | 深度:" + level);

                        Long generatedId = node.getCategoryId();
                        if (level == 1) {
                            currentL1Id = generatedId;
                            currentL1Ancestors = "0," + generatedId;
                        } else if (level == 2) {
                            currentL2Id = generatedId;
                            currentL2Ancestors = currentL1Ancestors + "," + generatedId;
                        }
                    }
                }
            }

            if (!hasProcessedAnyRow) {
                throw new Exception("智能导入失败：.docx 文档解析完毕，但未提取到有效数据行！");
            }
        }
    }

    /**
     * 🧹 降噪工具：专门清洗老式 .doc 文本末尾自带的响铃符 (\u0007) 及杂质
     */
    private String cleanDocText(String text) {
        if (text == null) return "";
        // .doc 单元格的文字末尾通常会被 POI 读出 ASCII 控制字符 7 (BEL)，必须砍掉
        return text.replaceAll("[\\u0007\\u0001]", "").trim();
    }
}