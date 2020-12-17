package com.ouyeel.obfm.fm.business.impl;

import com.alibaba.fastjson.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * 相关参数设置
 */
public class ChainParam {


    final public static String SYSTEM_ID = "SYSTEM_ID";
    final public static String REQUEST_SN = "REQUEST_SN";
    final public static String INVOKE_TIME = "INVOKE_TIME";
    final public static String BUSINESS_ID = "BUSINESS_ID";
    final public static String CALLBACK_URL = "CALLBACK_URL";
    final public static String KEY_ID = "KEY_ID";
    final public static String ACCOUNT_ID = "ACCOUNT_ID";
    final public static String TX_HASH = "TX_HASH";
    final public static String PRIVATE_KEY = "PRIVATE_KEY";
    final public static String PUBLIC_KEY = "PUBLIC_KEY";
    final public static String SM4_KEY = "SM4_KEY";
    final public static String SM4_IV = "SM4_IV";
    final public static String DATA_INFO = "DATA_INFO";
    final public static String CONTRACT_ADDRESS = "CONTRACT_ADDRESS";
    final public static String ON_CHAIN = "ON_CHAIN";
    final public static String BLOCK_TIME = "BLOCK_TIME";
    final public static String BLOCK_HEIGHT = "BLOCK_HEIGHT";

    final public static String TABLE_NAME = "TABLE_NAME";
    final public static String SEARCH_REQUEST_SN = "SEARCH_REQUEST_SN";
    final public static String ON_CHAIN_SUCCESS = "1";
    final public static String SUCCESS = "success";

    final static String[] UP_CHAIN_PARAM = {
            ChainParam.TABLE_NAME,
            ChainParam.SYSTEM_ID,
            ChainParam.REQUEST_SN,
            ChainParam.INVOKE_TIME,
            ChainParam.BUSINESS_ID,
            ChainParam.CALLBACK_URL,
            ChainParam.KEY_ID,
            ChainParam.ACCOUNT_ID,
            ChainParam.PRIVATE_KEY,
            ChainParam.PUBLIC_KEY,
//            ChainParam.SECRET_KEY,
            ChainParam.SM4_KEY,
            ChainParam.SM4_IV,
            ChainParam.DATA_INFO
    };

    final static Set<String> SYSTEM_PARAM = new HashSet() {
        {
            add(ChainParam.SYSTEM_ID);
            add(ChainParam.REQUEST_SN);
            add(ChainParam.INVOKE_TIME);
            add(ChainParam.BUSINESS_ID);
            add(ChainParam.CALLBACK_URL);
            add(ChainParam.KEY_ID);
            add(ChainParam.ACCOUNT_ID);
            add(ChainParam.TX_HASH);
            add(ChainParam.PRIVATE_KEY);
            add(ChainParam.PUBLIC_KEY);
//            add(ChainParam.SECRET_KEY);
            add(ChainParam.SM4_KEY);
            add(ChainParam.SM4_IV);
            add(ChainParam.CONTRACT_ADDRESS);
            add(ChainParam.ON_CHAIN);
            add(ChainParam.BLOCK_TIME);
            add(ChainParam.BLOCK_HEIGHT);
        }
    };

    final static int CORE_POOL_SIZE = 5;
    final static int MAXIMUM_POOL_SIZE = 20;
    final static int KEEP_ALIVET_TIME = 1000;
    final static long[] CALL_BACK_TIMES = new long[]{ 1000L * 8, 1000L * 16, 1000L * 60};


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
    static public JSONObject smallHumpToUpperUnderline(JSONObject inJson) {
        JSONObject outJson = new JSONObject();
        for (String key: inJson.keySet()) {
            Object value = inJson.get(key);
            if (value instanceof JSONObject) {
                value = smallHumpToUpperUnderline((JSONObject) value);
            }
            String newKey = smallHumpToUpperUnderline(key);
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
    static public JSONObject upperUnderlineToSmallHump(JSONObject inJson) {
        JSONObject outJson = new JSONObject();
        for (String key: inJson.keySet()) {
            Object value = inJson.get(key);
            if (value instanceof JSONObject) {
                value = upperUnderlineToSmallHump((JSONObject) value);
            }
            String newKey = upperUnderlineToSmallHump(key);
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
}
