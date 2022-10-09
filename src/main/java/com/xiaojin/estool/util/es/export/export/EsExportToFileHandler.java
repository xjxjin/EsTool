package com.xiaojin.estool.util.es.export.export;


import com.fasterxml.jackson.databind.JsonNode;
import com.xiaojin.estool.util.date.DateUtil;
import com.xiaojin.estool.util.html.HtmlUtil;
import com.xiaojin.estool.util.io.FileWriteUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public abstract class EsExportToFileHandler extends EsExportHandler
{
    private static Logger logger = LoggerFactory.getLogger(EsExportToFileHandler.class);

    protected String fileSaveDir;

    protected boolean append = false;

    protected FileWriteUtil fileWriteUtil;

    @Override
    public void init()
    {
        // 初始化文件保存路径
        // 使用指定的文件路径
        if (StringUtils.isNotEmpty(super.fileSavePath))
        {
            this.fileWriteUtil = new FileWriteUtil(super.fileSavePath, append);
        }
        // 未指定文件路径，则使用目录+索引名的方式保存
        else
        {
            String filePath = getFileSaveDir() + "/" + getFileName() + getSuffix();
            this.fileWriteUtil = new FileWriteUtil(filePath, append);
        }
    }

    @Override
    public final long dataListProcess(JsonNode root, long reqCount) throws Exception
    {
        long count = super.dataListProcess(root, reqCount);
        fileWriteUtil.flush();
        return count;
    }

    @Override
    public void dataProcess(JsonNode jsonNode, long reqCount, long currCount, long allCount) throws Exception
    {
        writeFile(jsonNode);
    }

    @Override
    public void close()
    {
        fileWriteUtil.close();
        // 记录文件保存路径
        logger("File Save Path:" + fileWriteUtil.getFilePath());
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
        // logger.error("", e);
        try
        {
            sendWebSocket(HtmlUtil.escapeLineBreak(e));
        }
        catch (Exception e1)
        {
            logger.error("", e1);
        }
    }

    public abstract void writeFile(JsonNode dataNode);

    public abstract String getSuffix();

    public void setFileSaveDir(String fileSaveDir)
    {
        this.fileSaveDir = fileSaveDir;
    }

    public String getFileSaveDir()
    {
        if (StringUtils.isEmpty(this.fileSaveDir))
        {
            this.fileSaveDir = System.getProperty("user.dir");
        }
        return fileSaveDir;
    }

    public void setAppend(boolean append)
    {
        this.append = append;
    }

    public String getFileName()
    {
        if (StringUtils.isNotEmpty(super.dayFileNameKey))
        {
            return super.getEsIndexName() + super.dayFileNameKey;
        }
        return super.getEsIndexName();
    }
}
