package com.xiaojin.estool.http.server;


import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @Auther dingjian
 * @Date 2021-01-04 8:58 下午
 */

//import java.net.BindException;
//import java.net.InetAddress;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.SynchronousQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.logging.SocketHandler;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class HttpServer
{
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private static ExecutorService cachedThreadPool = new ThreadPoolExecutor(0, 100, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    public void await(String host, int port, Dispatcher dispatcher)
    {
        ServerSocket serverSocket = null;
        try
        {
            // 服务器套接字对象
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName(host));
        }
        catch (BindException be)
        {
            logger.error("Port [" + port + "] already in use.", be);
            System.exit(1);
        }
        catch (Exception e)
        {
            logger.error("", e);
            System.exit(1);
        }

        /*try
        {
            // 打开默认浏览器
            // Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler http://127.0.0.1:" + port);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }*/

        // 循环等待一个请求
        while (true)
        {
            Socket socket = null;
            try
            {
                // 等待连接，连接成功后，返回一个Socket对象
                socket = serverSocket.accept();
                cachedThreadPool.execute(new SocketHandler(socket, dispatcher));
            }
            catch (Exception e)
            {
                logger.error("", e);
            }

        }
    }
}
