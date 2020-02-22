package controllers;

import com.sun.media.sound.RIFFInvalidDataException;
import helpers.Utils;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private ComboBox<String> serviceCombo;

    @FXML
    private ComboBox<String> industryCombo;

    /**
     * Initiates controls.
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add service options
        ObservableList<String> services = Utils.getInstance().getServiceOptions();
        serviceCombo.setItems(services);
        serviceCombo.getSelectionModel().select(0);

        // Add industry options
        ObservableList<String> industries = Utils.getInstance().getIndustryOptions();
        industryCombo.setItems(industries);
        industryCombo.getSelectionModel().select(0);
    }

    /**
     * This method shows only the chosen projects in the filters.
     *
     * @param actionEvent
     * @throws IOException
     */
    public void showFilteredProjects(ActionEvent actionEvent) throws IOException {
        if (serviceCombo.getSelectionModel().isEmpty() || industryCombo.getSelectionModel().isEmpty()) {
            Utils.getInstance().showMessage(Alert.AlertType.ERROR, "Грешка", "Моля, изберете всички нужни опции.");
            return;
        }

        String serviceName = serviceCombo.getSelectionModel().getSelectedItem();
        String industry = industryCombo.getSelectionModel().getSelectedItem();

        String query = String.format("SELECT p.projectno AS ID, p.name AS Име, p.description AS Описание, CONCAT(CONCAT(p.customer.name.first_name, ' '), p.customer.name.last_name) AS Клиент, p.service.name AS Услуга, pi.industry.name AS Индустрия " +
                "FROM project p INNER JOIN project_industry pi ON p.projectno = pi.project.projectno " +
                "WHERE p.service.name = '%s' AND pi.industry.name = '%s' " +
                "ORDER BY p.projectno DESC", serviceName, industry);

        ResultSet resultSet = Utils.getInstance().executeQuery(query);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/queries.fxml"));
        Parent tableParent = fxmlLoader.load();

        QueriesController queriesController = fxmlLoader.getController();

        try {
            queriesController.loadResultSet("Справка за проекти (услуга: " + serviceName + ", индустрия: " + industry + ")", resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Справки");
        stage.setScene(new Scene(tableParent, 800, 400));
        stage.show();
    }

    /**
     * This method shows all projects in a new window.
     *
     * @param actionEvent
     * @throws IOException
     */
    public void showProjects(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/projects.fxml"));
        Parent tableParent = fxmlLoader.load();

        ProjectsController projectsController = fxmlLoader.getController();

        try {
            projectsController.loadData();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Проекти");
        stage.setScene(new Scene(tableParent, 800, 400));
        stage.show();
    }

    /**
     * This query displays the names of the customers who have more than 3 projects and the number of their projects.
     *
     * @param actionEvent
     * @throws IOException
     */
    public void queryOne(ActionEvent actionEvent) throws IOException {
        String query = String.format("SELECT (TREAT(REF(u) AS REF customer_t).name.first_name ||  ' ' || (TREAT(REF(u) AS REF customer_t).name.last_name)) AS Име, COUNT(p.projectno) AS Проекти " +
                "FROM \"USER\" u JOIN project p ON u.idno = p.customer.idno " +
                "WHERE VALUE(u) IS OF TYPE (customer_t) " +
                "GROUP BY (TREAT(REF(u) AS REF customer_t).name.first_name ||  ' ' || (TREAT(REF(u) AS REF customer_t).name.last_name)) " +
                "HAVING COUNT(p.projectno) >= 3");

        ResultSet resultSet = Utils.getInstance().executeQuery(query);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/queries.fxml"));
        Parent tableParent = fxmlLoader.load();

        QueriesController queriesController = fxmlLoader.getController();

        try {
            queriesController.loadResultSet("Имената на клиентите с повече от 3 проекта, както и броя на проектите.", resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Справки");
        stage.setScene(new Scene(tableParent, 800, 400));
        stage.show();
    }

    /**
     * This query displays administrator's names and emails.
     *
     * @param actionEvent
     * @throws IOException
     */
    public void queryTwo(ActionEvent actionEvent) throws IOException {
        String query = String.format("SELECT (TREAT(REF(u) AS REF staff_t).name.first_name ||  ' ' || (TREAT(REF(u) AS REF staff_t).name.last_name)) AS Име, " +
                "TREAT(REF(u) AS REF staff_t).email AS Имейл " +
                "FROM \"USER\" u JOIN \"RANK\" r ON TREAT(REF(u) AS REF staff_t).rank.rankno = r.rankno " +
                "WHERE VALUE(u) IS OF TYPE (staff_t) AND r.name = 'Administrator'");

        ResultSet resultSet = Utils.getInstance().executeQuery(query);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/queries.fxml"));
        Parent tableParent = fxmlLoader.load();

        QueriesController queriesController = fxmlLoader.getController();

        try {
            queriesController.loadResultSet("Имената и имейл адресите на администраторите.", resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Справки");
        stage.setScene(new Scene(tableParent, 800, 400));
        stage.show();
    }

    /**
     * This query display project names and tags for the projects with tag "Branding".
     *
     * @param actionEvent
     * @throws IOException
     */
    public void queryThree(ActionEvent actionEvent) throws IOException {
        String query = String.format("SELECT p.name AS Име, pt.tag.name AS Таг FROM project p JOIN project_tag pt ON p.projectno = pt.project.projectno " +
                "WHERE pt.tag.name = 'брандинг'");

        ResultSet resultSet = Utils.getInstance().executeQuery(query);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/queries.fxml"));
        Parent tableParent = fxmlLoader.load();

        QueriesController queriesController = fxmlLoader.getController();

        try {
            queriesController.loadResultSet("Имената на проектите с тагове \"брандинг\".", resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Справки");
        stage.setScene(new Scene(tableParent, 800, 400));
        stage.show();
    }
}
