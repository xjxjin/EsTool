package com.xiaojin.estool.tool;


import com.fasterxml.jackson.databind.JsonNode;
import com.xiaojin.estool.http.websocket.WebSocketUtil;
import com.xiaojin.estool.util.data.JsonUtil;
import com.xiaojin.estool.util.date.DateUtil;
import com.xiaojin.estool.util.html.HtmlUtil;
import com.xiaojin.estool.util.http.HttpClient;
import com.xiaojin.estool.util.http.HttpClientLogCtrl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EsImp
{
    private static final Logger logger = LoggerFactory.getLogger(EsImp.class);

    private boolean isUseSourceId = true;

    public static void main(String[] args)
    {
        String esUrl = "119.23.148.91:9200";
        EsImp esImp = new EsImp();
        esImp.dataImport(esUrl, "e:/tmp/tianwei_crawler.json", null);
    }

    public void dataImport(String esUrl, String dataFilePath, WebSocket conn)
    {
        DecimalFormat f = new DecimalFormat("#,####");
        esUrl = "http://" + esUrl;
        LineIterator it = null;
        try
        {
            it = FileUtils.lineIterator(new File(dataFilePath), "utf-8");
            // 采用批量的方式新增数据
            HttpClient bulkClient = new HttpClient(esUrl + "/_bulk", 10000).setLogCtrl(HttpClientLogCtrl.NONE);

            List<String> contentList = new ArrayList<String>();
            long count = 0;
            while (it.hasNext())
            {
                String line = it.nextLine();
                if (line == null || line.length() == 0)
                {
                    continue;
                }
                count++;
                contentList.add(line);

                if (count % 200 == 0)
                {
                    send2es(bulkClient, esUrl, contentList, 0);
                    sendWebSocket(conn, "[" + DateUtil.formatPattenFull(new Date()) + "]Put count:" + f.format(count));
                }
            }
            send2es(bulkClient, esUrl, contentList, 0);
            sendWebSocket(conn, "[" + DateUtil.formatPattenFull(new Date()) + "]Put count:" + f.format(count));
            sendWebSocket(conn, "Import over.");
        }
        catch (Exception e)
        {
            logger.error("", e);
            try
            {
                sendWebSocket(conn, HtmlUtil.escapeLineBreak(e));
            }
            catch (Exception e1)
            {
                logger.error("", e1);
            }
        }
        finally
        {
            LineIterator.closeQuietly(it);
        }
    }

    private void send2es(HttpClient addClient, String esUrl, List<String> contentList, int tryCount)
    {
        if (tryCount >= 10)
        {
            contentList.clear();
            return;
        }
        try
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0, size = contentList.size(); i < size; i++)
            {
                String content = contentList.get(i);
                JsonNode root = JsonUtil.readTree(content);
                String index = root.path("_index").asText();
                String type = root.path("_type").asText();
                String id = root.path("_id").asText();
                String source = root.path("_source").toString();
                // 改为批量处理
                sb.append("{\"index\":{");
                sb.append("\"_index\":\"").append(index).append("\"");
                sb.append(",\"_type\":\"").append(type).append("\"");
                if (isUseSourceId && StringUtils.isNotEmpty(id))
                {
                    sb.append(",\"_id\":\"").append(id).append("\"");
                }
                sb.append("}}");
                sb.append("\r\n");
                sb.append(source).append("\r\n");
            }
            String s = addClient.doPut(sb.toString(), HttpClient.ENCODING_UTF8);
            System.out.println(s);
        }
        catch (Exception e)
        {
            logger.info("TryCount=" + tryCount);
            logger.error("", e);
            send2es(addClient, esUrl, contentList, tryCount + 1);
        }
        // 将集合清空
        contentList.clear();
    }

    private void sendWebSocket(WebSocket conn, String text)
    {
        logger.info(text);
        if (conn != null)
        {
            WebSocketUtil.send(conn, text.replaceAll("\r\n", "<br/>")); // 发送websocket消息到页面
        }
    }

    public void setUseSourceId(boolean isUseSourceId)
    {
        this.isUseSourceId = isUseSourceId;
    }

}
