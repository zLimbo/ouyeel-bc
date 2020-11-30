package com.zlimbo.zcat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class SqlController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private Connection connection;
    private String databaseName;
    private String host;
    private String port;
    private String userName;
    private String password;

    private boolean connectSuccess;
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isConnectSuccess() {
        return connectSuccess;
    }

    public void finialize() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public SqlController(String databaseName, String host, String port, String userName, String password) {
        logger.debug("[SqlControl] start");

        this.databaseName = databaseName;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName +
                "?useSSL=false" +
                "&useUnicode=true" +
                "&characterEncoding=UTF8" +
                "&serverTimezone=GMT" +
                "&allowPublicKeyRetrieval=true";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, userName, password);
            connectSuccess = true;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            connectSuccess = false;
            e.printStackTrace();
        }

        logger.debug("[SqlController] end\n");
    }


    Connection getConnection() {
        return connection;
    }


    public List<String> sqlShowTables() {
        String sql = "show tables";
        List<String> tables = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                logger.debug("table: " + resultSet.getString(1));
                tables.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return tables;
    }


    public SqlQueryResult sqlCreateTable(String sql) {
        logger.debug("[sqlCreateTable] start");
        SqlQueryResult sqlQueryResult = new SqlQueryResult();
        long start = System.currentTimeMillis();
        String errorMessage = null;
        PreparedStatement preparedStatement = null;
        logger.debug("== sql:" + sql);
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate(sql);
        } catch (Exception e) {
            sqlQueryResult.setErrorMessage(e.getMessage());
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        long end = System.currentTimeMillis();
        long spendTime = end - start;
        sqlQueryResult.setSpendTime(spendTime);
        logger.debug("[sqlCreateTable] end\n");
        return sqlQueryResult;
    }


    public static class SqlQueryResult {
        private final List<String> columns;
        private final List<List<String>> records;
        private String errorMessage;
        private long spendTime;

        public SqlQueryResult() {
            columns = new ArrayList<>();
            records = new ArrayList<>();
            errorMessage = null;
            spendTime = 0;
        };

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

        public void setSpendTime(long spendTime) {
            this.spendTime = spendTime;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }


    public SqlQueryResult sqlQuery(String sql) {
        logger.debug("[sqlQuery] start");

        long start = System.currentTimeMillis();

        String errorMessage = null;
        List<String> columns = new ArrayList<>();
        List<List<String>> records = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        try {
//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery(sql);
            preparedStatement = connection.prepareStatement(sql);
            logger.debug(preparedStatement.toString());
            ResultSet resultSet = preparedStatement.executeQuery();

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
            logger.debug("Exception");
            e.printStackTrace();
            //logger.debug("e.message: " + e.getMessage());
        } finally {
            //finally block used to close resources
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        long end = System.currentTimeMillis();
        long spendTime = end - start;

        logger.debug("[sqlQuery] end\n");
        return new SqlQueryResult(columns, records, spendTime, errorMessage);
    }


    public SqlQueryResult sqlInsert(String sql) {
        logger.debug("[sqlInsert] start");
        SqlQueryResult sqlQueryResult = new SqlQueryResult();
        long start = System.currentTimeMillis();

        PreparedStatement preparedStatement = null;
        logger.debug("== sql:" + sql);
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
        } catch (Exception e) {
            sqlQueryResult.setErrorMessage(e.getMessage());
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        long spendTime = end - start;
        sqlQueryResult.setSpendTime(spendTime);
        logger.debug("[sqlInsert] end\n");
        return sqlQueryResult;
    }


    public List<String> getColumns(String tableName) {
        logger.debug("[getColumns] start");

        List<String> columns = new ArrayList<>();
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet columnSet = databaseMetaData.getColumns(null, "%", tableName, "%");
            while (columnSet.next()) {
                String columnName = columnSet.getString("COLUMN_NAME");
                columns.add(columnName);
                logger.debug("== columnName: " + columnName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("[getColumns] end\n");
        return columns;
    }
}
