package com.xiaojin.estool.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.xiaojin.estool.entity.FileVo;
import com.xiaojin.estool.http.websocket.BaseWebSocketServer;
import com.xiaojin.estool.http.websocket.WebSocketUtil;
import com.xiaojin.estool.tool.EsDel;
import com.xiaojin.estool.tool.EsImp;
import com.xiaojin.estool.util.data.JsonUtil;
import com.xiaojin.estool.util.date.DateUtil;
import com.xiaojin.estool.util.es.EsExportUtil;
import com.xiaojin.estool.util.es.export.export.*;
import com.xiaojin.estool.util.excel.ExcelWriteUtil;
import com.xiaojin.estool.util.sql.SqlUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

public class EsToolWebSocketServer extends BaseWebSocketServer
{
    private static final Logger logger = LoggerFactory.getLogger(EsToolWebSocketServer.class);

    public EsToolWebSocketServer(int port, Draft d) // throws UnknownHostException
    {
        super(port, d);
    }

    @Override
    public void onMessage(WebSocket conn, String message)
    {
        logger.info(message);
        long startTime = System.currentTimeMillis();
        try
        {
            JsonNode node = JsonUtil.readTree(message);
            String type = node.path("type").asText();
            String esUrl = node.path("esUrl").asText();
            String sortType = node.path("sortType").asText();
            if ("browseFile".equals(type)) // 浏览文件
            {
                sortType = "2".equals(sortType) ? "updateTime" : "fileName";
                File dir = getDirFile(node);
                if (null == dir)
                {
                    logger.info("Root directory");
                    return;
                }
                File[] listFiles = dir.listFiles(new DataFileFilter());
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("dirPath", dir.getPath());
                List<FileVo> list = new ArrayList<FileVo>();
                list.add(new FileVo("..", false));
                for (File file : listFiles)
                {
                    FileVo vo = new FileVo(file);
                    vo.setSortType(sortType);
                    list.add(vo);
                }
                // 按文件名进行排序
                Collections.sort(list);
                map.put("dirListFiles", list);
                String result = JsonUtil.writeValueAsString(map);
                logger.info(result);
                WebSocketUtil.send(conn, result);
            }
            else if ("deleteSql".equals(type)) // 删除
            {
                String fileSavePath = "/tmp/" + UUID.randomUUID().toString();
                String deleteSql = node.path("deleteSql").asText();
                deleteSql = deleteSql.replace("delete", "select not_exists_field ");
                ExportToBackupFileHandler handler = new ExportToBackupFileHandler();
                handler.setFileSavePath(fileSavePath);
                handler.setConn(conn);
                handler.setSql(deleteSql);
                EsExportUtil util = new EsExportUtil("http://" + esUrl);
                util.dataExport(handler);

                EsDel.deleteByExportFile(esUrl, fileSavePath, conn);
                // 删除成功后，删除临时文件
                FileUtils.forceDelete(new File(fileSavePath));
                logger(conn, startTime);
                conn.close();
            }
            else if ("export".equals(type)) // 导出
            {
                // String indexName = node.path("indexName").asText();
                // indexName = indexName.replaceAll("\t", "").replaceAll(" ", "");
                // EsExp.dataExport(esUrl, indexName, conn);
                String querySql = node.path("exportQuerySql").asText();
                querySql = SqlUtil.removeBreakingWhitespace(querySql);
                String exportType = node.path("exportType").asText();
                String exportSeparator = node.path("exportSeparator").asText();
                boolean exportByDay = "true".equalsIgnoreCase(node.path("exportByDay").asText());
                // EsExp2.dataExport(esUrl, querySql, exportToLog, conn);
                EsExportHandler handler = null;
                if ("2".equals(exportType))
                {
                    if ("\\t".equals(exportSeparator))
                    {
                        exportSeparator = "\t";
                    }
                    handler = new ExportToLogFileHandler();
                    ((ExportToLogFileHandler) handler).setSeparator(exportSeparator);
                }
                else if ("3".equals(exportType))
                {
                    handler = new ExportToDataFileHandler();
                }
                else if ("4".equals(exportType))
                {
                    handler = new ExportToExcelFileHandler();
                    InputStream is = ExcelWriteUtil.class.getResourceAsStream("template.xlsx");
                    ExcelWriteUtil util = new ExcelWriteUtil(is, true);
                    ((ExportToExcelFileHandler) handler).setExcelWriteUtil(util);
                }
                else
                {
                    handler = new ExportToBackupFileHandler();
                }
                handler.setConn(conn);
                handler.setSql(querySql);
                EsExportUtil util = new EsExportUtil("http://" + esUrl);
                if (exportByDay)
                {
                    util.dataExportByDay(handler);
                }
                else
                {
                    util.dataExport(handler);
                }
                conn.close();
            }
            else if ("import".equals(type)) // 导入
            {
                String dataFilePath = node.path("dataFilePath").asText();
                boolean importUseSourceId = "true".equalsIgnoreCase(node.path("importUseSourceId").asText());
                EsImp esImp = new EsImp();
                esImp.setUseSourceId(importUseSourceId);
                esImp.dataImport(esUrl, dataFilePath, conn);
                conn.close();
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            logger.error("", e);
        }
    }

    private void logger(WebSocket conn, long startTime)
    {
        long endTime = System.currentTimeMillis();
        String startTimeStr = DateUtil.formatPattenFull(new Date(startTime));
        String endTimeStr = DateUtil.formatPattenFull(new Date(endTime));
        String duration = DateUtil.parseDurationMillis(endTime - startTime);
        String logMsg = MessageFormat
                .format("TaskExecutionOver.StartTime={0},EndTime={1},CostTime={2}", startTimeStr, endTimeStr, duration);
        logger.info(logMsg);
        WebSocketUtil.send(conn, logMsg);
    }

    private File getDirFile(JsonNode node)
    {
        String dirPath = node.path("dirPath").asText();
        String subPath = node.path("subPath").asText();
        File dir = null;
        if (StringUtils.isEmpty(dirPath))
        {
            dirPath = System.getProperty("user.dir");
            dir = new File(dirPath);
        }
        else
        {
            if (StringUtils.isEmpty(subPath))
            {
                dir = new File(dirPath);
                if (dir.isFile())
                {
                    dir = dir.getParentFile();
                }
            }
            else if ("..".equals(subPath))
            {
                dir = new File(dirPath);
                dir = dir.getParentFile();
            }
            else
            {
                dir = new File(dirPath, subPath);
            }

        }
        return dir;
    }
}

class DataFileFilter implements FileFilter
{
    @Override
    public boolean accept(File pathname)
    {
        if (pathname.isDirectory())
        {
            return true;
        }
        String fileName = pathname.getName();
        if (fileName.endsWith(".json"))
        {
            return true;
        }
        return false;
    }
}
