package com.xiaojin.estool.util.excel;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

public class ExcelUtil
{
    /**
     * 返回带有样式的Excel表格
     * @param name
     * @param workbook
     * @return
     */
    public static HSSFWorkbook getHSSFWorkbook(String name, HSSFWorkbook workbook)
    {
        HSSFSheet sheet = workbook.createSheet(name);
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 5000);

        // sheet.createFreezePane(1, 3);// 冻结

        // HSSFCellStyle sheetStyle = workbook.createCellStyle(); // 背景色的设定
        // sheetStyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index); // 前景色的设定
        // sheetStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index); // 填充模式
        // sheetStyle.setFillPattern(HSSFCellStyle.FINE_DOTS); // 设置列的样式
        // for (int i = 0; i <= 14; i++)
        // {
        // sheet.setDefaultColumnStyle((short) i, sheetStyle);
        // }

        return workbook;
    }

    public static HSSFCellStyle getheadStyle(HSSFWorkbook workbook)
    {
        HSSFFont headfont = workbook.createFont();
        headfont.setFontName("SimHei");// 黑体
        headfont.setFontHeightInPoints((short) 12);// 字体大小
        // 另一个样式
        HSSFCellStyle headstyle = workbook.createCellStyle();
        headstyle.setFont(headfont);

        headstyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
        // headstyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
        headstyle.setLocked(true);
        // headstyle.setWrapText(true);// 自动换行
        return headstyle;
    }

    public static HSSFCellStyle getStyle(HSSFWorkbook workbook)
    {
        HSSFFont font = workbook.createFont();
        font.setFontName("SimSun");// 宋体
        font.setFontHeightInPoints((short) 13);
        // 普通单元格样式
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);// 左右居中
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);// 上下居中
        style.setWrapText(true);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setBorderLeft((short) 0);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight((short) 0);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 设置单元格的边框为粗体
        style.setBottomBorderColor(HSSFColor.BLACK.index); // 设置单元格的边框颜色．
        style.setFillForegroundColor(HSSFColor.WHITE.index);// 设置单元格的背景颜色．
        return style;
    }
}
