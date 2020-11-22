package com.zlimbo.bcweb.controller;

public class ConnectInfo {

    static final String DATABASE = "blockchainbase";

    static final String DB_URL =
            "jdbc:mysql://localhost:3306/" + DATABASE +
                    "?useSSL=false" +
                    "&useUnicode=true" +
                    "&characterEncoding=UTF8" +
                    "&serverTimezone=GMT" +
                    "&allowPublicKeyRetrieval=true";
    //  Database credentials
    static final String USER = "root";
    static final String PASSWORD = "123456";
}
