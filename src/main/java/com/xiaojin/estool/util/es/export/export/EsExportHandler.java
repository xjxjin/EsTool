package com.xiaojin.estool.util.es.export.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.xiaojin.estool.http.websocket.WebSocketUtil;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.WebSocket;

import java.util.StringTokenizer;

public abstract class EsExportHandler
{
    public abstract void init();

    public long dataListProcess(JsonNode root, long reqCount) throws Exception
    {
        long count = 0;
        // 返回的数据集合
        JsonNode hits = root.path("hits").path("hits");
        for (JsonNode jsonNode : hits)
        {
            count++; // 提取的记录数+1
            allCount++;// 总提取的记录数+1
            dataProcess(jsonNode, reqCount, count, allCount);
        }

        return count;
    }

    public abstract void dataProcess(JsonNode jsonNode, long reqCount, long currCount, long allCount) throws Exception;

    public abstract void logger(String message);

    public abstract void logger(Exception e);

    public abstract void close();

    protected String dayFileNameKey;

    protected String fileSavePath;

    protected String sql;

    private WebSocket conn;

    protected long allCount = 0L;

    public String getDayFileNameKey()
    {
        return dayFileNameKey;
    }

    public void setDayFileNameKey(String dayFileNameKey)
    {
        this.dayFileNameKey = dayFileNameKey;
    }

    public void setFileSavePath(String fileSavePath)
    {
        this.fileSavePath = fileSavePath;
    }

    public String getSql()
    {
        return sql;
    }

    public long getAllCount()
    {
        return allCount;
    }

    public void setSql(String sql)
    {
        // 将SQL格式化，去掉换行，多个空格等无用字符
        this.sql = removeBreakingWhitespace(sql);
    }

    public String getEsIndexName()
    {
        String indexName = StringUtils.substringBetween(this.sql, " from ", " ");
        indexName = indexName.replaceAll("\\*", "");
        return indexName;
    }

    public String removeBreakingWhitespace(String original)
    {
        StringTokenizer whitespaceStripper = new StringTokenizer(original);
        StringBuilder builder = new StringBuilder();
        while (whitespaceStripper.hasMoreTokens())
        {
            String token = whitespaceStripper.nextToken();
            if ("select".equalsIgnoreCase(token) || "from".equalsIgnoreCase(token) || "where".equalsIgnoreCase(token)
                    || "and".equalsIgnoreCase(token))
            {
                token = token.toLowerCase();
            }
            builder.append(token);
            builder.append(" ");
        }
        return builder.toString();
    }

    public String[] extratFileNames(String sql)
    {
        String columnStr = StringUtils.substringBetween(sql, "select ", " from ");
        columnStr = columnStr.replace(" ", "");
        return columnStr.split(",");
    }

    protected void sendWebSocket(String text)
    {
        if (conn != null)
        {
            WebSocketUtil.send(conn, text.replaceAll("\r\n", "<br/>")); // 发送websocket消息到页面
        }
    }

    public void setConn(WebSocket conn)
    {
        this.conn = conn;
    }
}
