package com.ouyeel.obfm.sql.dao.impl;


//import com.baosight.obmp.chain.sql.config.ChainConfig;
//import com.baosight.obmp.chain.sql.dao.IChainDao;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ouyeel.obfm.sql.config.ChainConfig;
import com.ouyeel.obfm.sql.dao.IChainDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChainDaoImpl implements IChainDao {
    final static Logger logger = LoggerFactory.getLogger(ChainDaoImpl.class);

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
        logger.info("insertTx start paramMap: [{}]", paramMap);
        try {
            int n = 1000;
            long start = System.currentTimeMillis();
            for (int i = 1; i <= n; ++i) {
                if (i % 100 == 0) {
                    double spendTime = (System.currentTimeMillis() - start) / 1000.0;
                    System.out.println(i + " time: " + spendTime + "s, tps: " + (i / spendTime));
                }
                paramMap.put(ChainConfig.REQUEST_SN, UUID.randomUUID().toString());
                Object result = sqlMapClient.insert("insertTx", paramMap);
            }
            double spendTime = (System.currentTimeMillis()- start) / 1000.0;
            System.out.println("total time: " + spendTime + "s, tps: " + (n / spendTime));
//            logger.info("result: [{}]", result);
        } catch (SQLException e) {
            logger.error("insertTx fail  [{}]",e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    public String queryTxHashByRequestSn(String tableName, String requestSn) {
        logger.debug("queryTxHashByRequestSn start");
        String txHash = null;
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ChainConfig.TABLE_NAME, tableName);
        paramMap.put(ChainConfig.REQUEST_SN, requestSn);
        logger.debug("queryTxHashByRequestSn...");
        try {
            List<Map<String, String>> list =
                    sqlMapClient.queryForList("queryTxHashByRequestSn", paramMap);
            Map<String, String> map = list.get(0);
            txHash = map.get(ChainConfig.TX_HASH);
            logger.debug("txHash: [{}]", txHash);
        } catch (SQLException e) {
            logger.debug("queryTxHashByRequestSn fail");
        }
        logger.debug("queryByRequestSn success");
        logger.debug("queryTxHashByRequestSn end");
        return txHash;
    }


    @Override
    public List<Map<String, String>> queryAllByTxHash(String tableName, String txHash) {
        logger.debug("queryAllByTxHash start");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ChainConfig.TABLE_NAME, tableName);
        paramMap.put(ChainConfig.TX_HASH, txHash);
        List<Map<String, String>> list;
        logger.debug("queryAllByTxHash...");
        try {
            list = sqlMapClient.queryForList("queryAllByTxHash", paramMap);
        } catch (SQLException e) {
            logger.debug("queryAllByTxHash fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
        logger.debug("queryAllByTxHash end");
        return list;
    }


    @Override
    public List<Map<String, String>> queryAllByRequestSn(String tableName, String requestSn) {
        logger.debug("queryAllByRequestSn start");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ChainConfig.TABLE_NAME, tableName);
        paramMap.put(ChainConfig.REQUEST_SN, requestSn);
        List<Map<String,String>> list;
        logger.debug("queryAllByRequestSn...");
        try {
            list = sqlMapClient.queryForList("queryAllByRequestSn", paramMap);
        } catch (SQLException e) {
            logger.debug("queryAllByRequestSn fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
        logger.debug("queryAllByRequestSn end");
        return list;
    }


    @Override
    public List<Map<String, String>> queryCallbackByRequestSn(String tableName, String requestSn) {
        logger.info("queryCallbackByRequestSn start");
        logger.info("queryCallbackByRequestSn tableName:["+tableName+"],requestSn:["+requestSn+"]");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ChainConfig.TABLE_NAME, tableName);
        paramMap.put(ChainConfig.REQUEST_SN, requestSn);
        List<Map<String, String>> list;
        try {
            logger.info("queryCallbackByRequestSn param : [{}]",paramMap);
            list = sqlMapClient.queryForList("queryCallbackByRequestSn", paramMap);
            logger.info("queryByTxHash result:[{}]",list);
        } catch (SQLException e) {
            logger.debug("queryByTxHash fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
        logger.info("queryByTxHash success");
        logger.debug("queryCallbackByRequestSn end");
        return list;
    }



    /**
     * 状态查询通用接口
     * @param tableName 数据表名
     * @param state 状态参数
     * @return 哈希
     */
    public String queryState(String tableName, String state) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ChainConfig.TABLE_NAME, tableName);
        paramMap.put(ChainConfig.STATE, state);
        List<Map<String, String>> list = null;
        logger.debug("paramMap: " + paramMap);
        try {
            list = sqlMapClient.queryForList("queryState", paramMap);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return null;
        }
        if (list == null) {
            return null;
        }
        Map<String, String> map = list.get(0);
        String message = map.get(ChainConfig.LOWERCASE_STATE);
        return message;
    }

    /**
     * 状态查询通用接口
     * @param tableName 数据表名
     * @param state 状态参数
     * @return 哈希
     */
    public String queryStateWithBlockHeight(String tableName, String state, String blockHeight) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ChainConfig.TABLE_NAME, tableName);
        paramMap.put(ChainConfig.STATE, state);
        paramMap.put(ChainConfig.BLOCK_HEIGHT, blockHeight);

        List<Map<String, String>> list = null;
        logger.debug("paramMap: " + paramMap);
        try {
            list = sqlMapClient.queryForList("queryStateWithBlockHeight", paramMap);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return null;
        }
        if (list == null) {
            return null;
        }
        Map<String, String> map = list.get(0);
        String message = map.get(ChainConfig.LOWERCASE_STATE);
        return message;
    }


    public void test() {
        try {
            sqlMapClient.queryForList("test");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
