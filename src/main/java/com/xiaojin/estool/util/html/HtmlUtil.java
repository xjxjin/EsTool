package com.xiaojin.estool.util.html;

import java.util.List;

import com.xiaojin.estool.constants.Constants;
import com.xiaojin.estool.util.collections.ListUtil;
import com.xiaojin.estool.util.io.IOUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @Auther dingjian
 * @Date 2021-01-04 8:43 下午
 */



public class HtmlUtil
{
    /**
     * 将带换行符的字符串，格式化为网页显示的内容
     */
    public static String escapeLineBreak(String source) throws Exception
    {
        List<String> lines = IOUtil.toLines(source.getBytes(Constants.ENCODE.UTF_8));
        return ListUtil.join(lines, "<br/>");
    }

    /**
     * 将异常信息，格式化为网页显示的内容
     */
    public static String escapeLineBreak(Throwable t) throws Exception
    {
        String text = IOUtil.getStackTrace(t);
        return escapeLineBreak(text);
    }

    /**
     * 删除文本内容中的换行符，并去掉前后空格
     * 用于页面上，文本输入框修改为文本域，但是又输入了换行符的情况
     */
    public static String removeBreakAndTrim(String original)
    {
        if (StringUtils.isEmpty(original))
        {
            return original;
        }
        final char[] chars = original.toCharArray();
        int pos = 0;
        for (int i = 0; i < chars.length; i++)
        {
            if (chars[i] == '\r' || chars[i] == '\n' || chars[i] == '\t')
            {
                continue;
            }
            chars[pos] = chars[i];
            pos++;
        }
        return StringUtils.trimToEmpty(new String(chars, 0, pos));
    }
}
