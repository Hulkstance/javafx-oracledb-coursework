package helpers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.sql.*;

/**
 * Singleton class supposed to handle database operations and other stuff.
 */
public class Utils {

    private Connection cachedConnection = null;

    private static final Utils instance = new Utils();

    /**
     * So people don't accidentally instantiate it.
     * The point of the singleton pattern is only one instance at a time.
     */
    private Utils() {
    }

    /**
     * Gets a static instance of this class.
     *
     * @return the instance.
     */
    public static Utils getInstance() {
        return instance;
    }

    /**
     * Gets the database connection.
     *
     * @return the connection.
     */
    private Connection getConnection() {
        try {
            if (cachedConnection == null || cachedConnection.isClosed() || !cachedConnection.isValid(10)) {
                System.out.println("Attempting to get a new connection to DB!");
                DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
                cachedConnection = DriverManager.getConnection(
                        "jdbc:oracle:thin:@172.16.251.135:1521:orcl", "c##ex19_svetlio_coursework", "123456");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cachedConnection;
    }

    /**
     * Executes SQL query.
     *
     * @param query the query.
     * @return the result.
     */
    public ResultSet executeQuery(String query) {
        ResultSet result = null;
        try {
            PreparedStatement stmt = getConnection().prepareStatement(query);
            result = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Shows a message.
     *
     * @param alertType the message type.
     * @param title     the title.
     * @param message   the message.
     */
    public void showMessage(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gets service options from the database.
     *
     * @return the service options.
     */
    public ObservableList<String> getServiceOptions() {
        ResultSet resultSet = Utils.getInstance().executeQuery("SELECT name FROM service");
        ObservableList<String> row = FXCollections.observableArrayList();
        try {
            while (resultSet.next()) {
                row.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row;
    }

    /**
     * Gets industry options from the database.
     *
     * @return the industry options.
     */
    public ObservableList<String> getIndustryOptions() {
        ResultSet resultSet = Utils.getInstance().executeQuery("SELECT name FROM industry");
        ObservableList<String> row = FXCollections.observableArrayList();
        try {
            while (resultSet.next()) {
                row.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row;
    }

    /**
     * Gets all customer options.
     *
     * @return the customer options.
     */
    public ObservableList<String> getCustomerOptions() {
        ResultSet resultSet = Utils.getInstance().executeQuery("SELECT TREAT(REF(u) AS REF customer_t).idno AS ID, CONCAT(CONCAT(TREAT(REF(u) AS REF customer_t).name.first_name, ' '), (TREAT(REF(u) AS REF customer_t).name.last_name)) AS name FROM \"USER\" u WHERE VALUE(u) IS OF TYPE (customer_t)");
        ObservableList<String> row = FXCollections.observableArrayList();
        try {
            while (resultSet.next()) {
                row.add(resultSet.getString("name") + " (ID: " + resultSet.getString("ID") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row;
    }
}
