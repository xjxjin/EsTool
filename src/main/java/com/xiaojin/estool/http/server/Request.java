package com.xiaojin.estool.http.server;


import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xiaojin.estool.constants.Constants;
import com.xiaojin.estool.util.io.IOUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @Auther dingjian
 * @Date 2021-01-04 8:39 下午
 */


public class Request
{
    private static final Logger logger = LoggerFactory.getLogger(Request.class);

    private InputStream input;

    private List<String> requestLines;

    private String requestMethod;

    private String requestUri;

    private InetAddress localAddress;

    private InetSocketAddress remoteAddress;

    private Map<String, String> param = new HashMap<String, String>();

    public Request(InputStream input)
    {
        this.input = input;
        parse();
    }

    // 从InputStream中读取request信息，并从request中获取uri值
    public void parse()
    {
        try
        {
            requestLines = IOUtil.readByteLinesFromSocket(input);
            if (requestLines.size() > 0)
            {
                parseUri(requestLines.get(0));
                parseBody(requestLines.get(requestLines.size() - 1));
            }
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        printRequestUri();
    }

    /**
     *
     * requestString形式如下：
     * GET /index.html HTTP/1.1
     * Host: localhost:8080
     * Connection: keep-alive
     * Cache-Control: max-age=0
     * ...
     * 该函数目的就是为了获取/index.html字符串
     */
    private void parseUri(String requestString)
    {
        String[] arr = requestString.split(" ");
        requestMethod = arr[0];
        if (StringUtils.isNotEmpty(arr[1]))
        {
            int idx = arr[1].indexOf("?");
            if (idx != -1)
            {
                requestUri = arr[1].substring(0, idx);
                parseParameter(arr[1].substring(idx + 1));
            }
            else
            {
                requestUri = arr[1];
            }
        }
    }

    private void parseBody(String requestString)
    {
        if ("POST".equals(requestMethod) && requestString != null && requestString.length() > 0)
        {
            parseParameter(requestString);
        }
    }

    private void parseParameter(String queryString)
    {
        String[] paramArr = queryString.split("&");
        for (String paramStr : paramArr)
        {
            String[] paramTmp = paramStr.split("=");
            param.put(decode(paramTmp[0]), paramTmp.length > 1 ? decode(paramTmp[1]) : "");
        }
    }

    private String decode(String str)
    {

        try
        {
            String decodeResult = URLDecoder.decode(str, "utf-8");
            return decodeResult;
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        return "";
    }

    public String getRequestMethod()
    {
        return requestMethod;
    }

    public String getRequestUri()
    {
        return requestUri;
    }

    public InetAddress getLocalAddress()
    {
        return localAddress;
    }

    public void setLocalAddress(InetAddress localAddress)
    {
        this.localAddress = localAddress;
    }

    public InetSocketAddress getRemoteAddress()
    {
        return remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress)
    {
        this.remoteAddress = remoteAddress;
    }

    public Map<String, String> getParameter()
    {
        return param;
    }

    private void printRequestUri()
    {
        if (requestLines != null && !requestLines.isEmpty())
        {
            logger.info(requestLines.get(0));
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (requestLines != null && !requestLines.isEmpty())
        {
            for (int i = 0, n = requestLines.size(); i < n; i++)
            {
                if (i > 0)
                {
                    sb.append(Constants.SEPARATOR.LINE_SEPARATOR);
                }
                sb.append(requestLines.get(i));
            }
        }
        return sb.toString();
    }

}
