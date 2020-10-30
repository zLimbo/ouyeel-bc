package com.zlimbo.bc.controller;

import com.zlimbo.bc.DataBaseArgs;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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


    List<String> getTables() {
        List<String> tables = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("show tables");
            while (resultSet.next()) {
                System.out.println("====> table: " + resultSet.getString(1));
                tables.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tables;
    }


    public void sqlQueryAndShow(String sql, TableView tableView) {
        System.out.println("====================> [SqlControl] start");
        long start = System.currentTimeMillis();

        String errorMessage = null;
        List<String> columns = new ArrayList<>();
        List<List<String>> records = new ArrayList<>();
        Connection connection = null;
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
                if (statement != null)
                    connection.close();
            } catch (SQLException se) {
            }// do nothing
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }

        long end = System.currentTimeMillis();
        double spendSeconds = ((double)end - (double)start) / 1000.0;

        if (errorMessage == null) {
            tableView.getSelectionModel().setCellSelectionEnabled(true);

            for (int i = 0; i < columns.size(); ++i) {
                TableColumn<List<StringProperty>, String> tableColumn = new TableColumn<>(columns.get(i));
                int finalI = i;
                tableColumn.setCellValueFactory(data -> data.getValue().get(finalI));
                tableView.getColumns().add(tableColumn);
            }
            ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
            for (List<String> record : records) {
                List<StringProperty> row = new ArrayList<>();
                for (int i = 0; i < record.size(); ++i) {
                    row.add(i, new SimpleStringProperty(record.get(i)));
                }
                data.add(row);
            }
            tableView.setItems(data);
//            tablePane.setContent(tableView);
//            resultTabPane.getSelectionModel().select(tablePane);
//            sqlMessage.setText(sql + "\n> OK" + "\n> Time: " + spendSeconds + "s");
        } else {
            tableView.getColumns().clear();
            tableView.getItems().clear();
//            sqlMessage.setText(sql + "\n> Error: " + errorMessage + "\n> Time: " + spendSeconds + "s");
//            resultTabPane.getSelectionModel().select(messagePane);
        }
        System.out.println("====================> [sqlQueryAndShow] end\n");
    }


    public void sqlInsert(String sql) {
        System.out.println("====================> [sqlInsert] start");
        System.out.println("== sql:" + sql);
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("====================> [sqlInsert] end\n");
    }
}
