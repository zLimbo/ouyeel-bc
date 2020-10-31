package com.zlimbo.bc.controller;

import com.zlimbo.bc.DataBaseArgs;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Pair;

import java.net.URL;
import java.sql.*;
import java.util.*;


public class MainWindowController2 implements Initializable {


    public @FXML Button newQueryButton;
    public @FXML Tab objectsTab;
    public @FXML TabPane showTabPane;
    public @FXML TreeView dbTreeView;

    Map<String, Tab> tableTabMap = new HashMap<>();
    int queryId = 1;

    SqlController sqlController = new SqlController(DataBaseArgs.URL, DataBaseArgs.USER, DataBaseArgs.PASS);


    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("============> [initialize] start");

        //showTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);

        List<String> tables = sqlController.getTables();
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
                        if (tableTabMap.containsKey(item.getValue())) {
                            tableTab = tableTabMap.get(item.getValue());
                        } else {
                            tableTab = new Tab(item.getValue() + " @" + dbTreeView.getRoot().getValue());
                            showTabPane.getTabs().add(tableTab);
                            tableTabMap.put(item.getValue(), tableTab);
                            showTable(item.getValue());
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


    public void showTable(String tableName) {
        System.out.println("============> [showTable] start");
        String sql = "select * from " + tableName;

        BorderPane borderPane = new BorderPane();
        ToolBar toolBar = new ToolBar();
        Button closeButton = new Button("Close");
        Button addButton = new Button("Add");
        toolBar.getItems().addAll(closeButton, addButton);
        borderPane.setTop(toolBar);
        TableView tableView = new TableView();
        borderPane.setCenter(tableView);
        Tab tableTab = tableTabMap.get(tableName);
        tableTab.setContent(borderPane);

        sqlController.sqlQueryAndShow(sql, tableView);

        closeButton.setOnAction(event -> {
            showTabPane.getTabs().remove(tableTab);
            tableTabMap.remove(tableName);
        });

        addButton.setOnAction(event -> addRecord(tableName));

        System.out.println("============> [showTable] end");
    }


    private void addRecord(String tableName) {
        System.out.println("====================> [addRecord] start");

        List<String> columnNames = new ArrayList<>();
        try {
            DatabaseMetaData databaseMetaData = sqlController.getConnection().getMetaData();
            ResultSet columnSet = databaseMetaData.getColumns(null, "%", tableName, "%");
            while (columnSet.next()) {
                String columnName = columnSet.getString("COLUMN_NAME");
                columnNames.add(columnName);
                System.out.println("== columnName: " + columnName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<TextField> textFields = new ArrayList<>();

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Record");
        dialog.setHeaderText(null);

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, cancelButtonType);
        Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        submitButton.setDisable(true);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        BitSet bitset = new BitSet(columnNames.size());
        for (int i = 0; i < columnNames.size(); ++i) {
            String columnName = columnNames.get(i);
            Label label = new Label(columnName);
            TextField textField = new TextField();
            textField.setPromptText(columnName);
            gridPane.add(label, 0, i);
            gridPane.add(textField, 1, i);
            // 监听，输入不得为空，否则提交按钮为灰色
            int finalI = i;
            textField.textProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue.trim().isEmpty()) {
                    bitset.clear(finalI);
                } else {
                    bitset.set(finalI);
                }
                boolean allSet = true;
                for (int j = 0; j < columnNames.size(); ++j) {
                    if (!bitset.get(j)) {
                        allSet = false;
                        break;
                    }
                }
                submitButton.setDisable(!allSet);
            }));
            textFields.add(textField);
        }
        dialog.getDialogPane().setContent(gridPane);
        // 点击提交，插入数据
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                StringBuilder stringBuilder = new StringBuilder("INSERT INTO " + tableName + " VALUES(");
                for (int i = 0; i < textFields.size(); ++i) {
                    TextField textField = textFields.get(i);
                    stringBuilder.append(textField.getText());
                    if (i != textFields.size() - 1) {
                        stringBuilder.append(",");
                    }
                }
                stringBuilder.append((");"));
                String sql = stringBuilder.toString();
                sqlController.sqlInsert(sql);
                showTable(tableName);   // 更新表显示
            }
            return null;
        });

        dialog.showAndWait();
        
        System.out.println("====================> [addRecord] end\n");
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
        runButton.setOnAction(event -> sqlController.sqlQueryAndShow(textArea.getText(), tableView));

        System.out.println("============> [newQuery] end");
    }
}
