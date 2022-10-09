package com.xiaojin.estool.util.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.xiaojin.estool.util.date.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;


public class ExcelReadUtil
{
    private Workbook wb = null;

    private Sheet sheet = null;

    public ExcelReadUtil(InputStream is) throws Exception
    {
        wb = WorkbookFactory.create(is);
        sheet = wb.getSheetAt(0); // 默认使用第一个sheet页
    }

    public ExcelReadUtil(File file) throws Exception
    {
        this(new FileInputStream(file));
    }

    public List<String[]> readLinesByFetchColumn()
    {
        return readLinesByFetchColumn(255);
    }

    public List<String[]> readLinesByFetchColumn(int fetchColumnsSize)
    {
        List<String[]> list = new ArrayList<String[]>();
        int totalRowsNum = sheet.getLastRowNum() + 1; // 获取总行数
        for (int rows = 0; rows < totalRowsNum; rows++)
        {
            Row row = sheet.getRow(rows); // 取得某一行 对象
            if (row != null)
            {
                if (rows == 0) // 解析第一行数据，获取总列数
                {
                    int allColumnsNum = row.getLastCellNum(); // 获取总列数，即：列标+1
                    if (allColumnsNum < fetchColumnsSize) // 如果Excel中的列，小于要获取的列的值，按指定实际情况获取数据
                    {
                        fetchColumnsSize = allColumnsNum;
                    }
                }
                list.add(readRowData(row, fetchColumnsSize));
            }
        }
        return list;
    }

    public int getNumberOfSheets()
    {
        return wb.getNumberOfSheets();
    }

    public void changeSheet(int index)
    {
        sheet = wb.getSheetAt(index);
    }

    public void changeSheet(String sheetName)
    {
        sheet = wb.getSheet(sheetName);
    }

    public Iterator<String[]> iterator()
    {
        return new ExcelReadIterator();
    }

    /**
     * 读取一行的数据
     */
    private String[] readRowData(Row row, int fetchColumnsSize)
    {
        String[] rowData = new String[fetchColumnsSize];
        for (int columnNum = 0; columnNum < fetchColumnsSize; columnNum++)
        {
            Cell cell = row.getCell(columnNum);
            rowData[columnNum] = readValueFromCell(cell);
        }
        return rowData;
    }

    /**
     * 读取单元格的数据
     */
    private String readValueFromCell(Cell cell)
    {
        if (cell != null)
        {
            switch (cell.getCellType())
            {
                case XSSFCell.CELL_TYPE_STRING: // 字符串
                    return cell.getStringCellValue();

                case XSSFCell.CELL_TYPE_NUMERIC: // 数字
                    if (HSSFDateUtil.isCellDateFormatted(cell))// 如果是date类型则 ，获取该cell的date值
                    {
                        return DateUtil.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()), "yyyy-MM-dd");
                    }
                    // 纯数字
                    else
                    {
                        double value = cell.getNumericCellValue();
                        DecimalFormat bigDecimal = new DecimalFormat("0.00000000#");
                        String result = bigDecimal.format(value);
                        // 去除结尾的.0000字符串
                        String[] resultArr = StringUtils.split(result, '.');
                        if (Integer.parseInt(resultArr[1]) == 0)
                        {
                            return resultArr[0];
                        }
                        return result;
                    }

                case XSSFCell.CELL_TYPE_BOOLEAN: // Boolean
                    return String.valueOf(cell.getBooleanCellValue());

                case XSSFCell.CELL_TYPE_BLANK: // 空值
                case XSSFCell.CELL_TYPE_ERROR:
                default:
                    return "";
            }
        }
        return "";
    }

    /**
     * 使用迭代的方式，读取Excel中的数据
     */
    private class ExcelReadIterator implements Iterator
    {

        private int lastCellNum = 0; // 总列数

        private int maxRowNum = 0; // 总记录行数

        private int currRowsNum = 0; // 当前获取记录行数

        public ExcelReadIterator()
        {
            maxRowNum = sheet.getLastRowNum() + 1; // 获取总行数
        }

        public boolean hasNext()
        {
            return currRowsNum < maxRowNum;
        }

        @Override
        public String[] next()
        {
            try
            {
                Row row = sheet.getRow(currRowsNum); // 取得某一行 对象
                if (row != null)
                {
                    if (currRowsNum == 0) // 解析第一行数据，获取总列数
                    {
                        lastCellNum = row.getLastCellNum(); // 获取总列数，即：列标+1
                    }
                    return readRowData(row, lastCellNum);
                }
                return null;
            }
            finally
            {
                currRowsNum++;
            }
        }

        @Override
        public void remove()
        {
        }

    }

}
