package com.zlimbo.bcweb.domain;

public class Contract {

    private String tableName;
    private String bytecode;

    public Contract() {

    }

    public Contract(String tableName, String bytecode) {
        this.tableName = tableName;
        this.bytecode = bytecode;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getBytecode() {
        return bytecode;
    }

    public void setBytecode(String bytecode) {
        this.bytecode = bytecode;
    }
}
