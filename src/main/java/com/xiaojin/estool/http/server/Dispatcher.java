package com.xiaojin.estool.http.server;

/**
 * @Auther dingjian
 * @Date 2021-01-04 8:38 下午
 */
public interface Dispatcher
{
    public Response getResponse(Request request);
}
