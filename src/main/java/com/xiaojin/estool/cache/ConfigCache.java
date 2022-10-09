package com.xiaojin.estool.cache;

import com.xiaojin.estool.util.io.FileWriteUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ConfigCache
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigCache.class);

    private static Map<String, String> config = new HashMap<String, String>();

    public static void put(String key, String value)
    {
        if (value == null)
        {
            return;
        }
        // 值发生了改变，则更新map，并写入文件
        String oldValue = config.get(key);
        if (!value.equals(oldValue))
        {
            config.put(key, value);

            File configFile = getConfigFile();
            FileWriteUtil util = new FileWriteUtil(configFile);
            for (Entry<String, String> entry : config.entrySet())
            {
                util.appendLine(entry.getKey() + "=" + entry.getValue());
            }
            util.flush();
            util.close();
        }
    }

    public static String get(String key)
    {
        return config.get(key);
    }

    public static void loadConfig()
    {
        try
        {
            File configFile = getConfigFile();
            if (configFile.exists())
            {
                List<String> lines = FileUtils.readLines(configFile, "UTF-8");
                for (String line : lines)
                {
                    String[] values = StringUtils.split(line, '=');
                    config.put(values[0], values[1]);
                }
            }
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
    }

    private static File getConfigFile()
    {
        File jarFile = new File(ConfigCache.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        File jarDir = jarFile.getParentFile();
        return new File(jarDir, "config");
    }

}
