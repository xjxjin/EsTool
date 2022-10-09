package com.xiaojin.estool.entity;

import com.xiaojin.estool.util.date.DateUtil;

import java.io.File;
import java.util.Date;

public class FileVo implements Comparable<FileVo>
{
    private String fileName;

    private String updateTime = "";

    private boolean file;

    private String sortType;

    public FileVo()
    {
    }

    public FileVo(String fileName, boolean isFile)
    {
        this.fileName = fileName;
        this.file = isFile;
    }

    public FileVo(File file)
    {
        if (file != null)
        {
            this.fileName = file.getName();
            this.updateTime = DateUtil.formatPattenFull(new Date(file.lastModified()));
            this.file = file.isFile();
        }
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(String updateTime)
    {
        this.updateTime = updateTime;
    }

    public boolean isFile()
    {
        return file;
    }

    public void setFile(boolean file)
    {
        this.file = file;
    }

    public String getSortType()
    {
        return sortType;
    }

    public void setSortType(String sortType)
    {
        this.sortType = sortType;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("FileName=").append(fileName);
        sb.append(",updateTime=").append(updateTime);
        sb.append(",isFile=").append(file);
        return sb.toString();
    }

    @Override
    public int compareTo(FileVo o)
    {
        if ("updateTime".equals(sortType))
        {
            return o.updateTime.compareTo(updateTime);
        }
        return fileName.compareToIgnoreCase(o.fileName);
    }

}
