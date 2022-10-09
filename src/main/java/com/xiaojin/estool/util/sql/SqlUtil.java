package com.xiaojin.estool.util.sql;

import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
/**
 * @Auther dingjian
 * @Date 2021-01-04 8:30 下午
 */



public class SqlUtil
{
    /**
     * 转义like语句中的'_' '%' 将'?'转成sql的'\_' 将'%'转成sql的'\%' 将'''转成sql的'\''
     */
    public static String escapeSQLLikeStr(String likeStr)
    {
        String temp = likeStr.replace("\\", "\\\\\\\\");
        temp = temp.replace("%", "\\%");
        temp = temp.replace("'", "\\'");
        return temp;
    }

    /**
     * 方法2，过滤掉所有的特殊字符，公共方法
     */
    public static String escapeSQLLikeStr2(String str)
    {
        StringBuffer buf = new StringBuffer();
        char xx = 92; // 92代表\
        if (str == null || "".equals(str))
        {
            return str;
        }
        for (int i = 0; i < str.length(); i++)
        {
            char s = str.charAt(i);
            if ((byte) s == 92) // "\" 在SQL中需要4个"\"
            {
                buf.append(xx);
                buf.append(xx);
                buf.append(xx);
                buf.append(xx);
            }
            else if ((byte) s == 37 || (byte) s == 39 || (byte) s == 42) // 37代表% , 39代表’ , 42代表*
            {
                buf.append(xx);
                buf.append(s);
            }
            else
            {
                buf.append(s);
            }
        }
        return buf.toString();
    }

    /**
     * 转换sql查询中，like参数的特殊字符（本方法仅针对绑定变量的sql使用）
     */
    public static String escapeSQLLikeVariable(String likeStr)
    {
        if (StringUtils.isEmpty(likeStr))
        {
            return likeStr;
        }
        String temp = likeStr;
        // temp = temp.replace("\\", "\\\\\\\\");
        temp = temp.replace("_", "\\_");
        temp = temp.replace("%", "\\%");
        return "%" + temp + "%";
    }

    /**
     * 转换es sql查询中，like参数的特殊字符（本方法仅针对绑定变量的es sql使用）
     */
    public static String escapeEsSQLLikeVariable(String likeStr)
    {
        if (StringUtils.isEmpty(likeStr))
        {
            return likeStr;
        }
        String temp = likeStr;
        // temp = temp.replace("\\", "\\\\\\\\");
        temp = temp.replace("_", "\\_");
        temp = temp.replace("*", "\\*");
        return "*" + temp + "*";
    }

    /**
     * 转换sql查询中，like参数的特殊字符（本方法仅针对绑定变量的sql使用）
     */
    public static String escapeSQLRightLikeVariable(String likeStr)
    {
        String temp = escapeSQLLikeVariable(likeStr);
        if (!StringUtils.isEmpty(temp))
        {
            return temp.substring(1);
        }
        return likeStr;
    }

    /**
     * 去掉sql语句中的换行、多余的空格，格式化打印的sql语句
     */
    public static String removeBreakingWhitespace(String original)
    {
        StringTokenizer whitespaceStripper = new StringTokenizer(original);
        StringBuilder builder = new StringBuilder();
        while (whitespaceStripper.hasMoreTokens())
        {
            builder.append(whitespaceStripper.nextToken());
            builder.append(" ");
        }
        return builder.toString();
    }

    public static String escapeStringParamIn(String[] params)
    {
        return escapeParamIn(params, "'");
    }

    public static String escapeIntParamIn(String[] params)
    {
        return escapeParamIn(params, "");
    }

    /**
     * 将数组参数的值，转为sql中，in查询的参数
     */
    private static String escapeParamIn(String[] params, String quote)
    {
        if (params == null || params.length == 0)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder("");
        boolean firstFlag = true;
        for (String value : params)
        {
            if (StringUtils.isEmpty(value))
            {
                continue;
            }
            if (firstFlag)
            {
                firstFlag = false;
            }
            else
            {
                sb.append(",");
            }
            sb.append(quote).append(value).append(quote);
        }
        return sb.toString();
    }

    public static void main(String[] args)
    {
        String s = "中文字符a'aa\\s特殊_s%b%b'cc";
        System.out.println(escapeSQLLikeVariable(s));

    }

}
