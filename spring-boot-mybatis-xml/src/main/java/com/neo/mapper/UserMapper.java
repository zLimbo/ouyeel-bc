package com.neo.mapper;

public interface UserMapper {
	

	int upChain(String tableName, String privateKey, String systemId, String requestSn, String dataInfo);

	//int createTable(String tableName);
}