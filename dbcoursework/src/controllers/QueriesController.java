package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.ResultSet;
import java.sql.SQLException;

public class QueriesController {

    @FXML
    public Label titleLabel;

    @FXML
    public TableView<ObservableList<String>> tableView;

    /**
     * Loads a ResultSet.
     *
     * @param resultSet the ResultSet.
     * @throws SQLException
     */
    public void loadResultSet(String titleLabel, ResultSet resultSet) throws SQLException {
        this.titleLabel.setText(titleLabel);

        addColumns(resultSet);
        addRecords(resultSet);
    }

    /**
     * Adds columns.
     *
     * @param resultSet the ResultSet.
     * @throws SQLException
     */
    private void addColumns(ResultSet resultSet) throws SQLException {
        for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
            final int j = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(resultSet.getMetaData().getColumnName(i + 1));
            col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j)));
            tableView.getColumns().add(col);
        }
    }

    /**
     * Adds records.
     *
     * @param resultSet the ResultSet.
     * @throws SQLException
     */
    private void addRecords(ResultSet resultSet) throws SQLException {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        while (resultSet.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                row.add(resultSet.getString(i));
            }
            data.add(row);
        }
        tableView.setItems(data);
    }
}
