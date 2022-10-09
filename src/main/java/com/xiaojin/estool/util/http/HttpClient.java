package com.xiaojin.estool.util.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @Auther dingjian
 * @Date 2021-01-04 8:31 下午
 */



public class HttpClient
{
    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    private String requestUrl;

    private int timeout = 3000;

    private HttpClientLogCtrl logCtrl = HttpClientLogCtrl.ALL;

    private Map<String, String> head = new HashMap();

    protected HttpClient()
    {
    }

    public HttpClient(String requestUrl)
    {
        this.requestUrl = requestUrl;
    }

    public HttpClient(String requestUrl, int timeout)
    {
        this.requestUrl = requestUrl;
        this.timeout = timeout;
    }

    public HttpClient setRequestUrl(String requestUrl)
    {
        this.requestUrl = requestUrl;
        return this;
    }

    /**
     * 设置HttpClient的日志打印
     */
    public HttpClient setLogCtrl(HttpClientLogCtrl logCtrl)
    {
        this.logCtrl = logCtrl;
        return this;
    }

    /**
     * 添加请求使用的head参数
     * @param key
     * @param value
     */
    public HttpClient setRequestHead(String key, String value)
    {
        head.put(key, value);
        return this;
    }

    // 为connection设置请求参数
    private void setRequestHead(HttpURLConnection urlConnection)
    {
        if (head != null && !head.isEmpty())
        {
            for (Entry<String, String> entry : head.entrySet())
            {
                urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 以post形式发送http请求，默认编码为UTF-8
     * @param text
     * @throws Exception
     */
    public String doPost(String text) throws SocketTimeoutException, Exception
    {
        return doPost(text, ENCODING_UTF8);
    }

    public int doPost(String text, StringBuilder sb) throws SocketTimeoutException, Exception
    {
        return doRequest(REQUEST_POST, text, ENCODING_UTF8, sb);
    }

    /**
     * 以put形式发送http请求
     * @param text
     * @param encoding
     * @throws Exception
     */
    public String doPost(String text, String encoding) throws SocketTimeoutException, Exception
    {
        StringBuilder sb = new StringBuilder();
        doRequest(REQUEST_POST, text, encoding, sb);
        return sb.toString();
    }

    public int doPost(String text, String encoding, StringBuilder sb) throws SocketTimeoutException, Exception
    {
        return doRequest(REQUEST_POST, text, encoding, sb);
    }

    /**
     * 以put形式发送http请求，默认编码为UTF-8
     * @param text
     * @throws Exception
     */
    public String doPut(String text) throws SocketTimeoutException, Exception
    {
        return doPut(text, ENCODING_UTF8);
    }

    public int doPut(String text, StringBuilder sb) throws SocketTimeoutException, Exception
    {
        return doRequest(REQUEST_PUT, text, ENCODING_UTF8, sb);
    }

    /**
     * 以put形式发送http请求
     * @param text
     * @param encoding
     * @throws Exception
     */
    public String doPut(String text, String encoding) throws SocketTimeoutException, Exception
    {
        StringBuilder sb = new StringBuilder();
        doRequest(REQUEST_PUT, text, encoding, sb);
        return sb.toString();
    }

    public int doPut(String text, String encoding, StringBuilder sb) throws SocketTimeoutException, Exception
    {
        return doRequest(REQUEST_PUT, text, encoding, sb);
    }

    public String doRequest(String requestMethod, String text) throws SocketTimeoutException, Exception
    {
        return doRequest(requestMethod, text, ENCODING_UTF8);
    }

    public String doRequest(String requestMethod, String text, String encoding) throws SocketTimeoutException, Exception
    {
        StringBuilder sb = new StringBuilder();
        doRequest(requestMethod, text, encoding, sb);
        return sb.toString();
    }

    /**
     * 发送http请求
     * @param requestMethod 接口请求方法
     * @param text 发送消息内容(POST、PUT方法有效）
     * @param charset 字符集
     * @param sb 请求返回消息
     * @throws Exception
     */
    protected int doRequest(String requestMethod, String text, String charset, StringBuilder sb) throws SocketTimeoutException, Exception
    {
        HttpURLConnection urlConnection = null;
        OutputStreamWriter osw = null;
        BufferedReader reader = null;
        try
        {
            // 发送请求
            URL url = new URL(this.requestUrl);
            if (logCtrl.isLogUrl())
            {
                logger.debug("Method:" + requestMethod + ",Request Url:" + this.requestUrl);
            }
            urlConnection = (HttpURLConnection) url.openConnection();
            // 忽略ssl请求时，对hostname的验证
            if (urlConnection instanceof HttpsURLConnection)
            {
                ((HttpsURLConnection) urlConnection).setHostnameVerifier(new HostnameVerifierIgnore());
            }
            // 设置超时时间
            urlConnection.setConnectTimeout(this.timeout);
            urlConnection.setReadTimeout(this.timeout);
            // 设置请求方法
            urlConnection.setRequestMethod(requestMethod);
            // 不使用缓存
            urlConnection.setUseCaches(false);
            // 设置请求的head
            setRequestHead(urlConnection);
            // POST和PUT方法才有请求body的写入
            if (REQUEST_POST.equals(requestMethod) || REQUEST_PUT.equals(requestMethod))
            {
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                //urlConnection.setRequestProperty("Content-Type", "text/xml; charset=" + charset);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Connection", "close");
                osw = new OutputStreamWriter(urlConnection.getOutputStream(), charset);
                osw.write(text);
                osw.flush();
                if (logCtrl.isLogRequest())
                {
                    logger.debug("Request info:" + text);
                }
            }
            else
            {
                urlConnection.setRequestProperty("Connection", "close");
                urlConnection.connect();
            }
            int responseCode = urlConnection.getResponseCode();
            StringBuilder sbLog = new StringBuilder();
            // 读取请求返回值（200 OK，201 Created请求）
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED
                    || responseCode == HttpURLConnection.HTTP_ACCEPTED)
            {
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), charset));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line).append("\r\n");
                    sbLog.append(line);
                }
            }
            // 读取请求返回值（非200、201请求）
            else
            {
                InputStream errorStream = urlConnection.getErrorStream();
                if (errorStream != null)
                {
                    reader = new BufferedReader(new InputStreamReader(errorStream, charset));
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line).append("\r\n");
                        sbLog.append(line);
                    }
                    IOUtils.closeQuietly(errorStream);
                }
            }
            if (logCtrl.isLogResponse())
            {
                String response = sbLog.toString();
                logger.debug("Response info:" + response);
            }
            return responseCode;
        }
        finally
        {
            // 关闭连接
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
            IOUtils.closeQuietly(osw);
            IOUtils.closeQuietly(reader);
        }
    }

    public String doGet() throws SocketTimeoutException, Exception
    {
        return doGet(ENCODING_UTF8);
    }

    public int doGet(StringBuilder sb) throws SocketTimeoutException, Exception
    {
        return doGet(ENCODING_UTF8, sb);
    }

    public String doGet(String encoding) throws SocketTimeoutException, Exception
    {
        StringBuilder sb = new StringBuilder();
        doGet(encoding, sb);
        return sb.toString();
    }

    public int doGet(String encoding, StringBuilder sb) throws SocketTimeoutException, Exception
    {
        return doRequest(REQUEST_GET, null, encoding, sb);
    }

    public String doDelete() throws SocketTimeoutException, Exception
    {
        return doDelete(ENCODING_UTF8);
    }

    public String doDelete(String encoding) throws SocketTimeoutException, Exception
    {
        StringBuilder sb = new StringBuilder();
        doRequest(REQUEST_DELETE, null, encoding, sb);
        return sb.toString();
    }

    public String getRequestUrl()
    {
        return requestUrl;
    }

    public final static String ENCODING_UTF8 = "UTF-8";

    public final static String REQUEST_POST = "POST";

    public final static String REQUEST_PUT = "PUT";

    public final static String REQUEST_GET = "GET";

    public final static String REQUEST_DELETE = "DELETE";
}
