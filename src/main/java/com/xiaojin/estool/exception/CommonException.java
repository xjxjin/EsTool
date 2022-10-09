package com.xiaojin.estool.exception;

/**
 * @Auther dingjian
 * @Date 2021-01-04 8:26 下午
 */

public class CommonException extends RuntimeException
{
    private static final long serialVersionUID = 2988074186840784101L;

    private String errorCode;

    public CommonException(String message)
    {
        super(message);
    }

    public CommonException(Throwable cause)
    {
        super(cause);
    }

    public CommonException(String errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
    }

    public CommonException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CommonException setErrorCode(String errorCode)
    {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorCode()
    {
        return errorCode;
    }

}
