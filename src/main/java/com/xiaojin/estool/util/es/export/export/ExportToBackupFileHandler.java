package com.xiaojin.estool.util.es.export.export;

import com.fasterxml.jackson.databind.JsonNode;

public class ExportToBackupFileHandler extends EsExportToFileHandler
{
    @Override
    public String getSuffix()
    {
        return ".json";
    }

    @Override
    public void writeFile(JsonNode dataNode)
    {
        String value = dataNode.toString().replace("\r", "").replace("\n", "");
        fileWriteUtil.append(value).append("\r\n");
    }

}
