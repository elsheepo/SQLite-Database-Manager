import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DBController {

    private Connection connection;

    DBController(String databaseURL) {
        Connection connection = null;
        StringBuilder dbUrl = new StringBuilder("jdbc:sqlite:");
        dbUrl.append(databaseURL);

        try {
            connection = DriverManager.getConnection(dbUrl.toString());
            System.out.println("Connection to database established");
        } catch (SQLException connectionException) {
            System.err.println(connectionException.toString());
            new Alert(Alert.AlertType.ERROR, "There was a problem connecting to the database. Please check that the url is correct.").showAndWait();
        }
        this.connection = connection;
    }

    Connection getConnection() {
        return connection;
    }

    List queryTables() {
        String statement = "SELECT * FROM sqlite_master WHERE type='table' ORDER BY name";
        List<String> tableNames = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("name"));
            }
            resultSet.close();
        } catch (SQLException tableQueryException) {
            System.err.println(tableQueryException.toString());
        }
        return tableNames;
    }

    Map queryColumns(String tableName) {
        String statement = "PRAGMA table_info(" + tableName + ")";
        Map<String, String> columns = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                columns.put(resultSet.getString("name"), resultSet.getString("type"));
            }
            resultSet.close();
        } catch (SQLException columnQueryException) {
            System.err.println(columnQueryException.toString());
        }
        return columns;
    }

    ObservableList queryRows(String tableName) {
        String statement = "SELECT * from " + tableName;
        ObservableList<ObservableList> rows = FXCollections.observableArrayList();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    row.add(resultSet.getString(i));
                }
                rows.add(row);
            }
            resultSet.close();
        } catch (SQLException rowQueryException) {
            System.err.println(rowQueryException.toString());
        }
        return rows;
    }

    void disconnect() {
        this.connection = null;
        System.out.println("Connection Closed");
    }


}
