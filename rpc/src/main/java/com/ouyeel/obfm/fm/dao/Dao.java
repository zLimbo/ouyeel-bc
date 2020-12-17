package com.ouyeel.obfm.fm.dao;

import com.ouyeel.obfm.fm.business.impl.ChainParam;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Dao {


    boolean insertTx(Map<String, String> paramMap);

    String queryTxHashByRequestSn(String tableName, String requestSn);

    List<Map<String, String>> queryAllByTxHash(String tableName, String txHash);

    List<Map<String, String>> queryAllByRequestSn(String tableName, String requestSn);

    List<Map<String, String>> queryCallbackByRequestSn(String tableName, String requestSn);
}
