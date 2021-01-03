package com.ouyeel.obfm.fm.dao;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ouyeel.obfm.fm.business.impl.ChainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChainDao implements Dao {
    final static Logger logger = LoggerFactory.getLogger(ChainDao.class);

    static long time = 0;
    static int count = 0;
    /**
     * ibatis 连接
     */
    static SqlMapClient sqlMapClient = null;

//    SqlMapClient
    static {
        logger.debug("ibatis SqlMapConfig ...");
        try {
            Reader reader = Resources.getResourceAsReader("SqlMapConfig.xml");
            sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
            reader.close();
            logger.debug("ibatis SqlMapConfig success");
        } catch (Exception e) {
            logger.debug("ibatis SqlMapConfig error");
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public SqlMapClient getSqlMapClient() {
        return sqlMapClient;
    }


    @Override
    public boolean insertTx(Map<String, String> paramMap) {
        logger.debug("paramMap: [{}]", paramMap);
        logger.debug("insertTx start");
        logger.debug(ChainConfig.DATA_INFO + ": " + paramMap.get(ChainConfig.DATA_INFO));
        try {
            ++count;
            long start = System.currentTimeMillis();
            Object insertResult =  sqlMapClient.insert("insertTx", paramMap);
            long end = System.currentTimeMillis();
            time += end - start;
            System.out.println("ChainDao: " + count + ": " + (end - start) / 1000.0 + " s" +
                    ", cur:" + 1 / ((end - start) / 1000.0) + " tps" +
                    ", sum:" + count / (time / 1000.0) + " tps ThreadName: " + Thread.currentThread().getName() +
                    " systemTime: " + System.currentTimeMillis());
            logger.debug("insertResult: [{}]", insertResult);
        } catch (SQLException e) {
            logger.debug("insertTx fail");
            logger.error(e.getMessage());
            logger.error(e.getCause().getMessage());
            e.printStackTrace();
            return false;
        }
        logger.debug("insertTx end");
        return true;
    }


    @Override
    public String queryTxHashByRequestSn(String tableName, String requestSn) {
        logger.debug("queryTxHashByRequestSn start");
        String txHash = null;
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put(ChainConfig.TABLE_NAME, tableName);
        queryParamMap.put(ChainConfig.REQUEST_SN, requestSn);
        logger.debug("queryTxHashByRequestSn...");
        try {
//            long start = System.currentTimeMillis();
//            List<Map<String, String>> queryResulList =
//                    sqlMapClient.queryForList("queryTxHashByRequestSn", queryParamMap);
//            long end = System.currentTimeMillis();
//            time += end - start;
//            System.out.println("ChainDao query txHash: " + count + ": " + (end - start) / 1000.0 + " s" +
//                    ", cur:" + 1 / ((end - start) / 1000.0) + " tps" +
//                    ", sum:" + count / (time / 1000.0) + " tps ThreadName: " + Thread.currentThread().getName() +
//                    " systemTime: " + System.currentTimeMillis());

            List<Map<String, String>> queryResulList =
                    sqlMapClient.queryForList("queryTxHashByRequestSn", queryParamMap);
            Map<String, String> queryResultMap = queryResulList.get(0);
            txHash = queryResultMap.get(ChainConfig.TX_HASH);
            logger.debug("txHash: [{}]", txHash);
        } catch (Exception e) {
            logger.error(e.getMessage());
            //e.printStackTrace();
            logger.debug("queryTxHashByRequestSn fail");
        }
        logger.debug("queryByRequestSn success");
        logger.debug("queryTxHashByRequestSn end");
        return txHash;
    }


    @Override
    public List<Map<String, String>> queryAllByTxHash(String tableName, String txHash) {
        logger.debug("queryAllByTxHash start");
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put(ChainConfig.TABLE_NAME, tableName);
        queryParamMap.put(ChainConfig.TX_HASH, txHash);
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


    @Override
    public List<Map<String, String>> queryAllByRequestSn(String tableName, String requestSn) {
        logger.debug("queryAllByRequestSn start");
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put(ChainConfig.TABLE_NAME, tableName);
        queryParamMap.put(ChainConfig.REQUEST_SN, requestSn);
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


    @Override
    public List<Map<String, String>> queryCallbackByRequestSn(String tableName, String requestSn) {
        logger.debug("queryCallbackByRequestSn start");
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put(ChainConfig.TABLE_NAME, tableName);
        queryParamMap.put(ChainConfig.REQUEST_SN, requestSn);
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
