package com.xiaojin.estool.util.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import com.xiaojin.estool.util.date.DateUtil;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @Auther dingjian
 * @Date 2021-01-04 8:25 下午
 */


/**
 * json转换工具类
 */
public class JsonUtil
{
    // private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    public static String writeValueAsString(Object object)
    {
        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setTimeZone(TimeZone.getDefault());
            // 日期格式，转为yyyy-MM-dd HH:mm:ss格式字符串
            objectMapper.setDateFormat(new SimpleDateFormat(DateUtil.DATE_FORMAT_FULL));
            // BigDecimal类型，使用toPlainString方法转为文本
            objectMapper.configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
            return objectMapper.writeValueAsString(object);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String writeValueAsString(Object object, String dateFormat)
    {
        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setTimeZone(TimeZone.getDefault());
            // 日期格式，转为指定格式字符串
            objectMapper.setDateFormat(new SimpleDateFormat(dateFormat));
            // BigDecimal类型，使用toPlainString方法转为文本
            objectMapper.configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
            return objectMapper.writeValueAsString(object);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * {"name":"jone", "age":16}
     * 将字符串转为 Person对象
     * 使用方法：readValue(json, Person.class)
     */
    public static <T> T readValue(String json, Class<T> clazz) throws Exception
    {
        return readValue(getMapper(), json, clazz);
    }

    /**
     * 泛型支持
     *
     * {"name":"jone", "age":16, "address":{"country":"china"}}
     * 将 字符串 转为 Person 对象，其中Person类中，address为泛型类（Person<Address>)
     * 使用方法：readValue(json, Person.class, new Class[]{Address.class})
     *
     * [{"name":"jone", "age":16}]
     * 将字符串转为 Person对象集合
     * 使用方法：readValue(json, ArrayList.class, new Class[]{Person.class})
     */
    public static <T> T readValue(String json, Class<T> clazz, Class[] parameterClasses) throws Exception
    {
        ObjectMapper mapper = getMapper();
        JavaType javaType = mapper.getTypeFactory().constructParametricType(clazz, parameterClasses);
        return readValue(mapper, json, javaType);
    }

    /**
     * 将json中某一属性内容转为对象
     *
     * {"name":"jone", "age":16, "address":{"country":"china"}}
     * 将 address 转为 Address 对象(不使用泛型)
     * 使用方法：readValue(json, new String[]{"address"}, new Class[]{Address.class})
     */
    public static <T> T readValue(String json, String[] path, Class<T> clazz) throws Exception
    {
        ObjectMapper mapper = getMapper();
        String pathString = readString(mapper, json, path);
        if (StringUtils.isEmpty(pathString))
        {
            return null;
        }
        return readValue(mapper, pathString, clazz);
    }

    /**
     * 将json中某一属性内容转为对象，泛型支持
     *
     * {"test":{"name":"jone", "age":16, "address":{"country":"china"}}}
     * 将 字符串 中的test属性值，转为 Person 对象，其中Person类中，address为泛型类（Person<Address>)
     * 使用方法：readValue(json, new String[]{"test"}, Person.class, new Class[]{Address.class})
     */
    public static <T> T readValue(String json, String[] path, Class<T> clazz, Class[] parameterClasses) throws Exception
    {
        ObjectMapper mapper = getMapper();
        String pathString = readString(mapper, json, path);
        if (StringUtils.isEmpty(pathString))
        {
            return null;
        }
        JavaType javaType = mapper.getTypeFactory().constructParametricType(clazz, parameterClasses);
        return readValue(mapper, pathString, javaType);
    }

    /**
     * 泛型支持，返回结果为集合
     *
     * [{"name":"jone", "age":16, "address":{"country":"china"}}]
     * 将 字符串 转为 Person 集合
     * 使用方法：readValue(json, Person.class, new Class[]{Address.class})
     */
    public static <T> List<T> readValueList(String json, Class<T> clazz, Class[] parameterClasses) throws Exception
    {
        return readValueList(json, null, clazz, parameterClasses);
    }

    /**
     * 泛型支持，返回结果为集合
     *
     * {"test":[{"name":"jone", "age":16, "address":{"country":"china"}}]}
     * 将 字符串 中的test属性值，转为 Person 对象
     * 使用方法：readValue(json, new String[]{"test"}, Person.class, new Class[]{Address.class})
     */
    public static <T> List<T> readValueList(String json, String[] path, Class<T> clazz, Class[] parameterClasses) throws Exception
    {
        ObjectMapper mapper = getMapper();
        JavaType javaType = mapper.getTypeFactory().constructParametricType(clazz, parameterClasses);
        List<String> jsonList = readStringList(mapper, json, path);
        List<T> result = new ArrayList<T>();
        if (jsonList != null && !jsonList.isEmpty())
        {
            for (String str : jsonList)
            {
                result.add((T) readValue(mapper, str, javaType));
            }
        }
        return result;
    }

    public static <T> T readValue(ObjectMapper mapper, String json, JavaType javaType) throws Exception
    {
        if (StringUtils.isEmpty(json))
        {
            throw new RuntimeException("Json string is null");
        }
        return (T) mapper.readValue(json, javaType);
    }

    public static <T> T readValue(ObjectMapper mapper, String json, Class<T> clazz) throws Exception
    {
        if (StringUtils.isEmpty(json))
        {
            throw new RuntimeException("Json string is null");
        }
        return mapper.readValue(json, clazz);
    }

    public static JsonNode readTree(String json) throws Exception
    {
        if (StringUtils.isEmpty(json))
        {
            throw new RuntimeException("Json string is null");
        }
        return getMapper().readTree(json);
    }

    /**
     * 根据json的属性，得到对应的字符串
     */
    public static String readString(ObjectMapper mapper, String json, String[] path) throws Exception
    {
        List<String> list = readStringList(mapper, json, path);
        if (list != null && !list.isEmpty())
        {
            return list.get(0);
        }
        return null;
    }

    /**
     * 根据json的属性，得到对应的字符串集合
     */
    public static List<String> readStringList(ObjectMapper mapper, String json, String[] path) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        JsonNode node = mapper.readTree(json);
        if (path != null && path.length > 0)
        {
            for (String nodePath : path)
            {
                sb.append(nodePath).append("/");
                node = node.path(nodePath);
                if (node.isMissingNode())
                {
                    throw new RuntimeException("Json string is [" + json + "],Path [" + sb.toString() + "] not found");
                }
            }
        }
        if (node.isArray())
        {
            List<String> result = new ArrayList<String>();
            Iterator<JsonNode> it = node.iterator();
            while (it.hasNext())
            {
                JsonNode jsonNode = it.next();
                result.add(jsonNode.toString());
            }
            return result;
        }
        return Arrays.asList(node.toString());
    }

    public static ObjectMapper getMapper()
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getDefault());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 当json字符串中包含Java对象不存在的属性时，忽略此属性
        return mapper;
    }

}
