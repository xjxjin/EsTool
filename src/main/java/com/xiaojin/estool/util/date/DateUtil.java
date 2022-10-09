package com.xiaojin.estool.util.date;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.xiaojin.estool.exception.CommonException;
import org.apache.commons.lang3.StringUtils;

/**
 * @Auther dingjian
 * @Date 2021-01-04 8:25 下午
 */



public final class DateUtil
{
    // private static final Logger log = LoggerFactory.getLogger(DateUtil.class);

    public static final String dateRegex = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29-)) (20|21|22|23|[0-1]?\\d):[0-5]?\\d:[0-5]?\\d$";

    public static final String timeRegex = "([0-1]?[0-9]|2[0-3]):([0-5]?[0-9]):([0-5]?[0-9])";

    public static final String DATE_FORMAT_SIMPLE = "yyyyMMddHHmmss";

    public static final String DATE_FORMAT_SIMPLE_ZT = "yyyyMMddThhmmssZ";

    public static final String DATE_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss";

    public static final String DATE_FORMAT_SHORT = "yyyy-MM-dd";

    public static final int DATE_FORMAT_SHORT_LENGTH = DATE_FORMAT_SHORT.length();

    public static final String DATE_FORMAT_PROGRAM = "yyyy/MM/dd HH:mm";

    public static final int DATE_FORMAT_PROGRAM_LENGTH = DATE_FORMAT_PROGRAM.length();

    public static Date parse(String source, String format)
    {
        if (StringUtils.isEmpty(source))
        {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try
        {
            return sdf.parse(source);
        }
        catch (Exception e)
        {
            throw new CommonException("Prase date string failure,source=" + source, e);
        }
    }

    public static Date parsePattenFull(String source)
    {
        return parse(source, DATE_FORMAT_FULL);
    }

    public static Date parsePattenShort(String source)
    {
        return parse(source, DATE_FORMAT_SHORT);
    }

    public static Date parsePattenSimple(String source)
    {
        return parse(source, DATE_FORMAT_SIMPLE);
    }

    public static Date parsePattenProgram(String source)
    {
        return parse(source, DATE_FORMAT_PROGRAM);
    }

    public static String format(Date date, String format)
    {
        if (date == null)
        {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try
        {
            return sdf.format(date);
        }
        catch (Exception e)
        {
            throw new CommonException("Prase date string failure!" + date, e);
        }
    }

    public static String formatPattenFullZT(Date date)
    {
        return format(date, DATE_FORMAT_SIMPLE_ZT);
    }

    public static String formatPattenFull(Date date)
    {
        return format(date, DATE_FORMAT_FULL);
    }

    public static String formatPattenFull(Calendar cal)
    {
        if (cal == null)
        {
            return null;
        }
        return format(cal.getTime(), DATE_FORMAT_FULL);
    }

    public static String formatPattenShort(Date date)
    {
        return format(date, DATE_FORMAT_SHORT);
    }

    public static String formatPattenSimple(Date date)
    {
        return format(date, DATE_FORMAT_SIMPLE);
    }

    public static String formatPattenProgram(Date date)
    {
        return format(date, DATE_FORMAT_PROGRAM);
    }

    public static String simple2full(String source)
    {
        if (StringUtils.isEmpty(source))
        {
            return null;
        }
        if (source.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"))
        {
            return source;
        }
        Date d = parsePattenSimple(source);
        if (d != null)
        {
            return formatPattenFull(d);
        }
        return null;
    }

    /**
     * 时间相加
     * Calendar.DATE(天),Calendar.HOUR(小时)Calendar.MINUTE分钟,Calendar.SECOND 秒
     * @param date
     * @param calendar
     * @return
     */
    public static Date getDateAfter(Date date, int calendar, int number)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(calendar, number);
        return c.getTime();
    }

    /**
     * 将查询参数的时间字符串，转为标准格式的开始时间
     */
    public static String parseQueryTimeBegin(String queryDate)
    {
        if (StringUtils.isEmpty(queryDate))
        {
            return null;
        }
        int length = queryDate.length();
        // 处理月的格式
        if (length == "yyyy".length())
        {
            return queryDate.replace('/', '-') + "-01-01 00:00:00";
        }
        // 处理月的格式
        if (length == "yyyy-MM".length())
        {
            return queryDate.replace('/', '-') + "-01 00:00:00";
        }
        // 将yyyy/MM/dd格式变更为 yyyy-MM-dd格式
        if (length == DATE_FORMAT_SHORT_LENGTH)
        {
            return queryDate.replace('/', '-') + " 00:00:00";
        }
        if (length == DATE_FORMAT_PROGRAM_LENGTH)
        {
            return queryDate.replace('/', '-') + ":00";
        }
        return queryDate.replace('/', '-');
    }

    /**
     * 将查询参数的时间字符串，转为标准格式的结束时间
     */
    public static String parseQueryTimeEnd(String queryDate)
    {
        if (StringUtils.isEmpty(queryDate))
        {
            return null;
        }
        int length = queryDate.length();
        // 处理月的格式
        if (length == "yyyy-MM".length())
        {
            return queryDate.replace('/', '-') + "-01 00:00:00";
        }
        // 将yyyy/MM/dd格式变更为 yyyy-MM-dd格式
        if (length == DATE_FORMAT_SHORT_LENGTH)
        {
            return queryDate.replace('/', '-') + " 23:59:59";
        }
        if (length == DATE_FORMAT_PROGRAM_LENGTH)
        {
            return queryDate.replace('/', '-') + ":59";
        }
        return queryDate.replace('/', '-');
    }

    /**
     * 将查询参数的时间范围字符串，转为标准格式的开始、结束时间
     */
    public static String[] parseQueryTimeRange(String rangeDate)
    {
        if (StringUtils.isEmpty(rangeDate))
        {
            return null;
        }
        String[] result = new String[2];
        String[] dateStrArr = StringUtils.splitByWholeSeparator(rangeDate, " - ");
        if (dateStrArr != null && dateStrArr.length == 2)
        {
            result[0] = parseQueryTimeBegin(dateStrArr[0]);
            result[1] = parseQueryTimeEnd(dateStrArr[1]);
            return result;
        }
        return null;
    }

    /**
     * 将日期设置为当天的0点0分0秒
     */
    public static void setToDayFirst(Calendar cal)
    {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    /**
     * 将日期设置为当天的0点0分0秒
     */
    public static void setToWeekFirst(Calendar cal)
    {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    }

    /**
     * 将日期设置为本月1号，0点0分0秒
     */
    public static void setToMonthFirst(Calendar cal)
    {
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    /**
     * 将日期设置为下个月的1号，并将秒数减一秒，变成这个月的最后一个时刻
     */
    public static void setToMonthLast(Calendar cal)
    {
        setToMonthFirst(cal);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.SECOND, -1);
    }

    /**
     * 两个时间之间相差距离多少天
     * @param date1 时间参数 1：
     * @param date2 时间参数 2：
     * @return 相差天数
     */
    public static int getDistanceDays(Date date1, Date date2)
    {
        long time1 = date1.getTime();
        long time2 = date2.getTime();
        long diff;
        if (time1 < time2)
        {
            diff = time2 - time1;
        }
        else
        {
            diff = time1 - time2;
        }
        return (int) Math.ceil((double) diff / (1000 * 60 * 60 * 24));
    }

    /**
     * @param rangeDays
     * @param bool  12天以下是否分组
     * @return 每个刻度/抽取粒度
     */
    public static Long setCharDataTickInterval(int rangeDays, boolean bool, Long tickInterval)
    {
        long num = 1L;
        if (bool && rangeDays <= 1)
        {
            num = 1L;
        }
        else if (bool && rangeDays <= 3)
        {
            num = 3L;
        }
        else if (bool && rangeDays <= 6)
        {
            num = 6L;
        }
        else if (bool && rangeDays <= 12)
        {
            num = 12L;
        }
        else if (rangeDays <= 24)
        {
            num = 24L;
        }
        else if (rangeDays <= 48)
        {
            num = 48L;
        }
        else if (rangeDays <= 96)
        {
            num = 144L;
        }
        else if (rangeDays <= 192)
        {
            num = 288L;
        }
        else if (rangeDays > 192)
        {
            num = 720L;
        }
        if (num * 3600000L > tickInterval)
        {
            if (tickInterval < 10)
            {
                return 1L;
            }
            return tickInterval / 5;
        }
        return num * 3600000L;
    }

    public static String parseDurationMillis(long millis)
    {
        return parseDurationSecond(millis / 1000);
    }

    public static String parseDurationSecond(long seconds)
    {
        long hour = seconds / 3600;
        long minute = (seconds - hour * 3600) / 60;
        long second = seconds - hour * 3600 - minute * 60;
        StringBuilder sb = new StringBuilder();
        if (hour < 10)
        {
            sb.append("0");
        }
        sb.append(hour).append(":");
        if (minute < 10)
        {
            sb.append("0");
        }
        sb.append(minute).append(":");
        if (second < 10)
        {
            sb.append("0");
        }
        sb.append(second);
        return sb.toString();
    }

}
