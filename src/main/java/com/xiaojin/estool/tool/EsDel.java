package com.xiaojin.estool.tool;

//import com.coship.base.http.websocket.WebSocketUtil;
//import com.coship.base.util.data.JsonUtil;
//import com.coship.base.util.date.DateUtil;
//import com.coship.base.util.html.HtmlUtil;
//import com.coship.base.util.http.HttpClient;
//import com.coship.base.util.http.HttpClientLogCtrl;
import com.fasterxml.jackson.databind.JsonNode;
import com.xiaojin.estool.http.websocket.WebSocketUtil;
import com.xiaojin.estool.util.data.JsonUtil;
import com.xiaojin.estool.util.date.DateUtil;
import com.xiaojin.estool.util.html.HtmlUtil;
import com.xiaojin.estool.util.http.HttpClient;
import com.xiaojin.estool.util.http.HttpClientLogCtrl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EsDel
{
    private static final Logger logger = LoggerFactory.getLogger(EsDel.class);

    public static void deleteByExportFile(String esUrl, String dataFilePath, WebSocket conn)
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
                    sendWebSocket(conn, "[" + DateUtil.formatPattenFull(new Date()) + "]Delete count:" + f.format(count));
                }
            }
            send2es(bulkClient, esUrl, contentList, 0);
            sendWebSocket(conn, "[" + DateUtil.formatPattenFull(new Date()) + "]Delete count:" + f.format(count));
            sendWebSocket(conn, "Delete over.");
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

    private static void send2es(HttpClient addClient, String esUrl, List<String> contentList, int tryCount)
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
                // 改为批量处理
                sb.append("{\"delete\":{");
                sb.append("\"_index\":\"").append(index).append("\"");
                sb.append(",\"_type\":\"").append(type).append("\"");
                sb.append(",\"_id\":\"").append(id).append("\"");
                sb.append("}}");
                sb.append("\r\n");
            }
            addClient.doPut(sb.toString(), HttpClient.ENCODING_UTF8);
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

    private static void sendWebSocket(WebSocket conn, String text)
    {
        logger.info(text);
        if (conn != null)
        {
            WebSocketUtil.send(conn, text.replaceAll("\r\n", "<br/>")); // 发送websocket消息到页面
        }
    }
}
