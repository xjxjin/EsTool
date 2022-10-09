package com.xiaojin.estool.entity;


import com.xiaojin.estool.util.data.JsonUtil;
import com.xiaojin.estool.util.io.IOUtil;

import java.util.List;

public class Data
{
    private boolean success;

    private String msg;

    private Object data;

    private List<String> title;

    public Data()
    {
        this.success = false;
    }

    public Data(boolean success, String msg, Object data)
    {
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }

    public List<String> getTitle()
    {
        return title;
    }

    public void setTitle(List<String> title)
    {
        this.title = title;
    }

    @Override
    public String toString()
    {
        if (this.data instanceof Exception)
        {
            Exception e = (Exception) this.data;
            this.data = IOUtil.getStackTrace(e);
        }
        try
        {
            return JsonUtil.writeValueAsString(this);
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
    }
}
