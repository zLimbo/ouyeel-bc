package com.zlimbo.chainservice.impl;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * sql
 */
public class SqlService {

    final static Logger logger = LoggerFactory.getLogger(SqlService.class);

    /**
     * ibatis 连接
     */
    static SqlMapClient sqlMapClient = null;


    static {
        logger.debug("ibatis SqlMapConfig ...");
        try {
            Reader reader = Resources.getResourceAsReader("SqlMapConfig.xml");
            sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
            reader.close();
            logger.debug("ibatis SqlMapConfig success");
        } catch (IOException e) {
            logger.debug("ibatis SqlMapConfig error");
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public static SqlMapClient getSqlMapClient() {
        return sqlMapClient;
    }

    /**
     * 上链
     */
    public static boolean insertTx(Map<String, String> paramMap) {
        logger.debug("insertTx start");
        logger.debug(ChainParam.DATA_INFO + ": " + paramMap.get(ChainParam.DATA_INFO));
        try {
            Object insertResult =  sqlMapClient.insert("insertTx", paramMap);
            logger.debug("insertResult: [{}]", insertResult);
        } catch (SQLException e) {
            logger.debug("insertTx fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        logger.debug("insertTx end");
        return true;
    }


    public static String queryTxHashByRequestSn(String tableName, String requestSn) {
        logger.debug("queryTxHashByRequestSn start");
        String txHash = null;
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put(ChainParam.TABLE_NAME, tableName);
        queryParamMap.put(ChainParam.REQUEST_SN, requestSn);
        logger.debug("queryTxHashByRequestSn...");
        try {
            List<Map<String, String>> queryResulList =
                    sqlMapClient.queryForList("queryTxHashByRequestSn", queryParamMap);
            Map<String, String> queryResultMap = queryResulList.get(0);
            txHash = queryResultMap.get(ChainParam.TX_HASH);
            logger.debug("txHash: [{}]", txHash);
        } catch (SQLException e) {
            logger.debug("queryTxHashByRequestSn fail");
        }
        logger.debug("queryByRequestSn success");
        logger.debug("queryTxHashByRequestSn end");
        return txHash;
    }


    public static List<Map<String, String>> queryAllByTxHash(String tableName, String txHash) {
        logger.debug("queryAllByTxHash start");
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put(ChainParam.TABLE_NAME, tableName);
        queryParamMap.put(ChainParam.TX_HASH, txHash);
        List<Map<String, String>> queryResulList = null;
        logger.debug("queryAllByTxHash...");
        try {
            queryResulList = sqlMapClient.queryForList("queryAllByTxHash", queryParamMap);
        } catch (SQLException e) {
            logger.debug("queryAllByTxHash fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
        logger.debug("queryAllByTxHash end");
        return queryResulList;
    }

    public static List<Map<String, String>> queryAllByRequestSn(String tableName, String requestSn) {
        logger.debug("queryAllByRequestSn start");
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put(ChainParam.TABLE_NAME, tableName);
        queryParamMap.put(ChainParam.REQUEST_SN, requestSn);
        List<Map<String,String>> queryResulList = null;
        logger.debug("queryAllByRequestSn...");
        try {
            queryResulList = sqlMapClient.queryForList("queryAllByRequestSn", queryParamMap);
        } catch (SQLException e) {
            logger.debug("queryAllByRequestSn fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
        logger.debug("queryAllByRequestSn end");
        return queryResulList;
    }

    public static List<Map<String, String>> queryCallbackByRequestSn(String tableName, String requestSn) {
        logger.debug("queryCallbackByRequestSn start");
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put(ChainParam.TABLE_NAME, tableName);
        queryParamMap.put(ChainParam.REQUEST_SN, requestSn);
        List<Map<String, String>> queryResulList = null;
                logger.debug("queryByTxHash...");
        try {
            queryResulList = sqlMapClient.queryForList("queryCallbackByRequestSn", queryParamMap);
        } catch (SQLException e) {
            logger.debug("queryByTxHash fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
        logger.debug("queryByTxHash success");
        logger.debug("queryCallbackByRequestSn end");
        return queryResulList;
    }


}
