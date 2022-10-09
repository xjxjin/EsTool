package com.xiaojin.estool.util.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;

public class FileWriteUtil
{
    private static final Logger logger = LoggerFactory.getLogger(FileWriteUtil.class);

    /**
     * BufferedWriter实例
     */
    private BufferedWriter bufferedWriter;

    private String filePath;

    public FileWriteUtil(String path)
    {
        this.filePath = path;
        createWriter(new File(path), false);
    }

    public FileWriteUtil(String path, boolean append)
    {
        this.filePath = path;
        createWriter(new File(path), append);
    }

    public FileWriteUtil(File file)
    {
        this.filePath = file.getAbsolutePath();
        createWriter(file, false);
    }

    public FileWriteUtil(File file, boolean append)
    {
        this.filePath = file.getAbsolutePath();
        createWriter(file, append);
    }

    private void createWriter(File file, boolean append)
    {
        try
        {
            file = file.getCanonicalFile();
            File parent = file.getParentFile();
            if (parent != null && !parent.exists())
            {
                parent.mkdirs();
            }
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), "utf-8"), 8 * 1024);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
    }

    public FileWriteUtil flush()
    {
        if (bufferedWriter != null)
        {
            try
            {
                bufferedWriter.flush();
            }
            catch (IOException e)
            {
                logger.error("", e);
            }
        }
        return this;
    }

    public FileWriteUtil append(String log)
    {
        try
        {
            bufferedWriter.write(log);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        return this;
    }

    public FileWriteUtil appendLine(String log)
    {
        try
        {
            bufferedWriter.write(log);
            bufferedWriter.write("\r\n");
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        return this;
    }

    public void close()
    {
        if (bufferedWriter != null)
        {
            try
            {
                bufferedWriter.flush();
                bufferedWriter.close();
            }
            catch (IOException e)
            {
                logger.error("", e);
            }
        }
    }

    public String getFilePath()
    {
        return filePath;
    }

}
