package com.xiaojin.estool.util.es;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.xiaojin.estool.util.data.JsonUtil;
import com.xiaojin.estool.util.date.DateUtil;
import com.xiaojin.estool.util.es.export.export.EsExportHandler;
import com.xiaojin.estool.util.http.HttpClient;
import com.xiaojin.estool.util.http.HttpClientLogCtrl;
import com.xiaojin.estool.util.sql.SqlUtil;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
/**
 * @Auther dingjian
 * @Date 2021-01-04 8:46 下午
 */



public class EsExportUtil
{
    // private static Logger logger = LoggerFactory.getLogger(EsExportUtil.class);

    // 例如：http://10.9.220.18:9200/
    private String esUrl;

    public EsExportUtil(String esUrl)
    {
        if (esUrl.endsWith("/"))
        {
            this.esUrl = esUrl;
        }
        else
        {
            this.esUrl = esUrl + "/";
        }
    }

    public void dataExportByDay(EsExportHandler handler) throws Exception
    {
        String querySql = handler.getSql();
        Map<String, String> sqlMetaDataList = parseSqlPerDay(querySql);
        for (Map.Entry<String, String> entry : sqlMetaDataList.entrySet())
        {
            String sql = entry.getValue();
            String dayFileNameKey = entry.getKey();
            // 按天数据，重新构造handle
            handler.setSql(sql);
            handler.setDayFileNameKey(dayFileNameKey);
            dataExport(handler);
        }
    }

    /**
     *   查询ES的SQL
     *   生成文件名的扩展，生成文件名的命名方式：文件保存目录/索引名称+文件名扩展
     *  是否导出为日志文件
     */
    public long dataExport(EsExportHandler handler) throws Exception
    {
        handler.init();
        // 将SQL格式化，去掉换行，多个空格等无用字符
        String querySql = handler.getSql();
        // 从SQL中提取索引名称
        String indexName = StringUtils.substringBetween(querySql, " from ", " ");
        // 记录请求结果的请求次数
        long reqCount = 0;
        try
        {
            long startTime = System.currentTimeMillis();
            // 通过查询语句，获取一个数据快照，返回快照的scrollId
            String scrollUrl = esUrl + indexName + "/_search?pretty&scroll=2m&size=100";
            // 根据快照的scrollId提取数据
            String queryUrl = esUrl + "_search/scroll?scroll=5m&pretty&scroll_id=";
            // 将sql语句，翻译成dsl语句
            String explainSql = explainQuerySql(querySql);

            // 通过查询条件，得到数据的快照scrollId
            HttpClient esScrollClient = new HttpClient(scrollUrl, 10000).setLogCtrl(HttpClientLogCtrl.URL_REQUEST);
            String esScrollResult = esScrollClient.doPost(explainSql);
            JsonNode root = JsonUtil.readTree(esScrollResult);

            String scrollId = root.path("_scroll_id").asText();
            //jsonNode.get("hits").as
            Set<String> scrollIdSet = new LinkedHashSet();
            scrollIdSet.add(scrollId);

            // 通过scrollId获取数据
            //while (StringUtils.isNotEmpty(scrollId) && scrollId.length() > 36)
            while (root.path("hits").path("hits").size()>0)
            {
                reqCount++; // 请求的次数+1
                handler.dataListProcess(root, reqCount);
                HttpClient queryClient = new HttpClient(queryUrl + scrollId, 100000).setLogCtrl(HttpClientLogCtrl.NONE);
                String queryResult = queryClient.doGet();
                root = JsonUtil.readTree(queryResult);

                // 每次提取数据后，都能得到下一次提取数据的scrollId，可以和上次的id相同，也可以不同
                scrollId = root.path("_scroll_id").asText();
                scrollIdSet.add(scrollId);
                // 快照中的总数据量
                long total = root.path("hits").path("total").asLong();

                if (reqCount % 10 == 0) // 每请求10次，打印一次日志
                {
                    logger(handler, "Exporting", handler.getAllCount(), total);
                }
                if (handler.getAllCount() == total) // 数据已经全部查询完，跳出循环
                {
                    logger(handler, "ExportOver", handler.getAllCount(), total);
                    break;
                }
            }

            for (String scrollIdTmp : scrollIdSet)
            {
                HttpClient deleteClient = new HttpClient(esUrl + "_search/scroll/" + scrollIdTmp, 10000);
                deleteClient.setLogCtrl(HttpClientLogCtrl.URL);
                deleteClient.doDelete();
            }
            long endTime = System.currentTimeMillis();
            String startTimeStr = DateUtil.formatPattenFull(new Date(startTime));
            String endTimeStr = DateUtil.formatPattenFull(new Date(endTime));
            String duration = DateUtil.parseDurationMillis(endTime - startTime);
            handler.logger(MessageFormat.format("ExportStartTime={0},ExportEndTime={1},CostTime={2}", startTimeStr, endTimeStr, duration));
        }
        catch (Exception e)
        {
            handler.logger(e);
            throw e;
        }
        finally
        {
            handler.close();
        }
        return handler.getAllCount();
    }

    private void logger(EsExportHandler handler, String message, long count, long total)
    {
        DecimalFormat f = new DecimalFormat("#,####");
        String logText = MessageFormat.format("[{0}]Wirte record :{1}/{2}", message, f.format(count), f.format(total));
        handler.logger(logText);
    }

    public String explainQuerySql(String sql) throws Exception
    {
        if (StringUtils.isNotEmpty(queryDsl))
        {
            return queryDsl;
        }
        // if (sql.contains(" where "))
        // {
        HttpClient explain = new HttpClient(esUrl + "_sql/_explain",30000).setLogCtrl(HttpClientLogCtrl.REQUEST);
        String explainSql = explain.doPost(sql);
        explainSql = SqlUtil.removeBreakingWhitespace(explainSql);

        //return explainSql.replace("\"from\":0,\"size\":1000", "");
        return explainSql.replace("\"from\" : 0, \"size\" : 200,", "");
        // }
        // 没有查询条件，默认查询所有记录
        // return "{\"query\":{\"match_all\":{}}}";
    }

    // 将sql的查询条件，按照天进行整理成多个SQL
    private Map<String, String> parseSqlPerDay(String sql)
    {
        Map<String, String> result = new LinkedHashMap();

        sql = SqlUtil.removeBreakingWhitespace(sql);
        String querySql = StringUtils.substringBefore(sql, " where ");
        String conditionSql = StringUtils.substringAfter(sql, " where ");
        String[] conditionArr = conditionSql.split(" and ");
        boolean hasBetweenCondition = false;
        String conditionName = null;
        for (String condition : conditionArr)
        {
            if (condition.contains(">") || condition.contains("<"))
            {
                if (conditionName == null)
                {
                    conditionName = condition.split(">|<")[0];
                }
                else if (condition.startsWith(conditionName))
                {
                    hasBetweenCondition = true;
                    break;
                }
            }
        }
        // 没有匹配的时间闭区间查询条件，则进行全量导出
        if (!hasBetweenCondition)
        {
            result.put("", sql);
            return result;
        }
        // 补全剩下的条件
        StringBuilder sb = new StringBuilder();
        for (String condition : conditionArr)
        {
            if (!condition.startsWith(conditionName))
            {
                sb.append(" and ").append(condition);
            }
        }

        if (hasBetweenCondition)
        {
            String beginCondition = null;
            String endCondition = null;
            String begin = null;
            String end = null;
            for (String condition : conditionArr)
            {
                if (condition.startsWith(conditionName))
                {
                    if (condition.contains(">"))
                    {
                        beginCondition = condition;
                        // 查找 > 后面，空格之后的参数值
                        begin = condition.substring(condition.indexOf(" ", condition.indexOf(">")) + 1);
                        // 去掉参数前后的引号
                        begin = begin.substring(1, begin.length() - 1);
                    }
                    if (condition.contains("<"))
                    {
                        endCondition = condition;
                        end = condition.substring(condition.indexOf(" ", condition.indexOf("<")) + 1);
                        end = end.substring(1, end.length() - 1);
                    }
                }
            }
            // 按日期进行整理
            Calendar beginCal = Calendar.getInstance();
            beginCal.setTime(DateUtil.parsePattenFull(begin));
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(DateUtil.parsePattenFull(end));
            // 中间参数
            Calendar middleCal = Calendar.getInstance();
            middleCal.setTimeInMillis(beginCal.getTimeInMillis());
            middleCal.set(Calendar.HOUR_OF_DAY, 0);
            middleCal.set(Calendar.MINUTE, 0);
            middleCal.set(Calendar.SECOND, 0);
            middleCal.set(Calendar.MILLISECOND, 0);
            middleCal.add(Calendar.DAY_OF_YEAR, 1);
            // 开始循环，中间变量初始化为开始时间第二天的0点
            // List<SqlMetaData> result = new ArrayList();

            int count = 0;
            while (middleCal.getTimeInMillis() < endCal.getTimeInMillis())
            {
                String newSql = querySql + " where ";
                if (count == 0) // 第一天的SQL
                {
                    newSql = newSql + beginCondition + " and " + conditionName + " < '" + DateUtil.formatPattenFull(middleCal) + "'"
                            + sb.toString();
                }
                else
                {
                    newSql = newSql + conditionName + " >= '" + DateUtil.formatPattenFull(beginCal) + "' and " + conditionName + " < '"
                            + DateUtil.formatPattenFull(middleCal) + "'" + sb.toString();
                }
                result.put(DateUtil.format(beginCal.getTime(), "_yyyyMMdd"), newSql);
                // 开始时间、结束时间往后移动1天
                beginCal.setTimeInMillis(middleCal.getTimeInMillis());
                middleCal.add(Calendar.DAY_OF_YEAR, 1);
                count++;
            }
            // 最后一天的SQL
            String lastSql = querySql + " where " + conditionName + " >= '" + DateUtil.formatPattenFull(beginCal) + "' and " + endCondition
                    + sb.toString();
            result.put(DateUtil.format(beginCal.getTime(), "_yyyyMMdd"), lastSql);
            return result;
        }
        // return Arrays.asList(new SqlMetaData("", sql));
        result.put("", sql);
        return result;
    }

    private String queryDsl;

    public void setQueryDsl(String queryDsl)
    {
        this.queryDsl = queryDsl;
    }

}
