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
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    private final FileChooser fileChooser = new FileChooser();

    private Stage stage = new Stage();

    private DBController dbController;

    @FXML
    private TextField dbUrlTxt;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button fileChooserBtn, connectBtn, disconnectBtn, addBtn, updateBtn, deleteBtn, saveBtn, exitBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbUrlTxt.setPromptText("select a sqlite database");
        disconnectBtn.setDisable(true);
        addBtn.setDisable(true);
        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
        saveBtn.setDisable(true);
    }

    public void displayFileChooserOnClick() {
        displayFileChooser();
    }

    public void displayFileChooserOnReturn(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER)) {
            displayFileChooser();
        }
    }

    public void displayFileChooser() {
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            dbUrlTxt.setText(file.getAbsolutePath());
        }
    }

    public void connectOnClick() {
        handleConnect();
    }

    public void connectOnReturn(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER)) {
            handleConnect();
        }
    }

    public void handleConnect() {
        if (dbUrlTxt.getText().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Database url must be specified.").showAndWait();
        } else {
            dbController = new DBController(dbUrlTxt.getText());
            List<String> tableNames = dbController.queryTables();
            for (String tableName : tableNames) {
                //ObservableList<ObservableList> data = FXCollections.observableArrayList();
                Tab tab = new Tab(tableName);
                TableView<ObservableList> tableView = new TableView<>();
                tabPane.getTabs().add(tab);
                tab.setContent(tableView);
                Map columns = dbController.queryColumns(tableName);
                columns.forEach((k, v) -> {
                    TableColumn tableColumn = new TableColumn(k.toString());
                    switch (v.toString()) {
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

//                ResultSet tableValues = dbController.queryData(tableName);
//
//                try {
//                    for (int i = 0; i < tableValues.getMetaData().getColumnCount(); i++) {
//                        final int j = i;
//                        int dataValue = tableValues.getMetaData().getColumnType(i + 1);
//
//                        TableColumn tableColumn = new TableColumn(tableValues.getMetaData().getColumnName(i + 1));

//                        if (dataValue == 4) {
//                            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<String>>) param ->
//                                    new SimpleStringProperty(param.getValue().get(j).toString()));
//                        } else if (dataValue == 7) {
//                            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, Double>, ObservableValue<String>>) param ->
//                                    new SimpleStringProperty(param.getValue().get(j).toString()));
//                        } else if (dataValue == 12) {
//                            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param ->
//                                    new SimpleStringProperty(param.getValue().get(j).toString()));
//                        }
//                        tableView.getColumns().addAll(tableColumn);
//                    }
//
//                    while (tableValues.next()) {
//                        ObservableList<String> row = FXCollections.observableArrayList();
//
//                        for (int i = 1; i <= tableValues.getMetaData().getColumnCount(); i++) {
//                            row.add(tableValues.getString(i));
//                        }
//                        data.add(row);
//                    }
//                    tableValues.close();
//                } catch (SQLException e) {
//                    e.toString();
//                }
//                        tableView.getItems().addAll(data);

                ObservableList<ObservableList> rows = dbController.queryRows(tableName);
                tableView.getItems().addAll(rows);
            }
        }

        connectBtn.setDisable(true);
        disconnectBtn.setDisable(false);
        addBtn.setDisable(false);
        updateBtn.setDisable(false);
        deleteBtn.setDisable(false);
        saveBtn.setDisable(false);

    }

    public void add() {
    }

    public void disconnectOnClick() {
        handleDisconnect();
    }

    public void disconnectOnReturn(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER)) {
            handleDisconnect();
        }
    }

    public void handleDisconnect() {
        // close database connection
        dbController.disconnect();

        // reset buttons
        connectBtn.setDisable(false);
        disconnectBtn.setDisable(true);
        addBtn.setDisable(true);
        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
        saveBtn.setDisable(true);

        // clear the TabPane
        tabPane.getTabs().clear();
    }

    public void exitOnClick() {
        handleExit();
    }

    public void exitOnReturn(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER))
            handleExit();
    }

    public void handleExit() {
        new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to quit?").showAndWait();
        if (dbController != null && dbController.getConnection() != null) {
            dbController.disconnect();
        }
        Platform.exit();
    }

}
