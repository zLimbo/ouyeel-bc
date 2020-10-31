package com.zlimbo.bc.controller;

import com.zlimbo.bc.DataBaseArgs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class SqlController {


    Connection connection = null;

    Connection getConnection() {
        return connection;
    }


    SqlController(String dbName, String user, String passwd) {
        System.out.println("====================> [SqlControl] start");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(dbName, user, passwd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("====================> [sqlQueryAndShow] end\n");
    }


    public List<String> sqlShowTables() {
        List<String> tables = new ArrayList<>();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("show tables");
            while (resultSet.next()) {
                System.out.println("====> table: " + resultSet.getString(1));
                tables.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return tables;
    }

    
    public static class SqlQueryResult {
        private final List<String> columns;
        private final List<List<String>> records;
        private final String errorMessage;
        private final long spendTime;

        public SqlQueryResult(List<String> columns, List<List<String>> records, long spendTime, String errorMessage) {
            this.columns = columns;
            this.records = records;
            this.spendTime = spendTime;
            this.errorMessage = errorMessage;
        }

        public List<String> getColumns() {
            return columns;
        }
        
        public List<List<String>> getRecords() {
            return records;
        }

        public long getSpendTime() {
            return spendTime;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }


    public SqlQueryResult sqlQuery(String sql) {
        System.out.println("====================> [sqlQuery] start");

        long start = System.currentTimeMillis();

        String errorMessage = null;
        List<String> columns = new ArrayList<>();
        List<List<String>> records = new ArrayList<>();
        Statement statement = null;
        try {
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            connection = DriverManager.getConnection(DataBaseArgs.URL, DataBaseArgs.USER, DataBaseArgs.PASS);
            System.out.println("Connected database successfully...");

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            statement = connection.createStatement();

            System.out.println("====> query sql: " + sql);
            ResultSet resultSet = statement.executeQuery(sql);

            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();

            for (int i = 0; i < columnCount; ++i) {
                String columnName = resultSetMetaData.getColumnName(i + 1);
                columns.add(columnName);
            }

            while (resultSet.next()) {
                List<String> record = new ArrayList<String>();
                for (int i = 0; i < columnCount; ++i) {
                    record.add(resultSet.getString(i + 1));
                }
                records.add(record);
            }
            resultSet.close();
        } catch (Exception e) {
            errorMessage = e.getMessage();
            //Handle errors for Class.forName
            System.out.println("====> Exception");
            e.printStackTrace();
            //System.out.println("e.message: " + e.getMessage());
        } finally {
            //finally block used to close resources
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        long end = System.currentTimeMillis();
        long spendTime = end - start;
        
        System.out.println("====================> [sqlQuery] end\n");
        return new SqlQueryResult(columns, records, spendTime, errorMessage);
    }


    public String sqlInsert(String sql) {
        System.out.println("====================> [sqlInsert] start");

        String errorMessage = null;
        Statement statement = null;
        System.out.println("== sql:" + sql);
        try {
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        
        System.out.println("====================> [sqlInsert] end\n");
        return errorMessage;
    }
}
