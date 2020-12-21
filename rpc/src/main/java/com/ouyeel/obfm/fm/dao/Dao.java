package com.ouyeel.obfm.fm.dao;

import com.ibatis.sqlmap.client.SqlMapClient;

import java.util.List;
import java.util.Map;

public interface Dao {


    /**
     * 根据 tableName 上链数据
     * @param paramMap
     * @return
     */
    boolean insertTx(Map<String, String> paramMap);

    /**
     * 根据 requestSn 查询哈希值
     * @param tableName
     * @param requestSn
     * @return
     */
    String queryTxHashByRequestSn(String tableName, String requestSn);

    /**
     * 根据 txHash 查询交易数据
     * @param tableName
     * @param txHash
     * @return
     */
    List<Map<String, String>> queryAllByTxHash(String tableName, String txHash);

    /**
     * 根据 requestSn 查询交易数据
     * @param tableName
     * @param requestSn
     * @return
     */
    List<Map<String, String>> queryAllByRequestSn(String tableName, String requestSn);

    /**
     * 回调
     * @param tableName
     * @param requestSn
     * @return
     */
    List<Map<String, String>> queryCallbackByRequestSn(String tableName, String requestSn);


    SqlMapClient getSqlMapClient();
}
