package com.ruoyi.project.system.checklist.CheckListHandler;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

public class MergeSameColumnHandler implements CellWriteHandler {
    private int mergeRowIndex; // 从哪一行开始合并 (索引从0开始)
    private int[] mergeColumnIndex; // 需要合并哪些列

    public MergeSameColumnHandler(int mergeRowIndex, int[] mergeColumnIndex) {
        this.mergeRowIndex = mergeRowIndex;
        this.mergeColumnIndex = mergeColumnIndex;
    }

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<WriteCellData<?>> cellDataList, Cell cell, Head head,
                                 Integer relativeRowIndex, Boolean isHead) {
        int curRowIndex = cell.getRowIndex();
        int curColIndex = cell.getColumnIndex();

        // 只有大于指定的数据起始行才进行合并逻辑判断
        if (curRowIndex > mergeRowIndex) {
            for (int columnIndex : mergeColumnIndex) {
                if (curColIndex == columnIndex) {
                    mergeWithPrevRow(writeSheetHolder, cell, curRowIndex, curColIndex);
                    break;
                }
            }
        }
    }

    private void mergeWithPrevRow(WriteSheetHolder writeSheetHolder, Cell cell, int curRowIndex, int curColIndex) {
        Object curData = getCellValue(cell);

        // 防御机制1：如果当前单元格为空，绝对不参与纵向合并（防止与表尾空白处合并报错）
        if (curData == null || curData.toString().trim().isEmpty()) {
            return;
        }

        Row preRow = cell.getSheet().getRow(curRowIndex - 1);
        if (preRow == null) return;
        Cell preCell = preRow.getCell(curColIndex);
        if (preCell == null) return;

        Object preData = getCellValue(preCell);

        // 如果当前数据和上一行相同，触发合并
        if (curData.equals(preData)) {
            Sheet sheet = writeSheetHolder.getSheet();
            List<CellRangeAddress> mergeRegions = sheet.getMergedRegions();

            // 防御机制2：检查当前单元格或上一行单元格是否已经属于其他“跨列（横向）合并”区域（比如底部的合计栏）
            for (CellRangeAddress range : mergeRegions) {
                if (range.isInRange(curRowIndex, curColIndex) && range.getFirstColumn() != range.getLastColumn()) {
                    return; // 已经被横向合并，放弃操作
                }
                if (range.isInRange(curRowIndex - 1, curColIndex) && range.getFirstColumn() != range.getLastColumn()) {
                    return; // 上一行被横向合并，放弃操作
                }
            }

            boolean isMerged = false;
            for (int i = 0; i < mergeRegions.size(); i++) {
                CellRangeAddress cellRangeAddr = mergeRegions.get(i);
                // 如果前一行已经被垂直合并过，我们就拉长这个垂直合并区域
                if (cellRangeAddr.isInRange(curRowIndex - 1, curColIndex)
                        && cellRangeAddr.getFirstColumn() == curColIndex
                        && cellRangeAddr.getLastColumn() == curColIndex) {

                    sheet.removeMergedRegion(i);
                    cellRangeAddr.setLastRow(curRowIndex);
                    sheet.addMergedRegion(cellRangeAddr);
                    isMerged = true;
                    break;
                }
            }
            // 如果前一行没有被合并过，就新建一个合并区域 (把上一行和当前行合起来)
            if (!isMerged) {
                CellRangeAddress cellRangeAddress = new CellRangeAddress(curRowIndex - 1, curRowIndex, curColIndex, curColIndex);
                sheet.addMergedRegion(cellRangeAddress);
            }
        }
    }

    // 提取获取单元格值的通用方法
    private Object getCellValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        return null;
    }
}