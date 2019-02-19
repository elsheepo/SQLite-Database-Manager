import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static java.sql.DriverManager.getConnection;


public class Controller implements Initializable {

    private Connection connection;

    private final FileChooser fileChooser = new FileChooser();

    private Stage stage = new Stage();

    private DBController dbController;

    @FXML
    private TextField dbUrlTxt;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button connectBtn, disconnectBtn, addBtn, updateBtn, deleteBtn, saveBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbUrlTxt.setPromptText("select a sqlite database");
        toggleButtons();
    }

    public void displayFileChooserOnClick() {
        handleDisplayFileChooser();
    }

    public void displayFileChooserOnReturn(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER)) {
            handleDisplayFileChooser();
        }
    }

    private void handleDisplayFileChooser() {
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            dbUrlTxt.setText(file.getAbsolutePath());
        }
    }

    public void connectOnClick() {
        //handleConnect();
        connect();
    }

    public void connectOnReturn(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER)) {
            //handleConnect();
            connect();
        }
    }

    private void handleConnect() {
        if (dbUrlTxt.getText().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Database url must be specified.").showAndWait();
        } else {
            dbController = new DBController(dbUrlTxt.getText());
            toggleButtons();

            //process table
            List<String> tableNames = dbController.queryTables();
            for (String tableName : tableNames) {
                Tab tab = new Tab(tableName);
                tabPane.getTabs().add(tab);
                TableView<ObservableList> tableView = new TableView<>();
                tab.setContent(tableView);

                //process columns
                Map<String, String> columns = dbController.queryColumns(tableName);
                columns.forEach((columnName, columnType) -> {
                    TableColumn tableColumn = new TableColumn(columnName);
                    switch (columnType) {
                        case "INTEGER":
                            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<String>>) param ->
                                    new SimpleStringProperty(param.getValue().toString()));
                        case "REAL":
                            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, Double>, ObservableValue<String>>) param ->
                                    new SimpleStringProperty(param.getValue().toString()));
                        case "TEXT":
                        case "NONE":
                        default:
                            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param ->
                                    new SimpleStringProperty(param.getValue().toString()));
                    }
                    tableView.getColumns().addAll(tableColumn);
                });

                // process rows
                ObservableList<ObservableList> rows = dbController.queryRows(tableName);
                tableView.getItems().addAll(rows);
            }
        }
    }

    private void connect() {
        StringBuilder dbUrl = new StringBuilder("jdbc:sqlite:");
        if (dbUrlTxt.getText().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Database url must be specified.").showAndWait();
        } else {
            try {
                connection = getConnection(dbUrl.append(dbUrlTxt.getText()).toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            String tableQuery = "SELECT * FROM sqlite_master WHERE type='table' ORDER BY name";
            try (PreparedStatement tableQueryPS = connection.prepareStatement(tableQuery)) {
                ResultSet tableNames = tableQueryPS.executeQuery();
                while (tableNames.next()) {
                    ObservableList<ObservableList> data = FXCollections.observableArrayList();
                    Tab tab = new Tab(tableNames.getString("name"));
                    TableView<ObservableList> tableView = new TableView<>();
                    tabPane.getTabs().add(tab);
                    tab.setContent(tableView);
                    String dataQuery = "SELECT * from " + tableNames.getString("name") ;
                    ResultSet tableValues = connection.createStatement().executeQuery(dataQuery);

                    for (int i = 0; i < tableValues.getMetaData().getColumnCount(); i++) {
                        final int j = i;
                        int dataValue = tableValues.getMetaData().getColumnType(i + 1);
                        // I need to use some generics here on the TableColumn to get rid of the Unchecked call to setCellValueFactory()
                        TableColumn tableColumn = new TableColumn(tableValues.getMetaData().getColumnName(i + 1));
                        if (dataValue == 4) {
                            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<String>>) param ->
                                    new SimpleStringProperty(param.getValue().get(j).toString()));
                        } else if (dataValue == 7) {
                            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, Double>, ObservableValue<String>>) param ->
                                    new SimpleStringProperty(param.getValue().get(j).toString()));
                        } else if (dataValue == 12) {
                            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param ->
                                    new SimpleStringProperty(param.getValue().get(j).toString()));
                        }
                        tableView.getColumns().addAll(tableColumn);
                    }
                    while (tableValues.next()) {
                        ObservableList<String> row = FXCollections.observableArrayList();
                        for (int i = 1; i <= tableValues.getMetaData().getColumnCount(); i++) {
                            row.add(tableValues.getString(i));
                        }
                        data.add(row);
                    }
                    tableValues.close();
                    tableView.getItems().addAll(data);
                }
                tableNames.close();
            } catch (SQLException tableQueryException) {
                System.err.println(tableQueryException.toString());
            }
            connectBtn.setDisable(true);
            disconnectBtn.setDisable(false);
            addBtn.setDisable(false);
            updateBtn.setDisable(false);
            deleteBtn.setDisable(false);
            saveBtn.setDisable(false);
        }
    }

    public void disconnectOnClick() {
        //handleDisconnect();
        disconnect();
    }

    public void disconnectOnReturn(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER)) {
           //handleDisconnect();
            disconnect();
        }
    }
    private void disconnect() {
        connection = null;
        tabPane.getTabs().clear();
        connectBtn.setDisable(false);
        disconnectBtn.setDisable(true);
        addBtn.setDisable(true);
        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
        saveBtn.setDisable(true);
    }
    private void handleDisconnect() {
        dbController.disconnect();
        toggleButtons();
        tabPane.getTabs().clear();
    }

    public void exitOnClick() {
        handleExit();
    }

    public void exitOnReturn(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER))
            handleExit();
    }

    private void handleExit() {
        new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to quit?").showAndWait();
        if (dbController != null && dbController.getConnection() != null) {
            dbController.disconnect();
        }
        Platform.exit();
    }

    private void toggleButtons() {
        if (dbController != null && dbController.getConnection() != null) {
            connectBtn.setDisable(true);
            disconnectBtn.setDisable(false);
            addBtn.setDisable(false);
            updateBtn.setDisable(false);
            deleteBtn.setDisable(false);
            saveBtn.setDisable(false);
        } else {
            connectBtn.setDisable(false);
            disconnectBtn.setDisable(true);
            addBtn.setDisable(true);
            updateBtn.setDisable(true);
            deleteBtn.setDisable(true);
            saveBtn.setDisable(true);
        }
    }

}
