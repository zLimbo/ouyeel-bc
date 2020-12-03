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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 主窗口类
 */
public class MainWindowController implements Initializable {


    /**
     * 日志
     */
    final Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * fxml 文件中相应标签（id="xxx"）的映射的对象
     */
    public @FXML Button newQueryButton;
    public @FXML Tab objectsTab;
    public @FXML TabPane showTabPane;
    public @FXML TreeView dbTreeView;
    public @FXML Button newConnectionButton;
    public @FXML Button citaButton;
    public @FXML VBox mainVBox;


    /**
     * queryTab 分配id
     */
    int queryTabId = 1;


    /**
     * sql 相关操作类
     */
    SqlController sqlController;


    /**
     * 存储显示的 Tab, 方便使用 TabName 查找对应的 Tab
     */
    Map<String, Tab> tabMap = new HashMap<>();


    /**
     * 存储 cita url 和其相关操作类的映射
     */
    Map<String, ChainControl> chainControlMap = new HashMap<>();


    /**
     * 界面初始化的相应配置
     * @param location
     * @param resources
     */
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("[initialize] start");

        // 字体大小
        mainVBox.setStyle("-fx-font: 18  arial; -fx-font-family: 'Microsoft YaHei UI'");

        // 显示所有Tab的删除图标;
        showTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        // button icon
        newConnectionButton.setGraphic(
                new ImageView(new Image(getClass().getResourceAsStream("/image/connection.png"))));
        newQueryButton.setGraphic(
                new ImageView(new Image(getClass().getResourceAsStream("/image/query.png"))));
        citaButton.setGraphic(
                new ImageView(new Image(getClass().getResourceAsStream("/image/cita.png"))));

        // 没有数据库不显示 query
        newQueryButton.setDisable(true);

        // 测试
//        sqlController = new SqlController("blockchainbase",
//                "localhost", "3306", "root", "123456");
//        newQueryButton.setDisable(false);
//        showDatabase();




        logger.debug("[initialize] end");
    }


    /**
     * 显示一个数据库表
     * @param tableName
     */
    public void showTable(String tableName) {
        logger.debug("[showTable] start");

        Tab tableTab;
        SplitPane tableSplitPane;
        if (tabMap.containsKey(tableName)) {
            tableTab = tabMap.get(tableName);
            tableSplitPane = (SplitPane)((BorderPane)tableTab.getContent()).getCenter();
        } else {
            tableTab = new Tab(tableName);
            tableTab.setGraphic(
                    new ImageView(new Image(getClass().getResourceAsStream("/image/table.png")))
            );
            addTab(tableName, tableTab);
            tabAddContextMenu(tableName, tableTab);
            tableTab.setOnClosed(event1 -> closeTab(tableName, tableTab));
            BorderPane borderPane = new BorderPane();
            ToolBar toolBar = new ToolBar();
            Button closeButton = new Button("关闭",
                    new ImageView(new Image(getClass().getResourceAsStream("/image/close.png"))));
            Button addButton = new Button("Add",
                    new ImageView(new Image(getClass().getResourceAsStream("/image/add.png"))));
            toolBar.getItems().addAll(closeButton, addButton);
            borderPane.setTop(toolBar);
            tableSplitPane = new SplitPane();
            borderPane.setCenter(tableSplitPane);
            tableTab.setContent(borderPane);

            closeButton.setOnAction(event -> {
                closeTab(tableName, tableTab);
            });
            addButton.setOnAction(event -> addRecord(tableName));
        }

        String sql = "SELECT * FROM " + tableName;
        executeSqlAndShowTableView(sql, tableSplitPane, null);

        showTabPane.getSelectionModel().select(tableTab);

        logger.debug("[showTable] end");
    }


    /**
     * 执行sql语句并将其显示在相应的 TableView 中，同时显示结果信息
     * @param sql
     * @param tableSplitPane
     * @param messageTextArea
     * @return
     */
    private int executeSqlAndShowTableView(String sql, SplitPane tableSplitPane, TextArea messageTextArea) {
        logger.debug("[executeSqlAndShowTableView] start");
        logger.debug("sql: " + sql);
        int returnFlag = 0;
        tableSplitPane.getItems().clear();
        SqlController.SqlQueryResult sqlQueryResult = null;
        String sqlUpCase = sql.toUpperCase().trim();
        if (sqlUpCase.isEmpty()) {
            sqlQueryResult = new SqlController.SqlQueryResult();
            sqlQueryResult.setErrorMessage("Can not issue empty query.");
        } else if (sqlUpCase.startsWith("CREATE")) {
            logger.debug("CREATE TABLE!");
            sqlQueryResult = sqlController.sqlCreateTable(sql);
            if (sqlQueryResult.getErrorMessage() == null) {
                showDatabase();
                Matcher matcher = Pattern.compile("^\\s*\\w+\\s+\\w+\\s+(\\w+)").matcher(sql);  // 正则获取表名
                if (matcher.find()) {
                    String tableName = matcher.group(1);
                    sqlQueryResult = sqlController.sqlQuery("DESCRIBE " + tableName);
                }
                returnFlag = 1;
            }
        } else if (sqlUpCase.startsWith("INSERT")) {
            logger.debug("INSERT!");
            sqlQueryResult = sqlController.sqlInsert(sql);
            returnFlag = 2;
        } else { // Query
            sqlQueryResult = sqlController.sqlQuery(sql);
            returnFlag = 3;
        }
        String errorMessage = sqlQueryResult.getErrorMessage();
        List<String> columns = sqlQueryResult.getColumns();
        List<List<String>> records = sqlQueryResult.getRecords();
        long spendTime = sqlQueryResult.getSpendTime();
        if (errorMessage == null) {
            TableView tableView = new TableView();
            tableView.setPlaceholder(
                    new ImageView(new Image(getClass().getResourceAsStream("/image/tableEmpty.png"))));
            tableView.setTableMenuButtonVisible(true);
            Pagination pagination = new Pagination();
            pagination.setPageCount(1);
            tableSplitPane.getItems().addAll(tableView, pagination);
            tableSplitPane.setOrientation(Orientation.HORIZONTAL);
            tableSplitPane.setDividerPosition(0, 0.6);

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
            showPagination(tableView, pagination, columns, records);

            if (messageTextArea != null) {
                messageTextArea.setStyle("-fx-text-fill:#00ff00;");
                messageTextArea.setText(sql.trim() + "\n> OK" + "\n> 时间: " + (spendTime / 1000.0) + "s");
            }
        } else {
            returnFlag = 0;
            if (messageTextArea != null) {
                messageTextArea.setStyle("-fx-text-fill:#ff0000;");
                messageTextArea.setText(sql.trim() + "\n> Error: " + errorMessage + "\n> 时间: " + (spendTime / 1000.0) + "s");
            }
        }
        logger.debug("[executeSqlAndShowTableView] start");
        return returnFlag;
    }


    /**
     * 分页显示单个数据的详细情况
     * @param tableView
     * @param pagination
     * @param columns
     * @param records
     */
    private void showPagination(TableView tableView, Pagination pagination, List<String> columns, List<List<String>> records) {
        logger.debug("[showQuerySingle] start");

        TableView sigleTableView = new TableView();
        sigleTableView.setPlaceholder(
                new ImageView(new Image(getClass().getResourceAsStream("/image/tableEmpty.png"))));
        pagination.setPageCount(records.size());

        if (!records.isEmpty()) {
            TableColumn<List<StringProperty>, String> keyColumn = new TableColumn<>("key");
            TableColumn<List<StringProperty>, String> valueColumn = new TableColumn<>("value");
            keyColumn.setCellValueFactory(data -> data.getValue().get(0));
            valueColumn.setCellValueFactory(data -> data.getValue().get(1));
            sigleTableView.getColumns().addAll(keyColumn, valueColumn);

            ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
            List<StringProperty> valuePropertyList = new ArrayList<>();
            for (int i = 0; i < columns.size(); ++i) {
                List<StringProperty> row = new ArrayList<>();
                StringProperty valueProperty = new SimpleStringProperty(records.get(0).get(i));
                valuePropertyList.add(valueProperty);
                row.add(0, new SimpleStringProperty(columns.get(i)));
                row.add(1, valueProperty);
                data.add(row);
            }
            sigleTableView.setItems(data);

            pagination.setPageFactory(pageIndex -> {
                for (int i = 0; i < columns.size(); ++i) {
                    valuePropertyList.get(i).setValue(records.get(pageIndex).get(i));
                }
                return sigleTableView;
            });

            tableView.getSelectionModel().selectedIndexProperty().addListener(
                    (observable, oldValue, newValue) -> pagination.setCurrentPageIndex(newValue.intValue())
            );
        }

        logger.debug("[showQuerySingle] end");
    }


    /**
     * 增加一条数据项
     * @param tableName
     */
    private void addRecord(String tableName) {
        logger.debug("[addRecord] start");

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

        logger.debug("[addRecord] end");
    }


    /**
     * 连接新的数据库
     * @param actionEvent
     */
    public void newConnection(ActionEvent actionEvent) {
        logger.debug("[connectDatabase] start");
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
                    //logger.debug("database connect success");
                    sqlController = sqlController1;
                    showTabPane.getTabs().clear();
                    tabMap.clear(); // 清空
                    showDatabase();
                    newQueryButton.setDisable(false);
                } else {
                    //logger.debug("database connect fail");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Connection");
                    alert.setHeaderText("Invalid connection!");

                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();

        logger.debug("[connectDatabase] end");
    }


    /**
     * 显示数据库树形菜单
     */
    private void showDatabase() {
        logger.debug("[showDatabase] start");

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
                logger.debug("[handle] start\n");

                if (event.getClickCount() == 2) {
                    TreeItem<String> item = (TreeItem<String>) dbTreeView.getSelectionModel().getSelectedItem();
                    logger.debug("item name: " + item.getValue());
                    if (item.getValue() != sqlController.getDatabaseName()) {
                        String tableName = item.getValue();
                        showTable(tableName);
                    } else {
//                        showTabPane.getSelectionModel().select(objectsTab);
                        showDatabase();
                    }
                }

                logger.debug("[handle] end");
            }
        });

        logger.debug("[showDatabase] end");
    }


    /**
     * 创建新的查询Tab
     * @param actionEvent
     */
    public void newQuery(ActionEvent actionEvent) {
        logger.debug("[newQuery] start");

        String queryTabName = "查询[" + (queryTabId++) + "]";
        Tab queryTab = new Tab(queryTabName);
        queryTab.setGraphic(
                new ImageView(new Image(getClass().getResourceAsStream("/image/query2.png"))));
        addTab(queryTabName, queryTab);
        tabAddContextMenu(queryTabName, queryTab);
        queryTab.setOnClosed(event -> closeTab(queryTabName, queryTab));
        BorderPane borderPane = new BorderPane();
        queryTab.setContent(borderPane);

        ToolBar toolBar = new ToolBar();
        borderPane.setTop(toolBar);
        Button closeButton = new Button("关闭",
                new ImageView(new Image(getClass().getResourceAsStream("/image/close.png"))));
//        Button saveButton = new Button("Save",
//                new ImageView(new Image(getClass().getResourceAsStream("/image/save.png"))));
        Button runButton = new Button("运行",
                new ImageView(new Image(getClass().getResourceAsStream("/image/run.png"))));
        //toolBar.getItems().addAll(closeButton, saveButton, runButton);
        toolBar.getItems().addAll(closeButton, runButton);

        SplitPane splitPane = new SplitPane();
        borderPane.setCenter(splitPane);

        TextArea textArea = new TextArea();
        SplitPane tableSplitPane = new SplitPane();
        TextArea messageTextArea = new TextArea();
        messageTextArea.setEditable(false);
        splitPane.getItems().add(textArea);
        splitPane.setOrientation(Orientation.VERTICAL);


        closeButton.setOnAction(event -> {
            closeTab(queryTabName, queryTab);
        });

        runButton.setOnAction(event -> {
            splitPane.getItems().clear();
            int flag = executeSqlAndShowTableView(textArea.getText(), tableSplitPane, messageTextArea);
            if (flag == 3) {
                splitPane.getItems().addAll(textArea, tableSplitPane, messageTextArea);
                splitPane.setDividerPosition(0, 0.2);
                splitPane.setDividerPosition(1, 0.85);
            } else {
                splitPane.getItems().addAll(textArea, messageTextArea);
                splitPane.setDividerPosition(0, 0.5);
            }
        });

        logger.debug("[newQuery] end");
    }


    /**
     * 为Tab添加右键菜单
     * @param tabName
     * @param tab
     */
    private void tabAddContextMenu(String tabName, Tab tab) {
        logger.debug("[tabAddContextMenu] start [tabName = " + tabName + "]");
        ContextMenu contextMenu = new ContextMenu();
        MenuItem closeMenuItem = new MenuItem("关闭");
        closeMenuItem.setOnAction(event -> closeTab(tabName, tab));
        contextMenu.getItems().add(closeMenuItem);
        tab.setContextMenu(contextMenu);
        logger.debug("[tabAddContextMenu] end");
    }


    /**
     * 添加一个 Tab 的相应操作
     * @param tabName
     * @param tab
     */
    private void addTab(String tabName, Tab tab) {
        logger.debug("[addTab] start [tabName = " + tabName + "]");
        showTabPane.getTabs().add(tab);
        showTabPane.getSelectionModel().select(tab);
        tabMap.put(tabName, tab);
        logger.debug("[addTab] end");
    }


    /**
     * 删除一个 Tab 的相应操作
     * @param tabName
     * @param tab
     */
    private void closeTab(String tabName, Tab tab) {
        logger.debug("[closeTab] start [tabName = " + tabName + "]");
        showTabPane.getTabs().remove(tab);
        tabMap.remove(tabName);
        logger.debug("[closeTab] end");
    }


    /**
     * 连接 cita
     * @param actionEvent
     */
    public void connectionCita(ActionEvent actionEvent) {
        logger.debug("[connectionCita] start");
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

        logger.debug("[connectDatabase] end");
        //showCita();
    }


    /**
     * cita Tab 显示
     * @param citaUrl
     */
    public void showCita(String citaUrl) {
        logger.debug("[showCita] start");
        if (tabMap.containsKey(citaUrl)) {
            showTabPane.getSelectionModel().select(tabMap.get(citaUrl));
            return;
        }

        ChainControl chainControl = chainControlMap.get(citaUrl);
        chainControl.updateStart();
        Tab citaTab = new Tab(citaUrl + " @CITA");

        addTab(citaUrl, citaTab);
        tabAddContextMenu(citaUrl, citaTab);
        citaTab.setOnClosed(event -> closeTab(citaUrl, citaTab));

        citaTab.setGraphic(
                new ImageView(new Image(getClass().getResourceAsStream("/image/cita2.png")))
        );
        BorderPane borderPane = new BorderPane();

        ToolBar toolBar = new ToolBar();
        Button closeButton = new Button("关闭",
                new ImageView(new Image(getClass().getResourceAsStream("/image/close.png"))));
        toolBar.getItems().addAll(closeButton);
        borderPane.setTop(toolBar);
        TableView tableView = new TableView();
        borderPane.setCenter(tableView);
        citaTab.setContent(borderPane);

        closeButton.setOnAction(event -> {
            chainControl.updateStop();
            chainControlMap.remove(citaUrl);
            closeTab(citaUrl, citaTab);
        });

        TableColumn<List<StringProperty>, String> keyColumn = new TableColumn<>("key");
        TableColumn<List<StringProperty>, String> valueColumn = new TableColumn<>("value");
        keyColumn.setCellValueFactory(data -> data.getValue().get(0));
        valueColumn.setCellValueFactory(data -> data.getValue().get(1));
        tableView.getColumns().addAll(keyColumn, valueColumn);

        ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
        List<List<StringProperty>> bcInfo = chainControl.getBcInfo();

        for (List<StringProperty> list: bcInfo) {
            List<StringProperty> row = new ArrayList<>();
            row.add(0, list.get(0));
            row.add(1, list.get(1));
            data.add(row);
        }
        tableView.setItems(data);
        logger.debug("[showCita] end");
    }

}
