package com.xiaojin.estool.util.io;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.xiaojin.estool.constants.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther dingjian
 * @Date 2021-01-04 8:23 下午
 */



public class IOUtil
{
    private static final Logger logger = LoggerFactory.getLogger(IOUtil.class);

    /**
     * 读取本地文件内容，默认编码：UTF-8
     * @param filePathUrl 本地文件的URL
     * @return 文件的内容
     * @throws Exception
     */
    public static String readFileToString(URL filePathUrl) throws Exception
    {
        return readFileToString(filePathUrl, "utf-8");
    }

    /**
     * 读取本地文件内容，默认编码：UTF-8
     * @param filePathUrl 本地文件的URL
     * @param charset 文本内容编码
     * @return 文件的内容
     * @throws Exception
     */
    public static String readFileToString(URL filePathUrl, String charset) throws Exception
    {
        if (filePathUrl != null)
        {
            return FileUtils.readFileToString(new File(filePathUrl.getPath()), charset);
        }
        return "";
    }

    /**
     * 读取输入流的内容<br/>
     * 由于输入流的内容读取完成后，没有 -1（EOF） 标识，需要通过读取的字节数与buffer大小不一致，来判断读取已完成
     */
    public static byte[] readByteFromSocket(InputStream input) throws Exception
    {
        int readLen = 0;
        byte[] inputByte = new byte[0];
        if (input == null)
        {
            return inputByte;
        }
        int buffSize = 2048;
        byte[] buffer = new byte[buffSize];
        // ByteArrayOutputStream bos = new ByteArrayOutputStream(); // 第二种实现方式
        while ((readLen = input.read(buffer)) != -1)
        {
            // logger.info("read size:" + readLen);
            // bos.write(buffer, 0, readLen);
            inputByte = arrayConcat(inputByte, buffer, readLen);
            if (readLen == buffSize || !checkReadOver(inputByte))
            {
                continue;
            }
            break;
        }
        // return bos.toByteArray();
        return inputByte;
    }

    /**
     * 检查tcp请求header中，Content-Length字段的值
     */
    private static int contentLength(byte[] inputByte)
    {
        List<String> lines = toLines(inputByte);
        if (lines == null)
        {
            return -1;
        }
        for (String line : lines)
        {
            if (line != null && line.startsWith("Content-Length: "))
            {
                return Integer.parseInt(line.substring("Content-Length: ".length()));
            }
        }
        return -1;
    }

    /**
     * 判断socket的输入流内容，是否读取结束
     */
    private static boolean checkReadOver(byte[] inputByte)
    {
        boolean hasHeaderSymbol = false;
        int i = 0;
        // 检查是否出现连续2个 \r\n，此为header结束的分隔符（必定存在），未出现，表示header未读取完毕
        for (; i < inputByte.length - 3; i++)
        {
            if (inputByte[i] == '\r' && inputByte[i + 1] == '\n' //
                    && inputByte[i + 2] == '\r' && inputByte[i + 3] == '\n')
            {
                hasHeaderSymbol = true;
                break;
            }
        }
        // 没有分隔符，表示输入流未读取结束
        if (!hasHeaderSymbol)
        {
            return false;
        }
        // 检查header中的Content-Length: 字段内容
        int contentLength = contentLength(inputByte);
        // 如果存在该字段
        if (contentLength > 0)
        {
            return inputByte.length - i - 4 == contentLength;
        }
        return true;
    }

    /**
     * 将输入流转为字符数组，按 换行 分隔
     * @throws Exception
     */
    public static List<String> readByteLinesFromSocket(InputStream input) throws Exception
    {
        byte[] inputByte = readByteFromSocket(input);
        // logger.info(ByteUtil.toHexString(inputByte));
        return toLines(inputByte);
    }

    /**
     * 将字节数组的内容，转为行数据
     * @param inputByte 字节数组
     * @return 按 \r\n 换行后的字符内容
     */
    public static List<String> toLines(byte[] inputByte)
    {
        List<String> result = new ArrayList<String>();
        int startIndex = 0;
        int endIndex = 0;
        boolean hasSperate = false;
        for (int i = 0, n = inputByte.length; i < n; i++)
        {
            if ('\r' == inputByte[i] || '\n' == inputByte[i])
            {
                endIndex = i;
                hasSperate = true;
                if (endIndex - startIndex > 0)
                {
                    result.add(new String(inputByte, startIndex, (endIndex - startIndex), Constants.CHARSET.UTF_8));
                }
            }
            if (hasSperate)
            {
                hasSperate = false;
                if (i + 1 < n && '\n' == inputByte[i + 1])// 检测到 \r，如果下一个字节是 \n，跳过
                {
                    i++;
                }
                startIndex = i + 1;
                continue;
            }
        }
        // 处理最后一行
        if (startIndex < inputByte.length)
        {
            result.add(new String(inputByte, startIndex, (inputByte.length - startIndex), Constants.CHARSET.UTF_8));
        }
        return result;
    }

    /**
     * 字节数组合并
     * @param a 合并的第一个数组
     * @param b 合并的第二个数组
     * @param len 合并的第二个数组的长度
     * @return 合并后的新数组
     */
    private static byte[] arrayConcat(byte[] a, byte[] b, int len)
    {
        byte[] result = new byte[a.length + len];
        if (a.length > 0)
        {
            System.arraycopy(a, 0, result, 0, a.length);
        }
        if (len > 0)
        {
            System.arraycopy(b, 0, result, a.length, len);
        }
        return result;
    }

    /**
     * 读取properties配置文件
     */
    public static Map<String, String> readProperties(String filePath)
    {
        return readProperties(filePath, "utf-8");
    }

    /**
     * 读取properties配置文件
     */
    public static Map<String, String> readProperties(String filePath, String charset)
    {
        Map<String, String> properties = new LinkedHashMap<String, String>();
        try
        {
            List<String> list = IOUtils.readLines(IOUtil.class.getResourceAsStream(filePath), charset);
            for (String text : list)
            {
                int equalIdx = text.indexOf("=");
                properties.put(text.substring(0, equalIdx), text.substring(equalIdx + 1));
            }
        }
        catch (Exception e)
        {
            logger.error("FilePath:" + filePath, e);
        }

        return properties;
    }

    /**
     * 将堆栈的内容，转为字符串
     */
    public static String getStackTrace(Throwable t)
    {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        t.printStackTrace(printWriter);
        return result.toString();
    }

}
