package com.xiaojin.estool.util.es;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.xiaojin.estool.constants.Constants;
import com.xiaojin.estool.util.collections.ListUtil;
import com.xiaojin.estool.util.data.JsonUtil;
import com.xiaojin.estool.util.sql.SqlUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;
/**
 * @Auther dingjian
 * @Date 2021-01-04 8:28 下午
 */



public class EsUtil
{
    /**
     * @param hits ES返回的json文本结果中的hits.hits数据，一般为记录数据
     * @param separator 数据转文本，多字段之间使用的分隔符
     */
    public static String processHitsDataToString(JsonNode hits, String separator)
    {
        return processHitsDataToString(hits, separator, null);
    }

    /**
     *
     * @param hits ES返回的json文本结果中的hits.hits数据，一般为记录数据
     * @param separator 数据转文本，多字段之间使用的分隔符
     * @param querySql 使用的查询sql，主要用户获取用户查询数据字段，进行排版输出
     */
    public static String processHitsDataToString(JsonNode hits, String separator, String querySql)
    {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isEmpty(querySql))
        {
            querySql = "select * from index";
        }
        // 解析sql中的查询字段
        String columnNameStr = extractFieldStr(querySql);
        String[] fieldNameArr = null;
        if (!"*".equals(columnNameStr)) // 不是查询 * 请求
        {
            fieldNameArr = columnNameStr.split(",");
        }

        for (int i = 0; i < hits.size(); i++)
        {
            JsonNode source = hits.get(i).path("_source");
            if (i == 0)
            {
                // 如果是按照*查询，则解析第一条数据的字段名
                if (fieldNameArr == null)
                {
                    List<String> columnList = new ArrayList<String>();
                    Iterator<String> fieldNameIt = source.fieldNames();
                    while (fieldNameIt.hasNext())
                    {
                        String fieldName = fieldNameIt.next();
                        columnList.add(fieldName);
                    }
                    fieldNameArr = columnList.toArray(new String[columnList.size()]);
                }
                sb.append(ListUtil.join(fieldNameArr, separator)).append(Constants.SEPARATOR.LINE_SEPARATOR);
            }
            // 按字段提取数据
            for (int j = 0; j < fieldNameArr.length; j++)
            {
                String fieldName = fieldNameArr[j];
                if (j > 0)
                {
                    sb.append(separator);
                }
                Object value = extractJsonValue(source.path(fieldName));
                sb.append(valueToString(value));
            }
            sb.append(Constants.SEPARATOR.LINE_SEPARATOR);
        }
        return sb.toString();
    }

    private static String extractFieldStr(String querySql)
    {
        querySql = SqlUtil.removeBreakingWhitespace(querySql);
        String querySqlLower = querySql.toLowerCase();
        String open = "select ";
        int startIdx = querySqlLower.indexOf(open);
        int endIdx = querySqlLower.indexOf(" from ");
        String fieldNameStr = querySql.substring(startIdx + open.length(), endIdx);
        fieldNameStr = fieldNameStr.replaceAll(" ", "");
        return fieldNameStr;
    }

    /**
     * 从ES返回的json文本结果，提取明细记录
     */
    public static List<Map<String, Object>> processHitsData(String text) throws Exception
    {
        JsonNode root = JsonUtil.readTree(text);
        return processHitsData(root.path("hits").path("hits"));
    }

    /**
     * 从ES返回的json文本结果中的hits.hits数据，提取明细记录 ，不提取_id等字段
     */
    public static List<Map<String, Object>> processHitsData(JsonNode hits)
    {
        return processHitsData(hits, false);
    }

    /**
     * 从ES返回的json文本结果中的hits.hits数据，提取明细记录
     */
    public static List<Map<String, Object>> processHitsData(JsonNode hits, boolean containMetaData)
    {
        List<Map<String, Object>> mapList = new ArrayList();
        if (hits.isMissingNode())
        {
            return mapList;
        }
        for (int i = 0; i < hits.size(); i++)
        {
            Map<String, Object> map = processData(hits.get(i), containMetaData);
            mapList.add(map);
        }
        return mapList;
    }

    /**
     * 处理每一条ES记录，不提取_id等数据
     */
    public static Map<String, Object> processData(JsonNode data)
    {
        return processData(data, false);
    }

    /**
     * 处理每一条ES记录
     */
    public static Map<String, Object> processData(JsonNode data, boolean containMetaData)
    {
        JsonNode source = data.path("_source");
        Map<String, Object> map = new LinkedHashMap();
        Iterator<Map.Entry<String, JsonNode>> it = source.fields();
        while (it.hasNext())
        {
            Map.Entry<String, JsonNode> entry = it.next();
            map.put(entry.getKey(), extractJsonValue(entry.getValue()));
        }
        if (containMetaData)
        {
            map.put("_id", data.path("_id").asText());
        }
        return map;
    }

    /**
     * 将ES返回的文本数据，提取聚合结果
     */
    public static List<Map<String, Object>> processAggData(String text) throws Exception
    {
        JsonNode root = JsonUtil.readTree(text);
        return processAggData(root);
    }

    /**
     * 从ES返回的json文本结果中的aggregations数据，提取聚合结果
     */
    public static List<Map<String, Object>> processAggData(JsonNode root)
    {
        List<Map<String, Object>> mapList = new ArrayList();
        // aggregations 不存在，返回空
        JsonNode aggregations = root.path("aggregations");
        if (aggregations.isMissingNode())
        {
            return mapList;
        }
        boolean aggFlag = false; // 用于判断是否为简单聚合，即：没有buckets
        Map<String, Object> aggMap = new LinkedHashMap();
        Iterator<Map.Entry<String, JsonNode>> it = aggregations.fields();
        while (it.hasNext())
        {
            Map.Entry<String, JsonNode> entry = it.next();
            String keyName = entry.getKey();
            JsonNode buckets = entry.getValue().path("buckets");
            // group by 聚合
            if (!buckets.isMissingNode())
            {
                Map<String, Object> groupMap = new LinkedHashMap();
                processBuckets(keyName, buckets, mapList, groupMap);
            }
            // count、sum 聚合
            else
            {
                aggFlag = true;
                JsonNode value = entry.getValue().path("value");
                if (!value.isMissingNode())
                {
                    Object o = extractJsonValue(value);
                    aggMap.put(keyName, o);
                }
            }
        }
        if (aggFlag)
        {
            aggMap.put("doc_count", root.path("hits").path("total").asLong());
            mapList.add(aggMap);
        }
        return mapList;
    }

    /**
     * 递归处理聚合buckets结果
     */
    private static void processBuckets(String keyName, JsonNode buckets, List<Map<String, Object>> mapList, Map<String, Object> groupMap)
    {
        for (int i = 0; i < buckets.size(); i++)
        {
            // 将原有数据复制一份
            Map<String, Object> map = new LinkedHashMap();
            map.putAll(groupMap);
            // 增加新数据
            JsonNode bucket = buckets.get(i);
            Iterator<Map.Entry<String, JsonNode>> it = bucket.fields();
            boolean hasKeyAsString = false;
            boolean hasRecursion = false;
            while (it.hasNext())
            {
                Map.Entry<String, JsonNode> entry = it.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if ("key".equals(key))
                {
                    if (hasKeyAsString)
                    {
                        continue;
                    }
                    map.put(keyName, value.asText());
                }
                else if ("key_as_string".equals(key))
                {
                    hasKeyAsString = true;
                    map.put(keyName, value.asText());
                }
                else if ("doc_count".equals(key))
                {
                    map.remove("doc_count");
                    map.put("doc_count", value.asLong());
                }
                else if (value.isObject())
                {
                    JsonNode subBuckets = value.findValue("buckets");
                    if (subBuckets != null)
                    {
                        hasRecursion = true;
                        processBuckets(key, subBuckets, mapList, map);
                    }
                    else
                    {
                        JsonNode countValue = value.path("value");
                        if (!countValue.isMissingNode())
                        {
                            map.put(key, extractJsonValue(countValue));
                        }
                    }
                }
                else if (value.isValueNode())
                {
                    map.put(keyName, extractJsonValue(value));
                }
            }
            if (!hasRecursion)
            {
                // 将统计结果加入到List
                mapList.add(map);
            }
        }
    }

    /**
     * 从ES返回的json文本结果，提取聚合结果，并转为文本显示
     */
    public static String processAggDataToString(String text, String separator) throws Exception
    {
        JsonNode root = JsonUtil.readTree(text);
        return processAggDataToString(root, separator);
    }

    /**
     * 从ES返回的json文本结果中的aggregations数据，提取聚合结果，并转为文本显示
     */
    public static String processAggDataToString(JsonNode root, String separator)
    {
        StringBuilder sb = new StringBuilder();
        List<String> columnNameList = new ArrayList<String>();
        List<Map<String, Object>> result = processAggData(root);
        for (int i = 0, n = result.size(); i < n; i++)
        {
            Map<String, Object> data = result.get(i);
            // 第一行数据，提取标题
            if (i == 0)
            {
                Set<String> set = data.keySet();
                for (String key : set)
                {
                    columnNameList.add(key);
                }
                sb.append(ListUtil.join(columnNameList, separator)).append(Constants.SEPARATOR.LINE_SEPARATOR);
            }
            for (int j = 0; j < columnNameList.size(); j++)
            {
                String columnName = columnNameList.get(j);
                if (j > 0)
                {
                    sb.append(separator);
                }
                sb.append(valueToString(data.get(columnName)));
            }
            sb.append(Constants.SEPARATOR.LINE_SEPARATOR);
        }
        return sb.toString();
    }

    public static String formatQueryResult(String result, String separator) throws Exception
    {
        return formatQueryResult(result, separator, null);
    }

    public static String formatQueryResult(String result, String separator, String queryText) throws Exception
    {
        JsonNode root = JsonUtil.readTree(result);
        JsonNode hits = root.path("hits").path("hits");
        JsonNode aggregations = root.path("aggregations");
        if (!aggregations.isMissingNode())
        {
            return processAggDataToString(root, separator);
        }
        else if (!hits.isMissingNode() && hits.isArray())
        {
            return processHitsDataToString(hits, separator, queryText);
        }
        return null;
    }

    /**
     * 提取节点的值(按数据类型)
     */
    public static Object extractJsonValue(JsonNode node)
    {
        if (node instanceof ValueNode)
        {
            if (node instanceof TextNode)
            {
                return node.asText();
            }
            if (node instanceof BooleanNode)
            {
                return node.asBoolean();
            }
            if (node instanceof NumericNode)
            {
                if (node instanceof BigIntegerNode || node instanceof LongNode)
                {
                    return node.asLong();
                }
                if (node instanceof DoubleNode || node instanceof FloatNode)
                {
                    /**
                     * ES返回的long类型过大时，使用科学计数法表示，被系统识别为double类型
                     * 此时，统一将double类型，使用BigDecimal代替
                     */
                    return new BigDecimal(node.asText());
                }
                if (node instanceof IntNode)
                {
                    return node.asLong();
                }
            }
        }
        return node.asText();
    }

    private static String valueToString(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof BigDecimal)
        {
            return ((BigDecimal) value).toPlainString();
        }
        return value.toString();
    }

}
