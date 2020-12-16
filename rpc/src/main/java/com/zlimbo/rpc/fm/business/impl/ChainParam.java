package com.zlimbo.rpc.fm.business.impl;

import java.util.HashSet;
import java.util.Set;

/**
 * 相关参数设置
 */
public class ChainParam {

    final static String SYSTEM_ID = "SYSTEM_ID";
    final static String REQUEST_SN = "REQUEST_SN";
    final static String INVOKE_TIME = "INVOKE_TIME";
    final static String BUSINESS_ID = "BUSINESS_ID";
    final static String CALLBACK_URL = "CALLBACK_URL";
    final static String KEY_ID = "KEY_ID";
    final static String ACCOUNT_ID = "ACCOUNT_ID";
    final static String TX_HASH = "TX_HASH";
    final static String PRIVATE_KEY = "PRIVATE_KEY";
    final static String PUBLIC_KEY = "PUBLIC_KEY";
    final static String SECRET_KEY = "SECRET_KEY";
    final static String DATA_INFO = "DATA_INFO";
    final static String CONTRACT_ADDRESS = "CONTRACT_ADDRESS";
    final static String ON_CHAIN = "ON_CHAIN";
    final static String BLOCK_TIME = "BLOCK_TIME";
    final static String BLOCK_HEIGHT = "BLOCK_HEIGHT";

    final static String TABLE_NAME = "TABLE_NAME";
    final static String SEARCH_REQUEST_SN = "SEARCH_REQUEST_SN";
    final static String ON_CHAIN_SUCCESS = "1";
    final static String SUCCESS = "success";

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
            ChainParam.SECRET_KEY,
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
            add(ChainParam.SECRET_KEY);
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
}
