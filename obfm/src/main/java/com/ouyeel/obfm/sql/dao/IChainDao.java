package com.ouyeel.obfm.sql.dao;

import com.ibatis.sqlmap.client.SqlMapClient;

import java.util.List;
import java.util.Map;

public interface IChainDao {


    public SqlMapClient getSqlMapClient();

    /**
     * 根据 tableName 上链数据
     *
     * @param paramMap
     * @return
     */
    boolean insertTx(Map<String, String> paramMap);

    /**
     * 根据 requestSn 查询哈希值
     *
     * @param tableName
     * @param requestSn
     * @return
     */
    String queryTxHashByRequestSn(String tableName, String requestSn);

    /**
     * 根据 txHash 查询交易数据
     *
     * @param tableName
     * @param txHash
     * @return
     */
    List<Map<String, String>> queryAllByTxHash(String tableName, String txHash);

    /**
     * 根据 requestSn 查询交易数据
     *
     * @param tableName
     * @param requestSn
     * @return
     */
    List<Map<String, String>> queryAllByRequestSn(String tableName, String requestSn);

    /**
     * 回调
     *
     * @param tableName
     * @param requestSn
     * @return
     */
    List<Map<String, String>> queryCallbackByRequestSn(String tableName, String requestSn);


    /**
     * 状态查询
     *
     * @param tableName
     * @param state
     * @return
     */
    String queryState(String tableName, String state);

//    /**
//     * 通过智能合约查询交易数
//     * @param tableName
//     * @return
//     */
//    String queryState(String tableName);
//
//    /**
//     * 通过智能合约查询最近三次交易时间
//     * @param tableName
//     * @return
//     */
//    String queryLatestThreeTxDate(String tableName);
//
//    /**
//     * 通过智能合约下载交易数和最近三次交易时间
//     * @param tableName
//     * @return
//     */
//    String queryCountAndLatestThreeTxDate(String tableName);



}