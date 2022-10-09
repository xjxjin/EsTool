package com.xiaojin.estool.util.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * @Auther dingjian
 * @Date 2021-01-04 8:33 下午
 */

/**
 * 忽略https请求使用IP地址访问时的ssl hostname验证异常
 */
public class HostnameVerifierIgnore implements HostnameVerifier
{
    @Override
    public boolean verify(String hostname, SSLSession session)
    {
        return true;
    }
}
