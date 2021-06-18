package com.ouyeel.obfm.fm.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ouyeel.obfm.fm.business.impl.ChainConfig;
import com.ouyeel.obfm.fm.business.impl.ResponseCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class ChainDaoOnlyJdbc implements Dao{


//    @Value("${spring.datasource.url}")
//    private String url = "jdbc:mysql://localhost:3306/ouyeel?useSSL=false&useUnicode=true&characterEncoding=UTF8&serverTimezone=GMT&allowPublicKeyRetrieval=true";
    private String url = "jdbc:mysql://192.168.6.108:3100/ouyeel?useSSL=false&useUnicode=true&characterEncoding=UTF8&serverTimezone=GMT&allowPublicKeyRetrieval=true";
//    @Value("${spring.datasource.username}")
    private String username = "root";

//    @Value("${spring.datasource.password}")
    private String password = "admin";

//    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName = "com.mysql.jdbc.Driver";

    private List<Connection> connections = new ArrayList<>();

    private Integer size = 50;
    private Integer id = 0;

    static private ChainDaoOnlyJdbc chainDaoOnlyJdbc;

    private ChainDaoOnlyJdbc() {

    }

    static public ChainDaoOnlyJdbc getInstance() {
        if (chainDaoOnlyJdbc == null) {
            chainDaoOnlyJdbc = new ChainDaoOnlyJdbc();
            chainDaoOnlyJdbc.init();
        }
        return chainDaoOnlyJdbc;
    }

    public void init() {
        try {
            System.out.println("driverClassName: " + driverClassName);
            Class.forName(driverClassName);
            for (int i = 0; i < size; ++i) {
                connections.add(DriverManager.getConnection(url, username, password));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean insertTx(Map<String, String> paramMap) {

        paramMap.put(ChainConfig.ON_CHAIN, "1");
        paramMap.put(ChainConfig.TX_HASH, ChainConfig.getHexString(64));
        paramMap.put(ChainConfig.BLOCK_TIME, String.valueOf(System.currentTimeMillis()));
        paramMap.put(ChainConfig.BLOCK_HEIGHT, "0x54ef");
        String sql = "INSERT INTO " + paramMap.get(ChainConfig.TABLE_NAME) + " VALUES (" +
                "\'" + paramMap.get(ChainConfig.SYSTEM_ID) + "\', " +
                "\'" + paramMap.get(ChainConfig.REQUEST_SN) + "\', " +
                "\'" + paramMap.get(ChainConfig.INVOKE_TIME) + "\', " +
                "\'" + paramMap.get(ChainConfig.BUSINESS_ID) + "\', " +
                "\'" + paramMap.get(ChainConfig.CALLBACK_URL) + "\', " +
                "\'" + paramMap.get(ChainConfig.KEY_ID) + "\', " +
                "\'" + paramMap.get(ChainConfig.ACCOUNT_ID) + "\', " +
                "\'" + paramMap.get(ChainConfig.DATA_INFO) + "\', " +
                "\'" + paramMap.get(ChainConfig.SM4_KEY) + "\', " +
                "\'" + paramMap.get(ChainConfig.SM4_IV) + "\', " +
                "\'" + paramMap.get(ChainConfig.PRIVATE_KEY) + "\', " +
                "\'" + paramMap.get(ChainConfig.PUBLIC_KEY) + "\', " +
                "\'" + paramMap.get(ChainConfig.ON_CHAIN) + "\', " +
                "\'" + paramMap.get(ChainConfig.TX_HASH) + "\', " +
                "\'" + paramMap.get(ChainConfig.BLOCK_TIME) + "\', " +
                "\'" + paramMap.get(ChainConfig.BLOCK_HEIGHT) + "\')";

//        System.out.println("sql: " + sql);
        try {
            int id = Thread.currentThread().hashCode() % size;
            System.out.println("connection id: " + id);
            Statement statement = connections.get(id).createStatement();
            int result = statement.executeUpdate(sql);
            System.out.println(result);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public String queryTxHashByRequestSn(String tableName, String requestSn) {
        String sql = "SELECT TX_HASH FROM " + tableName + " WHERE REQUEST_SN = \'" + requestSn + "\' LIMIT 1";
        String txHash = null;
        try {
            int id = Thread.currentThread().hashCode() % size;
            System.out.println("connection id: " + id);
            Statement statement = connections.get(id).createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            resultSet.next();
            txHash = resultSet.getString(1);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        System.out.println("txHash: " + txHash);
        return txHash;
    }

    @Override
    public List<Map<String, String>> queryAllByTxHash(String tableName, String txHash) {
        return null;
    }

    @Override
    public List<Map<String, String>> queryAllByRequestSn(String tableName, String requestSn) {
        return null;
    }

    @Override
    public List<Map<String, String>> queryCallbackByRequestSn(String tableName, String requestSn) {
        return null;
    }

    @Override
    public SqlMapClient getSqlMapClient() {
        return null;
    }



}
