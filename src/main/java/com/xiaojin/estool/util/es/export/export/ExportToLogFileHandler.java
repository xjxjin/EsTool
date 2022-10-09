package com.xiaojin.estool.util.es.export.export;

//import com.coship.base.constants.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.xiaojin.estool.constants.Constants;

public class ExportToLogFileHandler extends EsExportToFileHandler
{
    private String[] fieldNames;

    private String separator = Constants.SEPARATOR.FIELD_SEPARATOR;

    @Override
    public void init()
    {
        super.init();
        this.fieldNames = extratFileNames(sql);
    }

    @Override
    public String getSuffix()
    {
        return ".txt";
    }

    @Override
    public void writeFile(JsonNode dataNode)
    {
        JsonNode source = dataNode.path("_source");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldNames.length; i++)
        {
            if (i > 0)
            {
                sb.append(separator);
            }
            sb.append(source.path(fieldNames[i]).asText());
        }
        fileWriteUtil.append(sb.toString()).append("\r\n");
    }

    public void setSeparator(String separator)
    {
        this.separator = separator;
    }

}
