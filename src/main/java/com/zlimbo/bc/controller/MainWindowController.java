package com.zlimbo.bc.controller;

import com.zlimbo.bc.DataBaseArgs;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    @FXML
    private TextField sqlTextField;

    @FXML
    private Button sqlRunButton;

    @FXML
    private Tab tablePane;

    @FXML
    private Tab messagePane;

    @FXML
    public TextArea sqlMessage;

    public void initialize(URL location, ResourceBundle resources) {

    }

    public void sqlRun(ActionEvent actionEvent) {

        String sql = sqlTextField.getText();

        System.out.println("============> sqlRun");


        List<String> columns = new ArrayList<>();
        List<List<String>> records = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DataBaseArgs.URL, DataBaseArgs.USER, DataBaseArgs.PASS);
            System.out.println("Connected database successfully...");

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();

            System.out.println("query sql: " + sql);
            ResultSet resultSet = stmt.executeQuery(sql);
            

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
            sqlMessage.setText(e.getMessage());
            //Handle errors for Class.forName
            System.out.println("====> Exception");
            e.printStackTrace();
            //System.out.println("e.message: " + e.getMessage());
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null)
                    conn.close();
            } catch (SQLException se) {
            }// do nothing
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }

        TableView tableView = new TableView();
        tableView.getSelectionModel().setCellSelectionEnabled(true);

        for (int i = 0; i < columns.size(); ++i) {
            TableColumn<List<StringProperty>, String> tableColumn = new TableColumn<>(columns.get(i));
            int finalI = i;
            tableColumn.setCellValueFactory(data -> data.getValue().get(finalI));
            tableView.getColumns().add(tableColumn);
        }
        ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
        for (List<String> record: records) {
            List<StringProperty> row = new ArrayList<>();
            for (int i = 0; i < record.size(); ++i) {
                row.add(i, new SimpleStringProperty(record.get(i)));
            }
            data.add(row);
        }
        tableView.setItems(data);

        tablePane.setContent(tableView);

        System.out.println("================> End\n");
    }
}
