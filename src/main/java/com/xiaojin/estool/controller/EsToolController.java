package com.xiaojin.estool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.xiaojin.estool.cache.ConfigCache;
import com.xiaojin.estool.constants.Constants;
import com.xiaojin.estool.entity.Data;
import com.xiaojin.estool.http.server.Response;
import com.xiaojin.estool.util.data.JsonUtil;
import com.xiaojin.estool.util.es.EsUtil;
import com.xiaojin.estool.util.http.HttpClient;
import com.xiaojin.estool.util.http.HttpClientLogCtrl;
import com.xiaojin.estool.util.io.IOUtil;
import com.xiaojin.estool.util.sql.SqlUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author lucene
 */
public class EsToolController
{
    private static final Logger logger = LoggerFactory.getLogger(EsToolController.class);

    public Response getConfig(String hostAddress, String clientAddress)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("hostAddress", hostAddress);
        map.put("esUrl", ConfigCache.get(clientAddress));
        String body = "{}";
        try
        {
            body = JsonUtil.writeValueAsString(map);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        return new Response().setContentType(Response.TYPE_JSON).setResponseBody(body);
    }

    public Response sqlQuery(Map<String, String> param)
    {
        String esUrl = param.get("esUrl");
        String queryType = param.get("queryType");
        String queryText = param.get("queryText");
        String separator = param.get("separator");
        String url = null;
        if ("sqlExplain".equals(queryType))
        {
            url = "http://" + esUrl + "/_sql/_explain";
        }
        else if ("sqlSearch".equals(queryType))
        {
            url = "http://" + esUrl + "/_sql";
        }
        else if ("sqlSearchFormat".equals(queryType))
        {
            url = "http://" + esUrl + "/_sql";
        }
        if (StringUtils.isNotEmpty(url))
        {
            HttpClient client = new HttpClient(url, 300000).setLogCtrl(HttpClientLogCtrl.REQUEST);
            try
            {
                queryText = filterComment(queryText); // 将查询文本中的注释内容过滤
                queryText = SqlUtil.removeBreakingWhitespace(queryText); // 格式化查询语句
                String result = client.doPost(queryText);
                if ("sqlSearchFormat".equals(queryType))
                {
                    return formatQueryResult(result, separator, queryText);
                }
                Data data = new Data(true, "", result);
                data.setTitle(extractTitle(result));
                Response response = new Response().setContentType(Response.TYPE_JSON).setResponseBody(data.toString());

                return response;
            }
            catch (Exception e)
            {
                logger.error(url, e);
                return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(false, "", e).toString());
            }
        }

        Response response = new Response().setContentType(Response.TYPE_JSON).setResponseBody(
                new Data(false, "", "Parameter [queryType] error.").toString());


        return response;
    }

    private String filterComment(String queryText) throws Exception
    {
        List<String> lines = IOUtil.toLines(queryText.getBytes(Constants.ENCODE.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (String line : lines)
        {
            if (line.startsWith("--"))
            {
                continue;
            }
            sb.append(line).append(" ");
        }
        return sb.toString();
    }

    public Response dslQuery(Map<String, String> param)
    {
        String esUrl = param.get("esUrl");
        String queryType = param.get("queryType");
        String queryIndex = param.get("indexText");
        String queryText = param.get("queryText");
        String separator = param.get("separator");
        String url = "http://" + esUrl + "/" + queryIndex + "/_search?preference=coship";
        HttpClient client = new HttpClient(url, 300000).setLogCtrl(HttpClientLogCtrl.URL_REQUEST);
        try
        {
            String result = client.doPost(queryText);
            if ("dslSearchFormat".equals(queryType))
            {
                return formatQueryResult(result, separator, null);
            }
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(true, "", result).toString());
        }
        catch (Exception e)
        {
            logger.error(url, e);
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(false, "", e).toString());
        }
    }

    public Response formatQueryResult(String result, String separator, String queryText) throws Exception
    {
        if ("\\t".equals(separator))
        {
            separator = "\t";
        }
        String formatResult = EsUtil.formatQueryResult(result, separator, queryText);
        return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(true, "", formatResult).toString());
    }

    private List<String> extractTitle(String content) throws Exception
    {
        List<String> titleList = new ArrayList();
        JsonNode root = JsonUtil.readTree(content);
        JsonNode hits = root.path("hits").path("hits");
        if (!hits.isMissingNode())
        {
            for (JsonNode node : hits)
            {
                JsonNode source = node.path("_source");
                Iterator<String> it = source.fieldNames();
                while (it.hasNext())
                {
                    String field = it.next();
                    if (!titleList.contains(field))
                    {
                        titleList.add(field);
                    }
                }
            }
        }
        return titleList;
    }

    public Response refreshIndex(Map<String, String> param)
    {
        String esUrl = param.get("esUrl");
        HttpClient client = new HttpClient("http://" + esUrl
                + "/_cat/indices?h=health,status,index,pri,rep,docs.count,docs.deleted,store.size");
        List<String[]> indexList = new ArrayList<String[]>();
        try
        {
            String result = client.doGet();
            List<String> lineList = IOUtil.toLines(result.getBytes());
            for (String line : lineList)
            {
                String[] idxStatus = StringUtils.split(line, " ");
                if (idxStatus.length == 8)
                {
                    indexList.add(idxStatus);
                }
                else
                {
                    if ("close".equals(idxStatus[0]))
                    {
                        indexList.add(new String[] { "", idxStatus[0], idxStatus[1], "", "", "", "", "" });
                    }
                    else
                    {
                        String[] defalutString = new String[] { "", "", "", "", "", "", "", "" };
                        for (int i = 0; i < idxStatus.length; i++)
                        {
                            defalutString[i] = idxStatus[i];
                        }
                    }
                }
            }

            Collections.sort(indexList, new Comparator<String[]>()
            {
                public int compare(String[] o1, String[] o2)
                {
                    if (o1 != null && o1.length >= 3 && o2 != null && o2.length >= 3)
                    {
                        return o1[2].compareTo(o2[2]);
                    }
                    return 0;
                }
            });
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(true, "", indexList).toString());
        }
        catch (Exception e)
        {
            logger.error("", e);
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(false, "", e).toString());
        }
    }

    public Response viewIndex(Map<String, String> param)
    {
        String esUrl = param.get("esUrl");
        String indexName = param.get("indexName");
        HttpClient client = new HttpClient("http://" + esUrl + "/" + indexName + "/_mapping");
        try
        {
            String result = client.doGet();
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(true, "", result).toString());
        }
        catch (Exception e)
        {
            logger.error("", e);
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(false, "", e).toString());
        }
    }

    public Response deleteIndex(Map<String, String> param)
    {
        String esUrl = param.get("esUrl");
        String indexName = param.get("indexName");
        HttpClient client = new HttpClient("http://" + esUrl + "/" + indexName, 60000);
        try
        {
            String result = client.doDelete();
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(true, "", result).toString());
        }
        catch (Exception e)
        {
            logger.error("", e);
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(false, "", e).toString());
        }
    }

    public Response refreshTemplate(Map<String, String> param)
    {
        String esUrl = param.get("esUrl");
        HttpClient client = new HttpClient("http://" + esUrl + "/_template");
        List<String> templateNameList = new ArrayList<String>();
        try
        {
            String resultJson = client.doGet();
            JsonNode node = JsonUtil.readTree(resultJson);
            for (Iterator<String> it = node.fieldNames(); it.hasNext();)
            {
                templateNameList.add(it.next());
            }
            Collections.sort(templateNameList);
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(true, "", templateNameList).toString());
        }
        catch (Exception e)
        {
            logger.error("", e);
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(false, "", e).toString());
        }
    }

    public Response viewTemplate(Map<String, String> param)
    {
        String esUrl = param.get("esUrl");
        String templateName = param.get("templateName");
        HttpClient client = new HttpClient("http://" + esUrl + "/_template/" + templateName);
        try
        {
            String result = client.doGet();
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(true, "", result).toString());
        }
        catch (Exception e)
        {
            logger.error("", e);
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(false, "", e).toString());
        }
    }

    public Response updateTemplate(Map<String, String> param)
    {
        String esUrl = param.get("esUrl");
        String templateName = param.get("templateName").trim();
        String queryText = param.get("queryText");
        HttpClient client = new HttpClient("http://" + esUrl + "/_template/" + templateName);
        try
        {
            String result = client.doPut(queryText);
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(true, "", result).toString());
        }
        catch (Exception e)
        {
            logger.error("", e);
            return new Response().setContentType(Response.TYPE_JSON).setResponseBody(new Data(false, "", e).toString());
        }
    }
}
