package controllers;

import helpers.Project;
import helpers.User;
import helpers.Utils;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsersControllers implements Initializable {

    @FXML
    public TableView<User> tableView;

    @FXML
    public TextField idField;

    @FXML
    public TextField emailField;

    @FXML
    public TextField passwordField;

    @FXML
    public TextField nameField;

    @FXML
    public TextField addressField;

    @FXML
    public TextField phoneField;

    private final ObservableList<User> users = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Table
        tableView.setEditable(true);

        TableColumn<User, Integer> colId = new TableColumn<>("ID".toUpperCase());
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> colEmail = new TableColumn<>("E-mail".toUpperCase());
        colEmail.setMinWidth(150);
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setCellFactory(TextFieldTableCell.forTableColumn());
        colEmail.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setName(event.getNewValue());

            String query = String.format("UPDATE \"USER\" SET email = '%s' WHERE idno = %d", event.getNewValue(), event.getRowValue().getId());
            Utils.getInstance().executeQuery(query);
        });

        TableColumn<User, String> colPassword = new TableColumn<>("Парола".toUpperCase());
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        colPassword.setCellFactory(TextFieldTableCell.forTableColumn());
        colPassword.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setName(event.getNewValue());

            String query = String.format("UPDATE \"USER\" SET password = '%s' WHERE idno = %d", event.getNewValue(), event.getRowValue().getId());
            Utils.getInstance().executeQuery(query);
        });

        TableColumn<User, String> colName = new TableColumn<>("Име".toUpperCase());
        colName.setMinWidth(150);
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(event -> {
            String firstName = "";
            String lastName = "";
            try {
                String[] nameSplit = event.getNewValue().split("\\s+");
                firstName = nameSplit[0];
                lastName = nameSplit[1];
            } catch (Exception e) {
                Utils.getInstance().showMessage(Alert.AlertType.ERROR, "Грешка", "Форматът за името е: Име Фамилия.");
                return;
            }

            event.getTableView().getItems().get(event.getTablePosition().getRow()).setName(event.getNewValue());

            String query = String.format("UPDATE \"USER\" SET name = name_t('%s', '%s') WHERE idno = %d", firstName, lastName, event.getRowValue().getId());
            Utils.getInstance().executeQuery(query);
        });

        TableColumn<User, String> colAddress = new TableColumn<>("Град".toUpperCase());
        colAddress.setMinWidth(150);
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colAddress.setCellFactory(TextFieldTableCell.forTableColumn());
        colAddress.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setName(event.getNewValue());

            String query = String.format("UPDATE \"USER\" u SET u.address.city = '%s' WHERE idno = %d", event.getNewValue(), event.getRowValue().getId());
            Utils.getInstance().executeQuery(query);
        });

        TableColumn<User, String> colPhone = new TableColumn<>("Телефон".toUpperCase());
        colPhone.setMinWidth(130);
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setCellFactory(TextFieldTableCell.forTableColumn());
        colPhone.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setName(event.getNewValue());

            String query = String.format("UPDATE \"USER\" SET phone = '%s' WHERE idno = %d", event.getNewValue(), event.getRowValue().getId());
            Utils.getInstance().executeQuery(query);
        });

        // Add columns to the table
        tableView.getColumns().addAll(colId, colEmail, colPassword, colName, colAddress, colPhone);

        // Context menu
        createContextMenu();
    }

    /**
     * Loads data from the database.
     *
     * @throws SQLException
     */
    public void loadData() throws SQLException {
        String query = "SELECT u.idno AS id, u.email AS email, u.password AS password, (u.name.first_name || ' ' || u.name.last_name) AS name, " +
                "u.address.city AS address, u.phone AS phone " +
                "FROM \"USER\" u " +
                "ORDER BY u.idno DESC";

        ResultSet resultSet = Utils.getInstance().executeQuery(query);

        while (resultSet.next()) {
            users.add(new User(resultSet.getInt("id"), resultSet.getString("email"), resultSet.getString("password"),
                    resultSet.getString("name"), resultSet.getString("address"), resultSet.getString("phone")));
        }

        tableView.setItems(users);
    }

    /**
     * Clears the table.
     */
    public void clear() {
        tableView.getItems().clear();
    }

    /**
     * Creates a context menu.
     */
    private void createContextMenu() {
        tableView.setRowFactory(tableView -> {
            final TableRow<User> row = new TableRow<>();
            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem removeMenuItem = new MenuItem("Изтрий");

            removeMenuItem.setOnAction(event -> {
                this.tableView.getItems().remove(row.getItem());

                String query = String.format("DELETE FROM \"USER\" WHERE idno = %d", row.getItem().getId());
                Utils.getInstance().executeQuery(query);
            });

            contextMenu.getItems().add(removeMenuItem);

            // Set context menu on row, but use a binding to make it only show for non-empty rows
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));

            return row;
        });
    }

    /**
     * Adds a new record into the database.
     *
     * @param actionEvent
     * @throws SQLException
     */
    public void addRecord(ActionEvent actionEvent) throws SQLException {
        // Validate empty fields
        if (idField.getText().isEmpty() || emailField.getText().isEmpty() || passwordField.getText().isEmpty() ||
                nameField.getText().isEmpty() || addressField.getText().isEmpty() || phoneField.getText().isEmpty()) {
            Utils.getInstance().showMessage(Alert.AlertType.ERROR, "Грешка", "Моля, попълнете всички полета.");
            return;
        }

        // Validate e-mail
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        Matcher matcher = pattern.matcher(emailField.getText());
        if (!matcher.matches()) {
            Utils.getInstance().showMessage(Alert.AlertType.ERROR, "Грешка", "Невалиден e-mail адрес.");
            return;
        }

        // Validate name
        String firstName = "";
        String lastName = "";
        try {
            String[] nameSplit = nameField.getText().split("\\s+");
            firstName = nameSplit[0];
            lastName = nameSplit[1];
        } catch (Exception e) {
            Utils.getInstance().showMessage(Alert.AlertType.ERROR, "Грешка", "Форматът за името е: Име Фамилия.");
            return;
        }

        // Validate address
        String province = "";
        String street = "";
        String city = "";
        String postalCode = "";
        try {
            String[] addressSplit = addressField.getText().split(",\\s+");
            province = addressSplit[0];
            street = addressSplit[1];
            city = addressSplit[2];
            postalCode = addressSplit[3];
        } catch (Exception e) {
            Utils.getInstance().showMessage(Alert.AlertType.ERROR, "Грешка", "Форматът за адреса е: Област, улица, град, пощенски код.");
            return;
        }

        // Add record into the database
        String query = String.format("INSERT INTO \"USER\" VALUES (%d, '%s', '%s', " +
                        "name_t('%s', '%s'), " +
                        "address_t('%s', '%s', '%s', '%s'), %s)",
                Integer.parseInt(idField.getText()), emailField.getText(), passwordField.getText(), firstName, lastName,
                province, street, city, postalCode, phoneField.getText());
        Utils.getInstance().executeQuery(query);

        // Show message
        Utils.getInstance().showMessage(Alert.AlertType.INFORMATION, "Информация", "Заявката беше изпълнена.");

        // Reload data
        clear();
        loadData();
    }
}
