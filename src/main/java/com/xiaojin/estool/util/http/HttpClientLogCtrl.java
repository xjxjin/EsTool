package com.xiaojin.estool.util.http;

/**
 * @Auther dingjian
 * @Date 2021-01-04 8:32 下午
 */
public class HttpClientLogCtrl
{
    public static HttpClientLogCtrl NONE = new HttpClientLogCtrl(false, false, false);

    public static HttpClientLogCtrl URL = new HttpClientLogCtrl(true, false, false);

    public static HttpClientLogCtrl REQUEST = new HttpClientLogCtrl(false, true, false);

    public static HttpClientLogCtrl RESPONSE = new HttpClientLogCtrl(false, false, true);

    public static HttpClientLogCtrl URL_REQUEST = new HttpClientLogCtrl(true, true, false);

    public static HttpClientLogCtrl URL_RESPONSE = new HttpClientLogCtrl(true, false, true);

    public static HttpClientLogCtrl REQUEST_RESPONSE = new HttpClientLogCtrl(false, true, true);

    public static HttpClientLogCtrl ALL = new HttpClientLogCtrl(true, true, true);

    private boolean logUrl;

    private boolean logRequest;

    private boolean logResponse;

    public HttpClientLogCtrl(boolean logUrl, boolean logRequest, boolean logResponse)
    {
        this.logUrl = logUrl;
        this.logRequest = logRequest;
        this.logResponse = logResponse;
    }

    public boolean isLogUrl()
    {
        return logUrl;
    }

    public boolean isLogRequest()
    {
        return logRequest;
    }

    public boolean isLogResponse()
    {
        return logResponse;
    }

}
