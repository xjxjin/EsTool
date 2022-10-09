package com.xiaojin.estool.http.server;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.xiaojin.estool.util.io.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @Auther dingjian
 * @Date 2021-01-04 8:23 下午
 */


public class Response
{
    private static final Logger logger = LoggerFactory.getLogger(Response.class);

    private String uri;

    private int httpStatus;

    private String contentType;

    private String responseBody;

    public Response()
    {
    }

    public Response(String uri)
    {
        this.uri = uri;
    }

    public Response setHttpStatus(int httpStatus)
    {
        this.httpStatus = httpStatus;
        return this;
    }

    public int getHttpStatus()
    {
        return this.httpStatus > 0 ? this.httpStatus : 200;
    }

    public Response setContentType(String contentType)
    {
        this.contentType = contentType;
        return this;
    }

    public String getContentType()
    {
        if (contentType != null && contentType.length() > 0)
        {
            return contentType;
        }
        String contentTypeTmp = null;
        String suffix = getSuffix();
        if (suffix != null && suffix.length() > 0)
        {
            contentTypeTmp = mimeType.get(suffix);
        }
        if (contentTypeTmp != null && contentTypeTmp.length() > 0)
        {
            return contentTypeTmp;
        }
        return TYPE_TEXT;
    }

    public Response setResponseBody(String responseBody)
    {
        this.responseBody = responseBody;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            sb.append("HTTP/1.1 ").append(getHttpStatus()).append("\r\n");
            sb.append("Content-Type: ").append(getContentType()).append("\r\n");
            sb.append("Content-Length: ").append(responseBody.getBytes("utf-8").length).append("\r\n").append("\r\n");
            sb.append(responseBody);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        return sb.toString();
    }

    public void write(OutputStream os)
    {
        try
        {
            /*byte[] b = toString().getBytes();
            int offset = 0;
            int buffLen = 1024;
            byte[] buff = new byte[buffLen];
            while (offset + buffLen < b.length)
            {
                System.arraycopy(b, offset, buff, 0, buffLen);
                os.write(buff);
                // os.flush();
                offset += buffLen;
            }
            if (offset < b.length)
            {
                buff = new byte[b.length - offset];
                System.arraycopy(b, offset, buff, 0, b.length - offset);
                os.write(buff);
            }*/
            os.write(toString().getBytes("utf-8"));
            os.flush();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
    }

    private String getSuffix()
    {
        if (uri != null && uri.length() > 0)
        {
            int lastSperateIdx = uri.lastIndexOf("/");
            int lastDotIdx = uri.lastIndexOf(".");
            if (lastDotIdx > lastSperateIdx)
            {
                return uri.substring(lastDotIdx);
            }
        }
        return "";
    }

    public static String TYPE_TEXT = "text/html;charset=UTF-8";

    public static String TYPE_XML = "application/xml;charset=UTF-8";

    public static String TYPE_JSON = "application/json;charset=UTF-8";

    public static Response NOT_FOUND = new Response();

    private static Map<String, String> mimeType = new HashMap<String, String>();

    static
    {
        NOT_FOUND.httpStatus = 404;
        NOT_FOUND.contentType = TYPE_TEXT;
        NOT_FOUND.responseBody = "<h1>File Not Found</h1>";
        mimeType = IOUtil.readProperties("/conf/mime.properties");
    }
}
