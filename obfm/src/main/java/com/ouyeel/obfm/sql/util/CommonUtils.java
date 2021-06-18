package com.ouyeel.obfm.sql.util;

import com.alibaba.fastjson.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * 通用工具类
 */
public class CommonUtils {

    /**
     * 小驼峰转大写下划线
     * @param inString
     * @return
     */
    static public String smallHumpToUpperUnderline(String inString) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < inString.length(); ++i) {
            char c = inString.charAt(i);
            if ('A' <= c && c <= 'Z') {
                stringBuilder.append('_');
                stringBuilder.append(c);
            } else if ('a' <= c && c <= 'z'){
                stringBuilder.append((char) (c - 'a' + 'A'));
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }


    /**
     * json的键小驼峰转大写下划线
     * @param inJson
     * @return
     */
    static public JSONObject smallHumpToUpperUnderline(Map inJson) {
        JSONObject outJson = new JSONObject();
        for (Object key: inJson.keySet()) {
            Object value = inJson.get(key);
            if (value instanceof Map) {
                value = smallHumpToUpperUnderline((JSONObject) value);
            }
            String newKey = smallHumpToUpperUnderline((String) key);
            outJson.put(newKey, value);
        }
        return outJson;
    }


    /**
     * 大写下划线转小驼峰
     * @param inString
     * @return
     */
    static public String upperUnderlineToSmallHump(String inString) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean afterUnderline = false;
        for (int i = 0; i < inString.length(); ++i) {
            char c = inString.charAt(i);
            if (c == '_') {
                afterUnderline = true;
                continue;
            }
            if (afterUnderline) {
                stringBuilder.append(c);
                afterUnderline = false;
            } else {
                if ('A' <= c && c <= 'Z') {
                    stringBuilder.append((char) (c - 'A' + 'a'));
                } else {
                    stringBuilder.append(c);
                }
            }
        }
        return stringBuilder.toString();
    }


    /**
     * json的键大写下划线转小驼峰
     * @param inJson
     * @return
     */
    static public JSONObject upperUnderlineToSmallHump(Map inJson) {
        JSONObject outJson = new JSONObject();
        for (Object key: inJson.keySet()) {
            Object value = inJson.get(key);
            if (value instanceof Map) {
                value = upperUnderlineToSmallHump((JSONObject) value);
            }
            String newKey = upperUnderlineToSmallHump((String) key);
            outJson.put(newKey, value);
        }
        return outJson;
    }


    /**
     * 字符串中的大写字母变小写
     * @param key
     * @return
     */
    static public String lowerCase(String key) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < key.length(); ++i) {
            char c = key.charAt(i);
            if ('A' <= c && c <= 'Z') {
                c = (char) (c - 'A' + 'a');
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    /**
     * 时间戳转yyyy-MM-dd HH:mm:ss
     * @param time
     * @return
     */
    public static String longToDateString(String time)  {
        long longTime = Long.valueOf(time).longValue();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String string = sdf.format(longTime);
        return string;
    }
}
