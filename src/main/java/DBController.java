import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

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

    public Connection getConnection() {
        return connection;
    }

    ResultSet queryTables() {
        String tableQuery = "SELECT * FROM sqlite_master WHERE type='table' ORDER BY name";
        ResultSet tableNames = null;
        try (PreparedStatement tableQueryPS = connection.prepareStatement(tableQuery)) {
            tableNames = tableQueryPS.executeQuery();
        } catch (SQLException tableQueryException) {
            System.err.println(tableQueryException.toString());
        }
        return tableNames;
    }

    ResultSet queryData(ResultSet tableNames) {
        ResultSet tableValues = null;
        try {
            String dataQuery = "SELECT * from " + tableNames.getString("name");
            tableValues = connection.createStatement().executeQuery(dataQuery);
        } catch(SQLException dataQueryException) {
            System.err.println(dataQueryException.toString());
        }
            return tableValues;
    }

    void disconnect() {
        this.connection = null;
        System.out.println("Connection Closed");
    }

}
