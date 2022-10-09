package com.xiaojin.estool.util.es.export.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.xiaojin.estool.util.date.DateUtil;
import com.xiaojin.estool.util.es.EsUtil;
import com.xiaojin.estool.util.html.HtmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExportToExcelFileHandler extends EsExportToExcelHandler
{
    private static Logger logger = LoggerFactory.getLogger(ExportToExcelFileHandler.class);

    private String[] fieldNames;

    @Override
    public void init()
    {
        this.fieldNames = extratFileNames(sql);
        excelWriteUtil.writeExcelData(fieldNames, 0);
    }

    @Override
    public void dataProcess(JsonNode jsonNode, long reqCount, long currCount, long allCount) throws Exception
    {
        JsonNode data = jsonNode.path("_source");
        List<Object> tmp = new ArrayList();
        for (int i = 0; i < fieldNames.length; i++)
        {
            tmp.add(EsUtil.extractJsonValue(data.path(fieldNames[i])));
        }
        excelWriteUtil.writeExcelData(tmp, (int) allCount - 1 + startIndex);
    }

    @Override
    public void close()
    {
        String filePath = System.getProperty("user.dir") + "/" + super.getEsIndexName() + ".xlsx";
        if (StringUtils.isNotEmpty(super.fileSavePath))
        {
            filePath = super.fileSavePath;
        }
        File saveFile = new File(filePath);
        try
        {
            excelWriteUtil.write(saveFile);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        // 记录文件保存路径
        logger("File Save Path:" + filePath);
    }

    @Override
    public void logger(String message)
    {
        logger.info(message);
        sendWebSocket("[" + DateUtil.formatPattenFull(new Date()) + "]" + message);
    }

    @Override
    public void logger(Exception e)
    {
        try
        {
            sendWebSocket(HtmlUtil.escapeLineBreak(e));
        }
        catch (Exception e1)
        {
            logger.error("", e1);
        }
    }

}
