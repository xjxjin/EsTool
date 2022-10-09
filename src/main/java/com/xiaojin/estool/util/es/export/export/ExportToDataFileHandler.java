package com.xiaojin.estool.util.es.export.export;

import com.fasterxml.jackson.databind.JsonNode;

public class ExportToDataFileHandler extends EsExportToFileHandler
{

    @Override
    public String getSuffix()
    {
        return ".data";
    }

    @Override
    public void writeFile(JsonNode dataNode)
    {
        String value = dataNode.path("_source").toString().replace("\r", "").replace("\n", "");
        fileWriteUtil.append(value).append("\r\n");
    }

}
