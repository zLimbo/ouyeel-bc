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
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.*;
import java.util.*;

public class MainWindowController2 implements Initializable {

    public @FXML Button newQueryButton;
    public @FXML Tab objectsTab;
    public @FXML TabPane showTabPane;
    public @FXML TreeView dbTreeView;

    Map<String, Tab> tabMap = new HashMap<>();
    int queryId = 1;

    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("============> [initialize] start");

        //showTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        

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
        TreeItem<String> databaseItem = new TreeItem<>(DataBaseArgs.DB_NAME,
                new ImageView(new Image(getClass().getResourceAsStream("/image/database.png"))));
        for (String table: tables) {
            TreeItem<String> tableItem = new TreeItem<>(table,
                    new ImageView(new Image(getClass().getResourceAsStream("/image/table.png"))));
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
                        Tab tableTab;
                        if (tabMap.containsKey(item.getValue())) {
                            tableTab = tabMap.get(item.getValue());
                        } else {
                            tableTab = new Tab(item.getValue() + " @" + dbTreeView.getRoot().getValue());
                            showTabPane.getTabs().add(tableTab);
                            showTable(item.getValue(), tableTab);
                            tabMap.put(item.getValue(), tableTab);
                        }
                        showTabPane.getSelectionModel().select(tableTab);
                    } else {
                        showTabPane.getSelectionModel().select(objectsTab);
                    }
                }
                System.out.println("============> [handle] end\n");
            }
        });
        System.out.println("============> [initialize] end\n");
    }


    public void showTable(String tableName, Tab tableTab) {
        System.out.println("============> [showTable] start");
        String sql = "select * from " + tableName;
        BorderPane borderPane = new BorderPane();
        ToolBar toolBar = new ToolBar();
        Button button = new Button("Sort");
        toolBar.getItems().add(button);
        borderPane.setTop(toolBar);
        TableView tableView = new TableView();
        borderPane.setCenter(tableView);
        tableTab.setContent(borderPane);
        sqlExecuteAndShow(sql, tableView);
        System.out.println("============> [showTable] end");
    }


    public void sqlExecuteAndShow(String sql, TableView tableView) {
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
        System.out.println("====================> [sqlExecuteAndShow] end\n");
    }


    public void newQuery(ActionEvent actionEvent) {
        System.out.println("============> [newQuery] start");
        Tab queryTab = new Tab("query" + queryId++);
        BorderPane borderPane = new BorderPane();
        ToolBar toolBar = new ToolBar();
        Button closeButton = new Button("Close");
        Button saveButton = new Button("Save");
        Button runButton = new Button("Run");
        toolBar.getItems().addAll(closeButton, saveButton, runButton);
        borderPane.setTop(toolBar);
        TextArea textArea = new TextArea();
        TableView tableView = new TableView();
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(textArea, tableView);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPosition(0, 0.3);
        borderPane.setCenter(splitPane);
        queryTab.setContent(borderPane);
        queryTab.setClosable(true);
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem("关闭");
        contextMenu.getItems().add(menuItem);
        queryTab.setContextMenu(contextMenu);
        showTabPane.getTabs().add(queryTab);
        showTabPane.getSelectionModel().select(queryTab);

        closeButton.setOnAction(event -> {
            showTabPane.getTabs().remove(queryTab);
        });
        menuItem.setOnAction(event -> {
            showTabPane.getTabs().remove(queryTab);
        });
        runButton.setOnAction(event -> sqlExecuteAndShow(textArea.getText(), tableView));

        System.out.println("============> [newQuery] end");
    }




//    public void sqlRun(ActionEvent actionEvent) {
//        System.out.println("============> [sqlRun] start");
//        String sql = sqlInput.getText().trim();
//        sqlExecuteAndShow(sql);
//        System.out.println("============> [sqlRun] end\n");
//    }
}
