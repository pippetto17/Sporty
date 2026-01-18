package view.fieldmanagerview;

import controller.ApplicationController;
import controller.FieldManagerController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.bean.BookingBean;
import model.domain.User;
import model.utils.Constants;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Logger;

public class GraphicFieldManagerView implements FieldManagerView {

    // Dipendenze essenziali
    private final FieldManagerController controller;
    private final User manager; // Rinominato da fieldManager per brevità
    private ApplicationController appController;
    private Stage stage;
    private final Logger logger = Logger.getLogger(getClass().getName());

    // UI Elements (Raggruppati per pulizia)
    @FXML
    private Label managerNameLabel;
    @FXML
    private Label totalFieldsLabel;
    @FXML
    private Label pendingRequestsLabel;
    @FXML
    private Label todayBookingsLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private TableView<BookingBean> bookingsTable;
    @FXML
    private TableColumn<BookingBean, String> fieldNameColumn;
    @FXML
    private TableColumn<BookingBean, String> requesterColumn;
    @FXML
    private TableColumn<BookingBean, String> dateColumn;
    @FXML
    private TableColumn<BookingBean, String> timeColumn;
    @FXML
    private TableColumn<BookingBean, String> typeColumn;
    @FXML
    private TableColumn<BookingBean, String> priceColumn;
    @FXML
    private Button approveButton;
    @FXML
    private Button rejectButton;

    private final ObservableList<BookingBean> bookingsList = FXCollections.observableArrayList();

    public GraphicFieldManagerView(FieldManagerController controller, User manager) {
        this.controller = controller;
        this.manager = manager;
    }

    @Override
    public void setApplicationController(ApplicationController appController) {
        this.appController = appController;
    }

    @Override
    public void display() {
        Platform.runLater(this::initStage);
    }

    private void initStage() {
        try {
            stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/field_manager_dashboard.fxml"));
            loader.setController(this);

            Scene scene = new Scene(loader.load(), 1000, 700);
            loadStyles(scene);

            stage.setTitle("Field Manager Dashboard - Sporty");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.severe("Dashboard load failed: " + e.getMessage());
        }
    }

    private void loadStyles(Scene scene) {
        String[] styles = { Constants.CSS_PATH_FIELD_MANAGER, Constants.CSS_PATH_STYLE, Constants.CSS_PATH_CONTROLS_DARK };
        for (String style : styles) {
            var resource = getClass().getResource(style);
            if (resource != null) {
                scene.getStylesheets().add(resource.toExternalForm());
            }
        }
    }

    @Override
    public void close() {
        if (stage != null)
            stage.close();
    }

    @FXML
    @SuppressWarnings("unused") // Called by FXML loader
    private void initialize() {
        managerNameLabel.setText("Manager: " + manager.getName() + " " + manager.getSurname());
        setupTable();
        loadData();
    }

    private void setupTable() {
        // Mapping colonne più compatto
        fieldNameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFieldName()));
        requesterColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRequesterUsername()));
        typeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));
        dateColumn.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getBookingDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        timeColumn.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getStartTime() + " - " + d.getValue().getEndTime()));
        priceColumn.setCellValueFactory(
                d -> new SimpleStringProperty(String.format("€%.2f", d.getValue().getTotalPrice())));

        bookingsTable.setItems(bookingsList);

        // Listener selezione riga
        bookingsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean active = selected != null;
            approveButton.setDisable(!active);
            rejectButton.setDisable(!active);
        });
    }

    private void loadData() {
        try {
            var stats = controller.getDashboardData(); // 'var' rende il codice meno verboso (Java 10+)
            totalFieldsLabel.setText(String.valueOf(stats.getTotalFields()));
            pendingRequestsLabel.setText(String.valueOf(stats.getPendingRequests()));
            todayBookingsLabel.setText(String.valueOf(stats.getTodayBookings()));

            bookingsList.setAll(controller.getPendingRequests());
        } catch (Exception e) {
            showMessage(e.getMessage(), true);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleApprove() {
        BookingBean selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        try {
            controller.approveBooking(selected.getBookingId());
            showMessage("Booking approved!", false);
            loadData();
        } catch (Exception e) {
            showMessage("Approval failed: " + e.getMessage(), true);
        }
    }

    @FXML
    @SuppressWarnings("unused") // Called by FXML
    private void handleReject() {
        BookingBean selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Booking");
        dialog.setHeaderText("Rejecting request from " + selected.getRequesterUsername());
        dialog.setContentText("Reason:");

        // Apply dark theme styling
        javafx.scene.control.DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().clear();
        String[] styles = { Constants.CSS_PATH_FIELD_MANAGER, Constants.CSS_PATH_STYLE, Constants.CSS_PATH_CONTROLS_DARK };
        for (String style : styles) {
            var resource = getClass().getResource(style);
            if (resource != null) {
                dialogPane.getStylesheets().add(resource.toExternalForm());
            }
        }

        Optional<String> result = dialog.showAndWait();

        // Guard Clause: esci se non c'è risultato o è vuoto
        if (result.isEmpty() || result.get().trim().isEmpty()) {
            if (result.isPresent())
                showMessage("Reason required", true);
            return;
        }

        try {
            controller.rejectBooking(selected.getBookingId(), result.get());
            showMessage("Booking rejected", false);
            loadData();
        } catch (Exception e) {
            showMessage("Rejection failed: " + e.getMessage(), true);
        }
    }

    @FXML
    @SuppressWarnings("unused") // Called by FXML
    private void handleRefresh() {
        loadData();
        showMessage("Dashboard refreshed", false);
    }

    @FXML // Navigazione semplificata con null check inline
    @SuppressWarnings("unused") // Called by FXML
    private void handleManageFields() {
        if (appController != null)
            appController.navigateToMyFields(controller);
    }

    @FXML
    @SuppressWarnings("unused") // Called by FXML
    private void handleAddField() {
        if (appController != null)
            appController.navigateToAddField(controller);
    }

    @FXML
    @SuppressWarnings("unused") // Called by FXML
    private void handleViewBookings() {
        Alert alert = createStyledAlert(Alert.AlertType.INFORMATION, "Feature Coming Soon",
                "This feature will be available in a future update.");
        alert.showAndWait();
    }

    /**
     * Creates a styled Alert dialog that matches the application's dark theme.
     */
    private Alert createStyledAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Apply dark theme styling
        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().clear();

        String[] styles = { Constants.CSS_PATH_FIELD_MANAGER, Constants.CSS_PATH_STYLE, Constants.CSS_PATH_CONTROLS_DARK };
        for (String style : styles) {
            var resource = getClass().getResource(style);
            if (resource != null) {
                dialogPane.getStylesheets().add(resource.toExternalForm());
            }
        }

        return alert;
    }

    // Unificato ShowError/Success/Info in un unico metodo logico
    private void showMessage(String msg, boolean isError) {
        messageLabel.getStyleClass().removeAll(Constants.CSS_ERROR, Constants.CSS_SUCCESS, Constants.CSS_INFO);
        messageLabel.getStyleClass().add(isError ? Constants.CSS_ERROR : Constants.CSS_SUCCESS);
        messageLabel.setText(msg);
    }
}
