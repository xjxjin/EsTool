package com.xiaojin.estool.util.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * @Auther dingjian
 * @Date 2021-01-04 8:29 下午
 */


public class ListUtil
{

    /**
     * 获取List的第 index 个数据
     */
    public static <T> T get(List<T> list, int index)
    {
        if (list != null && index < list.size())
        {
            return list.get(index);
        }
        return null;
    }

    /**
     * 获取List的第一个数据
     */
    public static <T> T getFirst(List<T> list)
    {
        if (list != null && !list.isEmpty())
        {
            return list.get(0);
        }
        return null;
    }

    /**
     * 获取List的最后一个数据
     */
    public static <T> T getLast(List<T> list)
    {
        if (list != null && !list.isEmpty())
        {
            return list.get(list.size() - 1);
        }
        return null;
    }

    public static boolean isEmpty(List list)
    {
        return list == null || list.isEmpty();
    }

    public static boolean isNotEmpty(List list)
    {
        return list != null && !list.isEmpty();
    }

    /**
     * 获取List的第一个数据
     */
    public static <T> T getOne(List<T> list)
    {
        if (isNotEmpty(list))
        {
            return list.get(0);
        }
        return null;
    }

    public static String join(Iterable it, String separator)
    {
        if (it == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder("");
        boolean firstFlag = true;
        for (Object value : it)
        {
            if (firstFlag)
            {
                firstFlag = false;
            }
            else
            {
                sb.append(separator);
            }
            sb.append(value);
        }
        return sb.toString();
    }

    public static String join(Object[] arr, String separator)
    {
        if (arr == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder("");
        boolean firstFlag = true;
        for (Object value : arr)
        {
            if (firstFlag)
            {
                firstFlag = false;
            }
            else
            {
                sb.append(separator);
            }
            sb.append(value);
        }
        return sb.toString();
    }

    public static String joinKey(Object... o)
    {
        return join(Arrays.asList(o), "_");
    }

    /**
     * 将数组转为List
     * 区别与Arrays.asList方法，该方法创建的List，可以继续添加数据
     */
    public static <T> List<T> asList(T... a)
    {
        List<T> result = new ArrayList();
        if (a != null)
        {
            for (T e : a)
            {
                result.add(e);
            }
        }
        return result;
    }

    public static List<Integer> toList(int[] arr)
    {
        List<Integer> list = new ArrayList();
        if (arr != null)
        {
            for (int tmp : arr)
            {
                list.add(tmp);
            }
        }
        return list;
    }

    public static List<Long> toList(long[] arr)
    {
        List<Long> list = new ArrayList();
        if (arr != null)
        {
            for (long tmp : arr)
            {
                list.add(tmp);
            }
        }
        return list;
    }

    /**
     * 字符串数组,是否包含某个字符串
     */
    public static boolean contains(String[] arr, String key)
    {
        if (arr == null || key == null)
        {
            return false;
        }
        for (String str : arr)
        {
            if (key.equals(str))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 数组截取
     */
    public static <T> List<T> subList(List<T> parent, int fromIndex, int toIndex)
    {
        if (parent == null)
        {
            return null;
        }
        List<T> newList = new ArrayList();
        for (int i = fromIndex; i < parent.size() && i <= toIndex; i++)
        {
            newList.add(parent.get(i));
        }
        return newList;
    }

    /**
     * 取List的前offset个数据，返回新List
     */
    public static <T> List<T> subListFirst(List<T> list, int offset)
    {
        if (isEmpty(list))
        {
            return list;
        }
        List<T> result = new ArrayList();
        for (int i = 0, n = list.size(); i < n && offset > 0; i++)
        {
            result.add(list.get(i));
            offset--;
        }
        return result;
    }

    /**
     * 取List的后offset个数据，返回新List
     */
    public static <T> List<T> subListLast(List<T> list, int offset)
    {
        if (isEmpty(list))
        {
            return list;
        }
        List<T> result = new ArrayList();
        for (int i = list.size() - 1; i >= 0 && offset > 0; i--)
        {
            result.add(0, list.get(i));
            offset--;
        }
        return result;
    }
}
