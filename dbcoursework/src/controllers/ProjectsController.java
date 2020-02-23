package controllers;

import helpers.Project;
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

public class ProjectsController implements Initializable {

    @FXML
    private TableView<Project> tableView;

    @FXML
    public TextField idField;

    @FXML
    public TextField nameField;

    @FXML
    public TextField descriptionField;

    @FXML
    private ComboBox<String> customerCombo;

    @FXML
    private ComboBox<String> serviceCombo;

    private final ObservableList<Project> projects = FXCollections.observableArrayList();

    /**
     * Initiates controls.
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add customer options
        ObservableList<String> customers = Utils.getInstance().getCustomerOptions();
        customerCombo.setItems(customers);
        customerCombo.getSelectionModel().select(0);

        // Add service options
        ObservableList<String> services = Utils.getInstance().getServiceOptions();
        serviceCombo.setItems(services);
        serviceCombo.getSelectionModel().select(0);

        // Table
        tableView.setEditable(true);

        TableColumn<Project, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Project, String> colName = new TableColumn<>("Име на проект".toUpperCase());
        colName.setMinWidth(200);
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setName(event.getNewValue());

            String query = String.format("UPDATE project SET name = '%s' WHERE projectno = %d", event.getNewValue(), event.getRowValue().getId());
            Utils.getInstance().executeQuery(query);
        });

        TableColumn<Project, String> colDescription = new TableColumn<>("Описание".toUpperCase());
        colDescription.setMinWidth(250);
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setCellFactory(TextFieldTableCell.forTableColumn());
        colDescription.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setName(event.getNewValue());

            String query = String.format("UPDATE project SET description = '%s' WHERE projectno = %d", event.getNewValue(), event.getRowValue().getId());
            Utils.getInstance().executeQuery(query);
        });

        TableColumn<Project, String> colCustomer = new TableColumn<>("Клиент".toUpperCase());
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));

        TableColumn<Project, String> colService = new TableColumn<>("Услуга".toUpperCase());
        colService.setCellValueFactory(new PropertyValueFactory<>("service"));

        // Add columns to the table
        tableView.getColumns().addAll(colId, colName, colDescription, colCustomer, colService);

        // Context menu
        createContextMenu();
    }

    /**
     * Loads data from the database.
     *
     * @throws SQLException
     */
    public void loadData() throws SQLException {
        String query = "SELECT p.projectno AS id, p.name AS name, p.description AS description, (p.customer.name.first_name || ' ' || p.customer.name.last_name) AS customer, " +
                "p.service.name AS service " +
                "FROM project p " +
                "ORDER BY p.projectno DESC";

        ResultSet resultSet = Utils.getInstance().executeQuery(query);

        while (resultSet.next()) {
            projects.add(new Project(resultSet.getInt("id"), resultSet.getString("name"),
                    resultSet.getString("description"), resultSet.getString("customer"), resultSet.getString("service")));
        }

        tableView.setItems(projects);
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
            final TableRow<Project> row = new TableRow<>();
            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem removeMenuItem = new MenuItem("Изтрий");

            removeMenuItem.setOnAction(event -> {
                this.tableView.getItems().remove(row.getItem());

                String query = String.format("DELETE FROM project WHERE projectno = %d", row.getItem().getId());
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
     */
    public void addRecord(ActionEvent actionEvent) throws SQLException {
        // Validate empty fields
        if (idField.getText().isEmpty() || nameField.getText().isEmpty() || descriptionField.getText().isEmpty() ||
                customerCombo.getSelectionModel().isEmpty() || serviceCombo.getSelectionModel().isEmpty()) {
            Utils.getInstance().showMessage(Alert.AlertType.ERROR, "Грешка", "Моля, попълнете всички полета.");
            return;
        }

        // Validate customer
        Pattern pattern = Pattern.compile("^[\\w ]*\\(ID: (?<customerId>[0-9]*)\\)$");
        Matcher matcher = pattern.matcher(customerCombo.getSelectionModel().getSelectedItem());
        if (!matcher.matches())
            return;

        // Add record into the database
        String query = String.format("INSERT INTO project VALUES (%d, '%s', '%s', " +
                        "(SELECT TREAT(REF(u) AS REF customer_t) FROM \"USER\" u WHERE idno = %d AND VALUE(u) IS OF TYPE (customer_t)), " +
                        "(SELECT REF(s) FROM service s WHERE name = '%s'))",
                Integer.parseInt(idField.getText()),
                nameField.getText(), descriptionField.getText(), Integer.parseInt(matcher.group("customerId")),
                serviceCombo.getSelectionModel().getSelectedItem());
        Utils.getInstance().executeQuery(query);

        // Show message
        Utils.getInstance().showMessage(Alert.AlertType.INFORMATION, "Информация", "Заявката беше изпълнена.");

        // Reload data
        clear();
        loadData();
    }
}
