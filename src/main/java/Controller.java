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
import javafx.util.Callback;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private Connection connection;

    @FXML
    public TextField dbUrlTxt;
    @FXML
    public TabPane tabPane;
    @FXML
    public Button connectBtn, disconnectBtn, addBtn, updateBtn, deleteBtn, saveBtn, exitBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbUrlTxt.setText("products.sqlite");
        disconnectBtn.setDisable(true);
        addBtn.setDisable(true);
        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
        saveBtn.setDisable(true);
    }

    public void connectOnClick() { connect(); }
    public void connectOnReturn(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER))
            connect();
    }
    private void connect() {

        StringBuilder dbUrl = new StringBuilder("jdbc:sqlite:");
        if (dbUrlTxt.getText().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Database url must be specified.").showAndWait();
        } else {
            connection = getConnection(dbUrl.append(dbUrlTxt.getText()).toString());
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
    private Connection getConnection(String url) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException connectionException) {
            System.err.println(connectionException.toString());
            new Alert(Alert.AlertType.ERROR, "There was a problem connecting to the database. Please check that the url is correct.").showAndWait();
        }
        return connection;
    }

    public void disconnectOnClick() { closeConnection(); }
    public void disconnectOnReturn(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER))
            closeConnection();
    }
    private void closeConnection() {

        // close connection and log to console
        connection = null;

        // reset buttons
        connectBtn.setDisable(false);
        disconnectBtn.setDisable(true);
        addBtn.setDisable(true);
        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
        saveBtn.setDisable(true);

        // close tabs
        tabPane.getTabs().clear();
    }

    public void exitOnClick() { exit(); }
    public void exitOnReturn(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER))
            exit();
    }
    private void exit() {
        new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to quit?").showAndWait();
        if (connection != null) {
            connection = null;
        }
        Platform.exit();
    }

}
