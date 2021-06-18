package com.ouyeel.obfm.sql.config;

import java.util.HashSet;
import java.util.Set;

/**
 * 相关参数设置
 */
public class ChainConfig {

    /**
     * 数据库公共字段
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
    final public static String SEARCH_REQUEST_SN = "REQUEST_SN";
    final public static String ON_CHAIN_SUCCESS = "1";
    final public static String SUCCESS = "success";
    final public static String CODE = "CODE";
    final public static String MSG = "MSG";


    /**
     * 状态查询
     */
    final public static String STATE = "STATE";
    final public static String STATE_HASH = "STATE_HASH";
    final public static String COUNT = "COUNT";

    final public static String LOWERCASE_STATE = "state";
    final public static String QUERY_STATE_COUNT = "count_tx";
    final public static String QUERY_STATE_TIME = "time_tx";
    final public static String QUERY_STATE_MIX = "*";


    final public static int STATE_SINGLE_LEN = 64;
    final public static int STATE_COUNT_LEN = 2 + STATE_SINGLE_LEN;
    final public static int STATE_TIME_LEN = 2 + STATE_SINGLE_LEN * 3;
    final public static int STATE_MIX_LEN = 2 + STATE_SINGLE_LEN * 4;


    /**
     * 接口公共字段
     */
    final public static String LOWERCASE_TABLE_NAME = "tableName";
    final public static String LOWERCASE_SYSTEM_ID = "systemId";
    final public static String LOWERCASE_REQUEST_SN = "requestSn";
    final public static String LOWERCASE_INVOKE_TIME = "invokeTime";
    final public static String LOWERCASE_BUSINESS_ID = "businessId";
    final public static String LOWERCASE_CALLBACK_URL = "callbackUrl";
    final public static String LOWERCASE_KEY_ID = "keyId";
    final public static String LOWERCASE_ACCOUNT_ID = "accountId";
    final public static String LOWERCASE_DATA_INFO = "dataInfo";
    final public static String LOWERCASE_TX_HASH = "txHash";
    final public static String LOWERCASE_BLOCK_TIME = "txTime";
    final public static String LOWERCASE_BLOCK_HEIGHT = "blockNumber";


    final public static String CALLBACK_SERVICE_ID = "callbackServiceId";



    final public static String[] UP_CHAIN_PARAM = {
            ChainConfig.TABLE_NAME,
            ChainConfig.SYSTEM_ID,
            ChainConfig.REQUEST_SN,
            ChainConfig.INVOKE_TIME,
            ChainConfig.BUSINESS_ID,
            ChainConfig.CALLBACK_URL,
            ChainConfig.KEY_ID,
            ChainConfig.ACCOUNT_ID,
            ChainConfig.PRIVATE_KEY,
            ChainConfig.PUBLIC_KEY,
            ChainConfig.SM4_KEY,
            ChainConfig.SM4_IV,
            ChainConfig.DATA_INFO
    };

    final public static Set<String> UP_CHAIN_PARAM_LOWERCASE = new HashSet() {
        {
                    add(ChainConfig.LOWERCASE_SYSTEM_ID);
                    add(ChainConfig.LOWERCASE_REQUEST_SN);
                    add(ChainConfig.LOWERCASE_INVOKE_TIME);
                    add(ChainConfig.LOWERCASE_BUSINESS_ID);
                    add(ChainConfig.LOWERCASE_CALLBACK_URL);
                    add(ChainConfig.LOWERCASE_KEY_ID);
                    add(ChainConfig.LOWERCASE_ACCOUNT_ID);
                    add(ChainConfig.LOWERCASE_DATA_INFO);
        }
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

}
