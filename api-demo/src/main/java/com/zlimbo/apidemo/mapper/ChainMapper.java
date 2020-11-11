package com.zlimbo.apidemo.mapper;

public interface ChainMapper {

    int upChain(String tableName, String privateKey, String systemId, String requestSn, String dataInfo);

}
