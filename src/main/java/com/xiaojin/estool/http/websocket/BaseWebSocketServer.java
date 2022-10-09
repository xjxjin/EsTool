package com.xiaojin.estool.http.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseWebSocketServer extends WebSocketServer
{
    private static final Logger logger = LoggerFactory.getLogger(BaseWebSocketServer.class);

    private static AtomicInteger counter = new AtomicInteger(0);

    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    // 若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    protected static CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<WebSocket>();

    public BaseWebSocketServer(int port, Draft d) // throws UnknownHostException
    {
        super(new InetSocketAddress(port), Collections.singletonList(d));
    }

    public BaseWebSocketServer(InetSocketAddress address, Draft d)
    {
        super(address, Collections.singletonList(d));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        webSocketSet.add(conn);
        int cnt = counter.incrementAndGet();
        logger.debug("///////////Opened connection number " + cnt);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        webSocketSet.remove(conn);
        counter.decrementAndGet();
        logger.debug("closed");
    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        logger.debug("Error:");
        logger.error("", ex);
    }

    @Override
    public void onMessage(WebSocket conn, String message)
    {
        logger.debug(message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer blob)
    {
        logger.debug(blob.toString());
    }

    public void onSend(String message)
    {
        for (WebSocket conn : webSocketSet)
        {
            WebSocketUtil.send(conn, message);
        }
    }

    @Override
    public void onWebsocketMessageFragment(WebSocket conn, Framedata frame)
    {
        FrameBuilder builder = (FrameBuilder) frame;
        builder.setTransferemasked(false);
        conn.sendFrame(frame);
    }

    public static void main(String[] args) // throws UnknownHostException
    {
        WebSocketImpl.DEBUG = false;
        int port;
        try
        {
            port = new Integer(args[0]);
        }
        catch (Exception e)
        {
            System.out.println("No port specified. Defaulting to 9003");
            port = 9003;
        }
        new BaseWebSocketServer(port, new Draft_17()).start();
    }

    /**
     * 从9000端口开始检测，获取一个可用端口
     */
    public static int getValidPort()
    {
        for (int i = 9000; i < 65535; i++)
        {
            try
            {
                new DatagramSocket(i);
                logger.info("Get valid port:" + i);
                return i;
            }
            catch (Exception ex)
            {
                continue;
            }
        }
        return -1;

    }

}
