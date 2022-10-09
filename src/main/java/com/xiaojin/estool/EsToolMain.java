package com.xiaojin.estool;

//import com.coship.base.http.server.HttpServer;
//import com.coship.base.http.websocket.BaseWebSocketServer;
//import com.coship.estool.cache.ConfigCache;
//import com.coship.estool.controller.EsToolDispatcher;
//import com.coship.estool.controller.EsToolWebSocketServer;
import com.xiaojin.estool.controller.EsToolDispatcher;
import com.xiaojin.estool.http.server.HttpServer;
import lombok.extern.slf4j.Slf4j;
import com.xiaojin.estool.cache.ConfigCache;
import com.xiaojin.estool.controller.EsToolWebSocketServer;
import com.xiaojin.estool.http.websocket.BaseWebSocketServer;
import org.java_websocket.drafts.Draft_17;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Slf4j
public class EsToolMain
{
    private static final Logger logger = LoggerFactory.getLogger(EsToolMain.class);

    public static void main(String[] args)
    {
        // 加载配置文件
        ConfigCache.loadConfig();
        // 建立websocket端口
        try
        {
            BaseWebSocketServer webSocketServer = new EsToolWebSocketServer(8057, new Draft_17());
            webSocketServer.start();
        }
        catch (Exception e)
        {
            log.error("", e);
        }
        // 建立http端口
        HttpServer server = new HttpServer();
        String host = "0.0.0.0";
        int port = 8076;
        if (args != null)
        {
            if (args.length > 0)
            {
                host = args[0];
            }
            if (args.length > 1)
            {
                port = Integer.valueOf(args[1]);
            }
        }
        // 等待连接请求
        server.await(host, port, new EsToolDispatcher());
    }
}
