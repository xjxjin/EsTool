package com.xiaojin.estool.controller;



import com.xiaojin.estool.cache.ConfigCache;
import com.xiaojin.estool.http.server.Dispatcher;
import com.xiaojin.estool.http.server.Request;
import com.xiaojin.estool.http.server.Response;
import com.xiaojin.estool.util.io.IOUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;

public class EsToolDispatcher implements Dispatcher
{
    private static final Logger logger = LoggerFactory.getLogger(EsToolDispatcher.class);

    private static String WEB_ROOT = "/webroot";

    public Response getResponse(Request request)
    {
        String uri = request.getRequestUri();
        // 服务端的IP，用于浏览器端生成websocket的访问URL
        String hostAddr = request.getLocalAddress().getHostAddress();
        // 客户端IP，用于缓存客户端使用的参数
        String clientAddr = request.getRemoteAddress().getAddress().getHostAddress();

        Map<String, String> param = request.getParameter();
        // 缓存esUrl
        String esUrl = param.get("esUrl");
        if (esUrl != null && esUrl.length() > 0)
        {
            ConfigCache.put(clientAddr, esUrl);
        }
        Response response = null;
        try
        {
            if (StringUtils.isEmpty(uri))
            {
                return Response.NOT_FOUND;
            }
            if ("/".equals(uri))
            {
                uri = "/index.html";
            }
            if ("/getConfig".equals(uri))
            {
                response = new EsToolController().getConfig(hostAddr, clientAddr);
            }
            else if ("/sqlQuery".equals(uri))
            {
                response = new EsToolController().sqlQuery(param);
            }
            else if ("/dslQuery".equals(uri))
            {
                response = new EsToolController().dslQuery(param);
            }
            else if ("/refreshIndex".equals(uri))
            {
                response = new EsToolController().refreshIndex(param);
            }
            else if ("/viewIndex".equals(uri))
            {
                response = new EsToolController().viewIndex(param);
            }
            else if ("/deleteIndex".equals(uri))
            {
                response = new EsToolController().deleteIndex(param);
            }
            else if ("/refreshTemplate".equals(uri))
            {
                response = new EsToolController().refreshTemplate(param);
            }
            else if ("/viewTemplate".equals(uri))
            {
                response = new EsToolController().viewTemplate(param);
            }
            else if ("/updateTemplate".equals(uri))
            {
                response = new EsToolController().updateTemplate(param);
            }
            // 静态文件
            else
            {
                String result = null;
                if (isRunAsJar) // 如果是以jar的方式运行，通过输入流的方式读取
                {
                    InputStream is = EsToolDispatcher.class.getResourceAsStream(WEB_ROOT + uri);
                    if (is == null)
                    {
                        return Response.NOT_FOUND;
                    }
                    result = IOUtils.toString(is, "utf-8");
                }
                else
                {
                    InputStream is = EsToolDispatcher.class.getResourceAsStream(WEB_ROOT + uri);
                 if(is==null){
                     return Response.NOT_FOUND;
                 }
                    result = IOUtils.toString(is,"utf-8");

                }
                if (StringUtils.isEmpty(result))
                {
                    return Response.NOT_FOUND;
                }
                response = new Response(uri).setResponseBody(result);
            }
        }
        catch (Exception e)
        {
            logger.error("uri=" + uri, e);
        }
        if (response == null)
        {
            return Response.NOT_FOUND;
        }
        return response;
    }

    private static boolean isRunAsJar = false;

    static
    {
        String webRootPath = EsToolDispatcher.class.getResource(WEB_ROOT).getPath();
        isRunAsJar = webRootPath.contains("jar/webroot");
    }
}
