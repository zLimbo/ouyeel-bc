package com.zlimbo.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

public interface ChainMapper {

	void createTable(@Param("tableName") String tableName);

	int upChain(String tableName, String privateKey, String systemId, String requestSn, String dataInfo, String txHash);

	List<HashMap<String,Object>> verifyBxTxHash(String tableName, String systemId, String txHash);

	List<HashMap<String,Object>> compensateQuery(String tableName, String searchRequestSn);

	List<HashMap<String,Object>> queryPrivateKey(String privateKey, String systemId);

	void insertPrivateKey(String systemId, String privateKey);

}