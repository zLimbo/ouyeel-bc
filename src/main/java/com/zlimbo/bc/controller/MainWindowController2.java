package com.zlimbo.bc.controller;

import com.zlimbo.bc.DataBaseArgs;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.*;


public class MainWindowController2 implements Initializable {


    public @FXML Button newQueryButton;
    public @FXML Tab objectsTab;
    public @FXML TabPane showTabPane;
    public @FXML TreeView dbTreeView;

    Map<String, Tab> tableTabMap = new HashMap<>();
    int queryId = 1;

    SqlController sqlController;


    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("============> [initialize] start");
        //showDatabase();
        System.out.println("============> [initialize] end\n");
    }


    public void showTable(String tableName) {
        System.out.println("============> [showTable] start");

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

        closeButton.setOnAction(event -> {
            showTabPane.getTabs().remove(tableTab);
            tableTabMap.remove(tableName);
        });
        addButton.setOnAction(event -> addRecord(tableName));

        String sql = "select * from " + tableName;
        showTableView(sql, tableView);

        System.out.println("============> [showTable] end");
    }


    private void showTableView(String sql, TableView tableView) {
        tableView.getColumns().clear();
        tableView.getItems().clear();
        SqlController.SqlQueryResult sqlQueryResult = sqlController.sqlQuery(sql);
        String errorMessage = sqlQueryResult.getErrorMessage();
        List<String> columns = sqlQueryResult.getColumns();
        List<List<String>> records = sqlQueryResult.getRecords();
        long spendTime = sqlQueryResult.getSpendTime();
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
//            sqlMessage.setText(sql + "\n> OK" + "\n> Time: " + spendTime + "s");
        } else {
//            sqlMessage.setText(sql + "\n> Error: " + errorMessage + "\n> Time: " + spendTime + "s");
//            resultTabPane.getSelectionModel().select(messagePane);
        }
    }


    private void addRecord(String tableName) {
        System.out.println("====================> [addRecord] start");

        List<String> columnNames = sqlController.getColumns(tableName);
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
        for (int i = 0; i < columnNames.size(); ++i) {
            String columnName = columnNames.get(i);
            Label label = new Label(columnName);
            TextField textField = new TextField();
            textField.setPromptText(columnName);
            gridPane.add(label, 0, i);
            gridPane.add(textField, 1, i);
            textFields.add(textField);
        }
        // 监听，输入不得为空，否则提交按钮为灰色
        for (TextField textField: textFields) {
            textField.textProperty().addListener((observable) -> {
                for (TextField textField1 : textFields) {
                    if (textField1.getText().trim().isEmpty()) {
                        submitButton.setDisable(true);
                        return;
                    }
                }
                submitButton.setDisable(false);
            });
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


    public void newConnection(ActionEvent actionEvent) {
        System.out.println("====================> [connectDatabase] start");
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("MySQL - New Connection");
        dialog.setHeaderText(null);

        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, cancelButtonType);
        Button connectButton = (Button) dialog.getDialogPane().lookupButton(connectButtonType);
        connectButton.setDisable(true);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        List<TextField> textFields = new ArrayList<>();
        Label databaseNameLabel = new Label("Database Name: ");
        TextField databaseNameTextField = new TextField();
        gridPane.add(databaseNameLabel, 0, 0);
        gridPane.add(databaseNameTextField, 1, 0);
        textFields.add(databaseNameTextField);

        Label hostLabel = new Label("Host: ");
        TextField hostTextField = new TextField();
        hostTextField.setText("localhost");
        gridPane.add(hostLabel, 0, 1);
        gridPane.add(hostTextField, 1, 1);
        textFields.add(hostTextField);

        Label portLabel = new Label("Port: ");
        TextField portTextField = new TextField();
        portTextField.setText("3306");
        gridPane.add(portLabel, 0, 2);
        gridPane.add(portTextField, 1, 2);
        textFields.add(portTextField);

        Label userNameLabel = new Label("User Name: ");
        TextField userNameTextField = new TextField();
        userNameTextField.setText("root");
        gridPane.add(userNameLabel, 0, 3);
        gridPane.add(userNameTextField, 1, 3);
        textFields.add(userNameTextField);

        Label passwordLabel = new Label("Password: ");
        PasswordField passwordField = new PasswordField();
        gridPane.add(passwordLabel, 0, 4);
        gridPane.add(passwordField, 1, 4);
        textFields.add(passwordField);

        for (TextField textField: textFields) {
            textField.textProperty().addListener((observable) -> {
                boolean allSet = true;
                for (TextField textField1 : textFields) {
                    if (textField1.getText().trim().isEmpty()) {
                        connectButton.setDisable(true);
                        return;
                    }
                }
                connectButton.setDisable(false);
            });
        }

        dialog.getDialogPane().setContent(gridPane);
        // 提交数据
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                String databaseName = databaseNameTextField.getText();
                String host = hostTextField.getText();
                String port = portTextField.getText();
                String userName = userNameTextField.getText();
                String password = passwordField.getText();
                sqlController = new SqlController(databaseName, host, port, userName, password);
                showTabPane.getTabs().clear();
                showDatabase();
            }
            return null;
        });

        dialog.showAndWait();

        System.out.println("====================> [connectDatabase] end\n");
    }


    private void showDatabase() {
        System.out.println("====================> [showDatabase] start");

        //showTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        if (sqlController == null) {
            return;
        }

        List<String> tables = sqlController.sqlShowTables();
        TreeItem<String> databaseItem = new TreeItem<>(sqlController.getDatabaseName(),
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

        System.out.println("====================> [showDatabase] end\n");
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
        runButton.setOnAction(event -> showTableView(textArea.getText(), tableView));

        System.out.println("============> [newQuery] end");
    }

}
