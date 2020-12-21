package com.zlimbo.bcweb.controller;

import com.alibaba.fastjson.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * 相关参数设置
 */
public class ChainParam {


    final public static String SYSTEM_ID = "systemId";
    final public static String REQUEST_SN = "requestSn";
    final public static String INVOKE_TIME = "invokeTime";
    final public static String BUSINESS_ID = "businessId";
    final public static String CALLBACK_URL = "callbackUrl";
    final public static String KEY_ID = "keyId";
    final public static String ACCOUNT_ID = "accountId";
    final public static String TX_HASH = "txHash";
//    final public static String PRIVATE_KEY = "privateKey";
//    final public static String PUBLIC_KEY = "publicKey";
//    final public static String SM4_KEY = "sm4Key";
//    final public static String SM4_IV = "sm4Iv";
    final public static String DATA_INFO = "dataInfo";
    final public static String CONTRACT_ADDRESS = "contractAddress";
    final public static String ON_CHAIN = "onChain";
    final public static String BLOCK_TIME = "blockTime";
    final public static String BLOCK_HEIGHT = "blockHeight";
    final public static String TABLE_NAME = "tableName";
    final public static String SEARCH_REQUEST_SN = "searchRequestSn";
    final public static String ON_CHAIN_SUCCESS = "onChainSuccess";
    final public static String SUCCESS = "success";
    final public static String CODE = "code";
    final public static String MSG = "msg";

    final public static String[] UP_CHAIN_PARAM = {
            ChainParam.TABLE_NAME,
            ChainParam.SYSTEM_ID,
            ChainParam.REQUEST_SN,
            ChainParam.INVOKE_TIME,
            ChainParam.BUSINESS_ID,
            ChainParam.CALLBACK_URL,
            ChainParam.KEY_ID,
            ChainParam.ACCOUNT_ID,
//            ChainParam.PRIVATE_KEY,
//            ChainParam.PUBLIC_KEY,
//            ChainParam.SM4_KEY,
//            ChainParam.SM4_IV,
            ChainParam.DATA_INFO
    };

    final public static Set<String> SYSTEM_PARAM = new HashSet() {
        {
            add(ChainParam.SYSTEM_ID);
            add(ChainParam.REQUEST_SN);
            add(ChainParam.INVOKE_TIME);
            add(ChainParam.BUSINESS_ID);
            add(ChainParam.CALLBACK_URL);
            add(ChainParam.KEY_ID);
            add(ChainParam.ACCOUNT_ID);
            add(ChainParam.TX_HASH);
//            add(ChainParam.PRIVATE_KEY);
//            add(ChainParam.PUBLIC_KEY);
//            add(ChainParam.SM4_KEY);
//            add(ChainParam.SM4_IV);
            add(ChainParam.CONTRACT_ADDRESS);
            add(ChainParam.ON_CHAIN);
            add(ChainParam.BLOCK_TIME);
            add(ChainParam.BLOCK_HEIGHT);
        }
    };



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
