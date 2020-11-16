package com.zlimbo.zcat;

public class DataBaseArgs {

    public static final String DB_NAME = "ouyeel";

    public static final String URL =
            "jdbc:mysql://localhost:3306/" + DB_NAME +
                    "?useSSL=false" +
                    "&useUnicode=true" +
                    "&characterEncoding=UTF8" +
                    "&serverTimezone=GMT" +
                    "&allowPublicKeyRetrieval=true";
    //  Database credentials
    public static final String USER = "root";
    public static final String PASS = "123456";
}
