package com.ouyeel.obfm.fm.business.impl;

import com.alibaba.fastjson.JSONObject;
import com.ouyeel.obfm.fm.dao.ChainDao;
import com.ouyeel.obfm.fm.dao.ChainDaoOnlyJdbc;
import com.ouyeel.obfm.fm.dao.Dao;

import java.util.*;

/**
 * 相关参数设置
 */
public class ChainConfig {

//    final public static Dao CHAIN_DAO = new ChainDao();

    /**
     * 数据库字段
     */
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
    final public static String CODE = "CODE";
    final public static String MSG = "MSG";


    final public static String[] UP_CHAIN_PARAM = {
            ChainConfig.TABLE_NAME,
            ChainConfig.SYSTEM_ID,
            ChainConfig.REQUEST_SN,
            ChainConfig.INVOKE_TIME,
            ChainConfig.BUSINESS_ID,
            ChainConfig.CALLBACK_URL,
            ChainConfig.KEY_ID,
            ChainConfig.ACCOUNT_ID,
            ChainConfig.DATA_INFO,
            ChainConfig.SM4_KEY,
            ChainConfig.SM4_IV,
            ChainConfig.PRIVATE_KEY,
            ChainConfig.PUBLIC_KEY,
    };


    final public static Set<String> SYSTEM_PARAM = new HashSet() {
        {
            add(ChainConfig.SYSTEM_ID);
            add(ChainConfig.REQUEST_SN);
            add(ChainConfig.INVOKE_TIME);
            add(ChainConfig.BUSINESS_ID);
            add(ChainConfig.CALLBACK_URL);
            add(ChainConfig.KEY_ID);
            add(ChainConfig.ACCOUNT_ID);
            add(ChainConfig.TX_HASH);
            add(ChainConfig.PRIVATE_KEY);
            add(ChainConfig.PUBLIC_KEY);
            add(ChainConfig.SM4_KEY);
            add(ChainConfig.SM4_IV);
            add(ChainConfig.CONTRACT_ADDRESS);
            add(ChainConfig.ON_CHAIN);
            add(ChainConfig.BLOCK_TIME);
            add(ChainConfig.BLOCK_HEIGHT);
        }
    };


    final static int CORE_POOL_SIZE = 4;
    final static int MAXIMUM_POOL_SIZE = 8;
    final static int KEEP_ALIVET_TIME = 500;
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
    static public JSONObject smallHumpToUpperUnderline(Map<String, Object> inJson) {
        JSONObject outJson = new JSONObject();
        for (String key: inJson.keySet()) {
            Object value = inJson.get(key);
            if (value instanceof Map) {
                value = smallHumpToUpperUnderline((Map<String, Object>) value);
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
    static public JSONObject upperUnderlineToSmallHump(Map<String, Object> inJson) {
        JSONObject outJson = new JSONObject();
        for (String key: inJson.keySet()) {
            Object value = inJson.get(key);
            if (value instanceof Map) {
                value = upperUnderlineToSmallHump((Map)value);
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

    public static String Hex = "0123456789abcdef";

    // 返回指定的十六进制字符串
    public static String getHexString(int length) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        Random random = new Random();
        while (length-- != 0) {
            stringBuilder.append(Hex.charAt(random.nextInt(16)));
        }
        return stringBuilder.toString();
    }
}
