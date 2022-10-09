package com.xiaojin.estool.util.es.export.export;

import com.xiaojin.estool.util.excel.ExcelWriteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EsExportToExcelHandler extends EsExportHandler
{
    private static Logger logger = LoggerFactory.getLogger(EsExportToExcelHandler.class);

    protected ExcelWriteUtil excelWriteUtil;

    protected int startIndex = 1;

    @Override
    public void init()
    {
    }

    @Override
    public void close()
    {
    }

    @Override
    public void logger(String message)
    {
        logger.info(message);
    }

    @Override
    public void logger(Exception e)
    {
    }

    public void setExcelWriteUtil(ExcelWriteUtil excelWriteUtil)
    {
        this.excelWriteUtil = excelWriteUtil;
    }

    public void setStartIndex(int startIndex)
    {
        this.startIndex = startIndex;
    }
}
