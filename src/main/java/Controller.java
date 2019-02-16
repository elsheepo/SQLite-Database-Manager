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
import java.util.ResourceBundle;


public class Controller implements Initializable {

    private final FileChooser fileChooser = new FileChooser();

    private Stage stage = new Stage();

    private File dbFile;
    private Connection connection;
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
    private void displayFileChooser() {
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            dbFile = file;
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
    private void handleConnect() {
        if (dbUrlTxt.getText().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Database url must be specified.").showAndWait();
        } else {
            dbController = new DBController(dbUrlTxt.getText());
            ResultSet tableNames = dbController.queryTables();

            try {
                while (tableNames.next()) {
                    ObservableList<ObservableList> data = FXCollections.observableArrayList();
                    Tab tab = new Tab(tableNames.getString("name"));
                    TableView<ObservableList> tableView = new TableView<>();
                    tabPane.getTabs().add(tab);
                    tab.setContent(tableView);

                    ResultSet tableValues = dbController.queryData(tableNames);

                    for (int i = 0; i < tableValues.getMetaData().getColumnCount(); i++) {
                        final int j = i;
                        int dataValue = tableValues.getMetaData().getColumnType(i + 1);
                        String dataValueString = null;

                        // I need to use some generics here on the TableColumn to get rid of the Unchecked call to setCellValueFactory()
                        TableColumn tableColumn = new TableColumn(tableValues.getMetaData().getColumnName(i + 1));
                        if (dataValue == 4) {
                            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<String>>) param ->
                                    new SimpleStringProperty(param.getValue().get(j).toString()));
                            dataValueString = "Integer";
                        } else if (dataValue == 7) {
                            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, Double>, ObservableValue<String>>) param ->
                                    new SimpleStringProperty(param.getValue().get(j).toString()));
                            dataValueString = "Double";
                        } else if (dataValue == 12) {
                            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param ->
                                    new SimpleStringProperty(param.getValue().get(j).toString()));
                            dataValueString = "String";
                        }
                        tableView.getColumns().addAll(tableColumn);
                    }
                    int rowCounter = 0;

                    while (tableValues.next()) {
                        ObservableList<String> row = FXCollections.observableArrayList();

                        for (int i = 1; i <= tableValues.getMetaData().getColumnCount(); i++) {
                            row.add(tableValues.getString(i));
                        }
                        data.add(row);
                        rowCounter++;
                    }
                    tableValues.close();
                    tableView.getItems().addAll(data);
                }
                tableNames.close();
                System.out.println("ResultSet tableNames closed");

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

    private void add() {
        
    }

    public void disconnectOnClick() {
        handleDisconnect();
    }

    public void disconnectOnReturn(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER)) {
            handleDisconnect();
        }
    }

    private void handleDisconnect() {
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

    private void handleExit() {
        new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to quit?").showAndWait();
        if (dbController.getConnection() != null) {
            dbController.disconnect();
        }
        Platform.exit();
    }

}
