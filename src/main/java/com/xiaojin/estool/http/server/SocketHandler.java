package com.xiaojin.estool.http.server;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther dingjian
 * @Date 2021-01-04 9:00 下午
 */


public class SocketHandler implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);

    private Socket socket;

    private Dispatcher dispatcher;

    public SocketHandler(Socket socket, Dispatcher dispatcher)
    {
        this.socket = socket;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run()
    {
        InputStream input = null;
        OutputStream output = null;
        try
        {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            // 创建Request对象并解析
            Request request = new Request(input);
            request.setLocalAddress(socket.getLocalAddress());
            request.setRemoteAddress((InetSocketAddress) socket.getRemoteSocketAddress());
            // 创建 Response 对象
            Response response = dispatcher.getResponse(request);
            response.write(output);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            try
            {
                // 关闭 socket 对象
                socket.close();
            }
            catch (IOException e)
            {
                logger.error("", e);
            }
        }
    }

}
