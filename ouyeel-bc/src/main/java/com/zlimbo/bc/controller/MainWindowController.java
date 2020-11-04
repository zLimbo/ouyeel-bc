package com.zlimbo.bc.controller;

import com.zlimbo.bc.DataBaseArgs;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {
    
    @FXML
    private TabPane resultTabPane;

    @FXML
    private TextArea sqlInput;

    @FXML
    private AnchorPane sqlAnchorPane;

    @FXML
    private TreeView dbTreeView;

    @FXML
    private AnchorPane leftAnchorPane;

    @FXML
    private Button sqlRunButton;

    @FXML
    private Tab tablePane;

    @FXML
    private Tab messagePane;

    @FXML
    private TextArea sqlMessage;

    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("============> [initialize] start");
        Connection connection = null;
        Statement statement = null;
        List<String> tables = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DataBaseArgs.URL, DataBaseArgs.USER, DataBaseArgs.PASS);
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("show tables");
            while (resultSet.next()) {
                System.out.println("====> table: " + resultSet.getString(1));
                tables.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        TreeItem<String> databaseItem = new TreeItem<>(DataBaseArgs.DB_NAME, new ImageView(
                new Image(getClass().getResourceAsStream("/image/database.png"))));
        for (String table: tables) {
            TreeItem<String> tableItem = new TreeItem<>(table, new ImageView(
                    new Image(getClass().getResourceAsStream("/image/table.png"))));
            //tableItem.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> showTable(table));
            databaseItem.getChildren().add(tableItem);
        }
        dbTreeView.setRoot(databaseItem);

        dbTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("============> [handle] start\n");
                if (event.getClickCount() == 2) {
                    TreeItem<String> item = (TreeItem<String>) dbTreeView.getSelectionModel().getSelectedItem();
                    System.out.println("====> item name: " + item.getValue());
                    if (item.getValue() != DataBaseArgs.DB_NAME) {
                        showTable(item.getValue());
                    }
                }
                System.out.println("============> [handle] end\n");
            }
        });
        System.out.println("============> [initialize] end\n");
    }

    
    public void showTable(String table) {
        System.out.println("============> [showTable] start");
        String sql = "select * from " + table;
        sqlExecuteAndShow(sql);
        System.out.println("============> [showTable] end");
    }
    
    
    public void sqlRun(ActionEvent actionEvent) {
        System.out.println("============> [sqlRun] start");
        String sql = sqlInput.getText().trim();
        sqlExecuteAndShow(sql);
        System.out.println("============> [sqlRun] end\n");
    }


    public void sqlExecuteAndShow(String sql) {
        System.out.println("====================> [sqlExecuteAndShow] start");
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
            TableView tableView = new TableView();
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
            tablePane.setContent(tableView);
            resultTabPane.getSelectionModel().select(tablePane);
            sqlMessage.setText(sql + "\n> OK" + "\n> Time: " + (spendSeconds / 1000.0) + "s");
        } else {
            //sqlMessage.setTextFormatter();
            sqlMessage.setText(sql + "\n> Error: " + errorMessage + "\n> Time: " + (spendSeconds / 1000.0) + "s");
            resultTabPane.getSelectionModel().select(messagePane);
        }
        System.out.println("====================> [sqlExecuteAndShow] end\n");
    }

}
