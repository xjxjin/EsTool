package com.xiaojin.estool.http.websocket;

import com.xiaojin.estool.util.html.HtmlUtil;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebSocketUtil
{
    private static final Logger logger = LoggerFactory.getLogger(WebSocketUtil.class);

    public static void send(WebSocket conn, String message)
    {
        if (conn != null && conn.getReadyState() == WebSocket.READYSTATE.OPEN)
        {
            conn.send(message);
        }
        else
        {
            logger.error("Connection status is [" + (conn == null ? null : conn.getReadyState()) + "]");
        }
    }

    public static void send(WebSocket conn, Throwable t) throws Exception
    {
        if (conn != null && conn.getReadyState() == WebSocket.READYSTATE.OPEN)
        {
            conn.send(HtmlUtil.escapeLineBreak(t));
        }
        else
        {
            logger.error("Connection status is [" + (conn == null ? null : conn.getReadyState()) + "]");
        }
    }
}
