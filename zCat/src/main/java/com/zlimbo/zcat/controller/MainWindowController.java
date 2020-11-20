package com.zlimbo.zcat.controller;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainWindowController implements Initializable {


    public @FXML Button newQueryButton;
    public @FXML Tab objectsTab;
    public @FXML TabPane showTabPane;
    public @FXML TreeView dbTreeView;
    //public @FXML TextArea messageTextArea;
    public @FXML Button newConnectionButton;
    public @FXML Button citaButton;

    Map<String, Tab> tabMap = new HashMap<>();
    int queryId = 1;

    SqlController sqlController;
    Map<String, ChainControl> chainControlMap = new HashMap<>();


    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("============> [initialize] start");
        newConnectionButton.setGraphic(
                new ImageView(new Image(getClass().getResourceAsStream("/image/connection.png"))));
        newQueryButton.setGraphic(
                new ImageView(new Image(getClass().getResourceAsStream("/image/query.png"))));
        newQueryButton.setDisable(true);
        citaButton.setGraphic(
                new ImageView(new Image(getClass().getResourceAsStream("/image/cita.png"))));

        sqlController = new SqlController("ouyeel",
                "localhost", "3306", "root", "123456");
        showDatabase();
        newQueryButton.setDisable(false);

        System.out.println("============> [initialize] end\n");
    }


    public void showTable(String tableName) {
        System.out.println("============> [showTable] start");

        BorderPane borderPane = new BorderPane();
        ToolBar toolBar = new ToolBar();
        Button closeButton = new Button("Close",
                new ImageView(new Image(getClass().getResourceAsStream("/image/close.png"))));
        Button addButton = new Button("Add",
                new ImageView(new Image(getClass().getResourceAsStream("/image/add.png"))));
        toolBar.getItems().addAll(closeButton, addButton);
        borderPane.setTop(toolBar);
        TableView tableView = new TableView();
        borderPane.setCenter(tableView);
        Tab tableTab = tabMap.get(tableName);
        tableTab.setContent(borderPane);

        closeButton.setOnAction(event -> {
            showTabPane.getTabs().remove(tableTab);
            tabMap.remove(tableName);
        });
        addButton.setOnAction(event -> addRecord(tableName));

        String sql = "SELECT * FROM " + tableName;
        executeSqlAndShowTableView(sql, tableView, null);

        System.out.println("============> [showTable] end");
    }


    private void executeSqlAndShowTableView(String sql, TableView tableView, TextArea messageTextArea) {
        tableView.setTableMenuButtonVisible(true);
        tableView.getColumns().clear();
        tableView.getItems().clear();
        SqlController.SqlQueryResult sqlQueryResult = null;
        String sqlUpperCase = sql.toUpperCase();
        if (sqlUpperCase.startsWith("CREATE TABLE")) {
            sqlQueryResult = sqlController.sqlCreateTable(sql);
            showDatabase();
        } else if (sqlUpperCase.startsWith("INSERT INTO")) {
            sqlQueryResult = sqlController.sqlInsert(sql);
            if (sqlQueryResult.getErrorMessage() == null) {
                Matcher matcher = Pattern.compile("^\\w+\\s+\\w+\\s+(\\w+)").matcher(sql); // 正则获取表名
                if (matcher.find()) {
                    String tableName = matcher.group(1);
                    if (tabMap.containsKey(tableName)) {
                        showTable(tableName);   // 更新表显示
                    }
                }
            }
        } else {
            sqlQueryResult = sqlController.sqlQuery(sql);
        }
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
            if (messageTextArea != null) {
                messageTextArea.setStyle("-fx-text-fill:#00ff00;");
                messageTextArea.setText(sql + "\n> OK" + "\n> Time: " + (spendTime / 1000.0) + "s");
            }
        } else {
            if (messageTextArea != null) {
                messageTextArea.setStyle("-fx-text-fill:#ff0000;");
                messageTextArea.setText(sql + "\n> Error: " + errorMessage + "\n> Time: " + (spendTime / 1000.0) + "s");
            }
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
                    stringBuilder.append("'" + textField.getText() + "'");
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
        gridPane.setPadding(new Insets(20, 150, 10, 30));

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
                SqlController sqlController1 = new SqlController(databaseName, host, port, userName, password);
                if (sqlController1.isConnectSuccess()) {
                    sqlController = sqlController1;
                    showTabPane.getTabs().clear();
                    tabMap.clear(); // 清空
                    showDatabase();
                    newQueryButton.setDisable(false);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Connection");
                    alert.setHeaderText("Invalid connection!");

                    alert.showAndWait();
                }
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
        databaseItem.setExpanded(true);
        dbTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("============> [handle] start\n");

                if (event.getClickCount() == 2) {
                    TreeItem<String> item = (TreeItem<String>) dbTreeView.getSelectionModel().getSelectedItem();
                    System.out.println("====> item name: " + item.getValue());
                    if (item.getValue() != sqlController.getDatabaseName()) {
                        Tab tableTab;
                        if (tabMap.containsKey(item.getValue())) {
                            tableTab = tabMap.get(item.getValue());
                        } else {
                            tableTab = new Tab(item.getValue() + " @" + dbTreeView.getRoot().getValue());
                            tableTab.setGraphic(
                                    new ImageView(new Image(getClass().getResourceAsStream("/image/table2.png")))
                            );
                            showTabPane.getTabs().add(tableTab);
                            tabMap.put(item.getValue(), tableTab);
                        }
                        showTable(item.getValue());
                        showTabPane.getSelectionModel().select(tableTab);
                    } else {
                        showTabPane.getSelectionModel().select(objectsTab);
                        showDatabase();
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
        queryTab.setGraphic(
                new ImageView(new Image(getClass().getResourceAsStream("/image/query2.png"))));
        queryTab.setClosable(true);
        showTabPane.getTabs().add(queryTab);
        showTabPane.getSelectionModel().select(queryTab);

        BorderPane borderPane = new BorderPane();
        queryTab.setContent(borderPane);

        ToolBar toolBar = new ToolBar();
        borderPane.setTop(toolBar);
        Button closeButton = new Button("Close",
                new ImageView(new Image(getClass().getResourceAsStream("/image/close.png"))));
        Button saveButton = new Button("Save",
                new ImageView(new Image(getClass().getResourceAsStream("/image/save.png"))));
        Button runButton = new Button("Run",
                new ImageView(new Image(getClass().getResourceAsStream("/image/run.png"))));
        toolBar.getItems().addAll(closeButton, saveButton, runButton);

        SplitPane splitPane = new SplitPane();
        borderPane.setCenter(splitPane);

        TextArea textArea = new TextArea();
        TableView tableView = new TableView();
        TextArea messageTextArea = new TextArea();
        messageTextArea.setEditable(false);
        splitPane.getItems().addAll(textArea, tableView, messageTextArea);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPosition(0, 0.2);
        splitPane.setDividerPosition(1, 0.85);


//        ContextMenu contextMenu = new ContextMenu();
//        MenuItem menuItem = new MenuItem("关闭");
//        contextMenu.getItems().add(menuItem);
//        queryTab.setContextMenu(contextMenu);
//        menuItem.setOnAction(event -> {
//            showTabPane.getTabs().remove(queryTab);
//        });

        closeButton.setOnAction(event -> {
            showTabPane.getTabs().remove(queryTab);
        });

        runButton.setOnAction(event -> executeSqlAndShowTableView(textArea.getText(), tableView, messageTextArea));

        System.out.println("============> [newQuery] end");
    }


    public void connectionCita(ActionEvent actionEvent) {
        System.out.println("====================> [connectionCita] start");
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        //dialog.setWidth(200);
        dialog.setTitle("CITA - New Connection");
        dialog.setHeaderText(null);

        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, cancelButtonType);
        Button connectButton = (Button) dialog.getDialogPane().lookupButton(connectButtonType);
        connectButton.setDisable(true);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(30, 60, 10, 20));

        Label citaUrlLabel = new Label("CITA URL: ");
        TextField citaUrlTextField = new TextField();
        citaUrlTextField.setPrefWidth(300);
        citaUrlTextField.textProperty().addListener(observable -> {
            if (citaUrlTextField.getText().trim().isEmpty()) {
                connectButton.setDisable(true);
            } else {
                connectButton.setDisable(false);
            }
        });
        gridPane.add(citaUrlLabel, 0, 0);
        gridPane.add(citaUrlTextField, 1, 0);

        dialog.getDialogPane().setContent(gridPane);
        // 提交数据
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                String citaUrl = citaUrlTextField.getText().trim();
                if (!chainControlMap.containsKey(citaUrl)) {
                    ChainControl chainControl = new ChainControl(citaUrl);
                    if (chainControl.isConnectSuccess()) {
                        chainControlMap.put(citaUrl, chainControl);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error Connection");
                        alert.setHeaderText("Invalid connection!");
                        alert.showAndWait();
                        return null;
                    }
                }
                showCita(citaUrl);
            }
            return null;
        });

        dialog.showAndWait();

        System.out.println("====================> [connectDatabase] end\n");
        //showCita();
    }


    public void showCita(String citaUrl) {
        System.out.println("============> [showCita] start");
        if (tabMap.containsKey(citaUrl)) {
            showTabPane.getSelectionModel().select(tabMap.get(citaUrl));
            return;
        }

        ChainControl chainControl = chainControlMap.get(citaUrl);
        chainControl.updateStart();
        Tab citaTab = new Tab(citaUrl + " @CITA");
        tabMap.put(citaUrl, citaTab);
        showTabPane.getTabs().add(citaTab);
        showTabPane.getSelectionModel().select(citaTab);

        citaTab.setGraphic(
                new ImageView(new Image(getClass().getResourceAsStream("/image/cita2.png")))
        );
        BorderPane borderPane = new BorderPane();

        ToolBar toolBar = new ToolBar();
        Button closeButton = new Button("Close",
                new ImageView(new Image(getClass().getResourceAsStream("/image/close.png"))));
        toolBar.getItems().addAll(closeButton);
        borderPane.setTop(toolBar);
        TableView tableView = new TableView();
        borderPane.setCenter(tableView);
        citaTab.setContent(borderPane);

        closeButton.setOnAction(event -> {
            showTabPane.getTabs().remove(citaTab);
            chainControl.updateStop();
            chainControlMap.remove(citaUrl);
            tabMap.remove(citaUrl);
        });

        TableColumn<List<StringProperty>, String> attributeColumn = new TableColumn<>("attribute");
        TableColumn<List<StringProperty>, String> valueColumn = new TableColumn<>("value");
        attributeColumn.setCellValueFactory(data -> data.getValue().get(0));
        valueColumn.setCellValueFactory(data -> data.getValue().get(1));
        tableView.getColumns().addAll(attributeColumn, valueColumn);

        ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
        List<List<StringProperty>> bcInfo = chainControl.getBcInfo();

        for (List<StringProperty> list: bcInfo) {
            List<StringProperty> row = new ArrayList<>();
            row.add(0, list.get(0));
            row.add(1, list.get(1));
            data.add(row);
        }
        tableView.setItems(data);
        System.out.println("============> [showCita] end");
    }

}
