package com.xiaojin.estool.util.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
/**
 * @Auther dingjian
 * @Date 2021-01-04 8:49 下午
 */



public class ExcelWriteUtil
{
    private Workbook wb = null;

    private Sheet sheet = null;

    private InputStream is = null;

    public ExcelWriteUtil() throws Exception
    {
        // 创建Excel的工作簿 Workbook,对应到一个excel文档
        InputStream is = ExcelWriteUtil.class.getResourceAsStream("template.xlsx");
        init(is, false);
    }

    public ExcelWriteUtil(InputStream is) throws Exception
    {
        init(is, false);
    }

    public ExcelWriteUtil(InputStream is, boolean batchWrite) throws Exception
    {
        init(is, batchWrite);
    }

    private void init(InputStream is, boolean batchWrite) throws Exception
    {
        this.is = is;
        wb = create(is, batchWrite);
        sheet = wb.getSheetAt(0); // 默认使用第一个sheet页
        createCellStyle();
    }

    /**
     * 根据输入流，创建Workbook对象
     */
    private Workbook create(InputStream is, boolean batchWrite) throws Exception
    {
        try
        {
            if (!is.markSupported())
            {
                is = new PushbackInputStream(is, 8);
            }

            if (POIFSFileSystem.hasPOIFSHeader(is))
            {
                return new HSSFWorkbook(is);
            }
            if (POIXMLDocument.hasOOXMLHeader(is))
            {
                if (batchWrite)
                {
                    // 大数据用的工作薄，内存中只保存1000条数据，多的数据写入磁盘临时文件
                    return new SXSSFWorkbook(new XSSFWorkbook(is), 1000);
                }
                else
                {
                    return new XSSFWorkbook(OPCPackage.open(is));
                }
            }
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
        throw new IllegalArgumentException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
    }

    /**
     * 设置单元格宽（单位：1个字符宽度）
     */
    public void setColumnWidth(int[] widthArray)
    {
        for (int i = 0; i < widthArray.length; i++)
        {
            sheet.setColumnWidth(i, widthArray[i] * 256);
        }
    }

    /**
     * 写入一行数据
     * @param dataArray 数据内容，每个数据为一列
     * @param startRowNum 数据写入行（第一行开始于0）
     */
    public void writeExcelData(Object[] dataArray, int startRowNum)
    {
        Row rowContent = getRow(sheet, startRowNum);
        writeRow(rowContent, dataArray);
    }

    /**
     * 写入一行数据
     * @param dataArray 数据内容，每个数据为一列
     * @param startRowNum 数据写入行（第一行开始于0）
     */
    public void writeExcelData(List<Object> dataArray, int startRowNum)
    {
        Row rowContent = getRow(sheet, startRowNum);
        writeRow(rowContent, dataArray);
    }

    /**
     * 写入多行数据
     * @param dataList 数据内容集合，每个数据为一行
     * @param startRowNum 数据写入行（第一行开始于0）
     */
    public void writeExcelDataList(List<Object[]> dataList, int startRowNum)
    {
        for (int i = 0; i < dataList.size(); i++)
        {
            Row rowContent = getRow(sheet, i + startRowNum);
            writeRow(rowContent, dataList.get(i));
        }
    }

    /**
     * 写入多行数据
     * @param dataList 数据内容集合，每个数据为一行
     * @param dataMapper 将对象的内容转换为字符串数组
     * @param startRowNum 数据写入行（第一行开始于0）
     */
    public void writeExcelDataList(List dataList, ExcelDataMapper dataMapper, int startRowNum)
    {
        for (int i = 0; i < dataList.size(); i++)
        {
            Object[] dataRow = dataMapper.dataMapper(dataList.get(i), i);
            Row rowContent = getRow(sheet, i + startRowNum);
            writeRow(rowContent, dataRow);
        }
    }

    private void writeRow(Row row, Object[] dataRow)
    {
        for (int i = 0; i < dataRow.length; i++)
        {
            Cell cell = getCell(row, i);
            Object val = dataRow[i];
            writeCellValue(cell, val);
        }
    }

    private void writeRow(Row row, List<Object> dataRow)
    {
        for (int i = 0; i < dataRow.size(); i++)
        {
            Cell cell = getCell(row, i);
            Object val = dataRow.get(i);
            writeCellValue(cell, val);
        }
    }

    /**
     * Excel单元格写入值
     * @param rowNum 单元格行数（第一行开始于0）
     * @param columnNum 单元格列数（第一列开始于0）
     * @param val 待写入的值
     */
    public void writeCellValue(int rowNum, int columnNum, Object val)
    {
        Row row = getRow(sheet, rowNum);
        Cell cell = getCell(row, columnNum);
        writeCellValue(cell, val);
    }

    private void writeCellValue(Cell cell, Object val)
    {
        if (val instanceof Number)
        {
            cell.setCellValue(((Number) val).doubleValue());
        }
        else if (val instanceof Boolean)
        {
            cell.setCellValue((Boolean) val);
        }
        else
        {
            String value = val == null ? "" : val.toString();
            cell.setCellValue(value);
        }
    }

    private Row getRow(Sheet sheet, int rownum)
    {
        Row row = sheet.getRow(rownum);
        if (row == null)
        {
            row = sheet.createRow(rownum);
        }
        return row;
    }

    private Cell getCell(Row row, int cellNum)
    {
        Cell cell = row.getCell(cellNum);
        if (cell == null)
        {
            cell = row.createCell(cellNum);
        }
        // 设置单元格样式
        setExcelCellStyle(cell);
        return cell;
    }

    /**
     * 单元格合并
     */
    public void cellRegion(int firstRow, int lastRow, int firstCol, int lastCol)
    {
        // 合并日期占两行(4个参数，分别为起始行，结束行，起始列，结束列)
        // 行和列都是从0开始计数，且起始结束都会合并
        CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
        sheet.addMergedRegion(region);
    }

    /**
     * 新增行
     * @param startRow 新增行的起始行数
     * @param endCol 新增行需要的列数
     * @param n 需要新增的行数
     */
    public void shiftRows(int startRow, int endCol, int n)
    {
        // 新增n行
        sheet.shiftRows(startRow, sheet.getLastRowNum(), n, true, false);
        // 读取上一行的单元格样式
        Row srcRow = getRow(sheet, startRow - 1);
        List<CellStyle> srcCellStyleList = new ArrayList();
        for (int j = 0; j < endCol; j++)
        {
            srcCellStyleList.add(getCell(srcRow, j).getCellStyle());
        }
        // 将原样式复制给新增的行
        for (int i = 0; i < n; i++)
        {
            Row dstRow = getRow(sheet, startRow + i);
            for (int j = 0; j < endCol; j++)
            {
                getCell(dstRow, j).setCellStyle(srcCellStyleList.get(j));
            }
        }
    }

    /**
     * 将Excel文件写入到输出流
     */
    public void write(OutputStream os) throws Exception
    {
        wb.write(os);
    }

    /**
     * 将Excel文件写入到文件
     */
    public void write(File outPutFile) throws Exception
    {
        if (outPutFile.exists())
        {
            outPutFile.delete();
        }
        if (!outPutFile.getParentFile().exists())
        {
            outPutFile.getParentFile().mkdirs();
        }
        FileOutputStream os = new FileOutputStream(outPutFile);
        wb.write(os);
        IOUtils.closeQuietly(os);
    }

    public void changeSheet(int index)
    {
        sheet = wb.getSheetAt(index);
    }

    public void changeSheet(String sheetName)
    {
        sheet = wb.getSheet(sheetName);
    }

    public void createSheet(String sheetName)
    {
        sheet = wb.createSheet(sheetName);
    }

    public void close()
    {
        IOUtils.closeQuietly(this.is);
    }

    private CellStyle userDefinedCellStyle = null;

    private CellStyle titleCellStyle = null;

    private CellStyle contentCellStyle = null;

    public CellStyle getUserDefinedCellStyle()
    {
        return userDefinedCellStyle;
    }

    public CellStyle getTitleCellStyle()
    {
        return titleCellStyle;
    }

    public CellStyle getContentCellStyle()
    {
        return contentCellStyle;
    }

    /**
     * 创建默认的单元格样式
     */
    private void createCellStyle()
    {
        // 粗体显示
        Font boldFont = wb.createFont();
        boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

        // 默认单元格样式，可修改使用
        userDefinedCellStyle = wb.createCellStyle();

        // 默认的标题单元格样式
        titleCellStyle = wb.createCellStyle();
        titleCellStyle.setFont(boldFont);
        // 设置背景色
        // titleCellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        // titleCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        titleCellStyle.setBorderBottom(CellStyle.BORDER_MEDIUM); // 下边框
        titleCellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);// 左边框
        titleCellStyle.setBorderTop(CellStyle.BORDER_MEDIUM);// 上边框
        titleCellStyle.setBorderRight(CellStyle.BORDER_MEDIUM);// 右边框

        // 默认的内容单元格样式
        contentCellStyle = wb.createCellStyle();
        contentCellStyle.setBorderBottom(CellStyle.BORDER_THIN); // 下边框
        contentCellStyle.setBorderLeft(CellStyle.BORDER_THIN);// 左边框
        contentCellStyle.setBorderTop(CellStyle.BORDER_THIN);// 上边框
        contentCellStyle.setBorderRight(CellStyle.BORDER_THIN);// 右边框
    }

    private void setExcelCellStyle(Cell cell)
    {
        if (isUseCellStyle)
        {
            if (cellStyleType == USER_DEFINED_CELL_STYLE)
            {
                cell.setCellStyle(userDefinedCellStyle);
            }
            if (cellStyleType == TITIL_CELL_STYLE)
            {
                cell.setCellStyle(titleCellStyle);
            }
            if (cellStyleType == CONTENT_CELL_STYLE)
            {
                cell.setCellStyle(contentCellStyle);
            }
        }
    }

    private boolean isUseCellStyle = false;

    private int cellStyleType = 2;

    public static int USER_DEFINED_CELL_STYLE = 0; // 自定义样式

    public static int TITIL_CELL_STYLE = 1; // 标题样式

    public static int CONTENT_CELL_STYLE = 2; // 内容样式

    public void useCellStyle(boolean isUseCellStyle)
    {
        this.isUseCellStyle = isUseCellStyle;
    }

    public void setCellStyleType(int cellStyleType)
    {
        this.cellStyleType = cellStyleType;
    }
}
