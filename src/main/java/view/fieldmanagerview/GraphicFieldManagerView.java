package view.fieldmanagerview;

import controller.FieldManagerController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.bean.BookingBean;
import model.domain.User;
import model.utils.Constants;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Graphic view for Field Manager Dashboard.
 * Displays pending booking requests and field statistics.
 */
public class GraphicFieldManagerView implements FieldManagerView {
    private final FieldManagerController controller;
    private final User fieldManager;
    private controller.ApplicationController applicationController;
    private Stage stage;

    // Statistics
    @FXML
    private Label managerNameLabel;
    @FXML
    private Label totalFieldsLabel;
    @FXML
    private Label pendingRequestsLabel;
    @FXML
    private Label todayBookingsLabel;

    // Table
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

    // Buttons
    @FXML
    private Button approveButton;
    @FXML
    private Button rejectButton;

    // Message
    @FXML
    private Label messageLabel;

    private final ObservableList<BookingBean> bookingsList = FXCollections.observableArrayList();

    public GraphicFieldManagerView(FieldManagerController controller, User fieldManager) {
        this.controller = controller;
        this.fieldManager = fieldManager;
    }

    @Override
    public void setApplicationController(controller.ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @Override
    public void display() {
        javafx.application.Platform.runLater(() -> {
            try {
                stage = new javafx.stage.Stage();
                stage.setTitle("Field Manager Dashboard - Sporty");

                // Load FXML
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/fxml/field_manager_dashboard.fxml"));
                loader.setController(this);
                javafx.scene.layout.VBox root = loader.load();

                // Load CSS
                javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 700);
                scene.getStylesheets().add(
                        getClass().getResource("/css/field_manager.css").toExternalForm());
                scene.getStylesheets().add(
                        getClass().getResource("/css/style.css").toExternalForm());
                scene.getStylesheets().add(
                        getClass().getResource("/css/controls-dark.css").toExternalForm());
                stage.setScene(scene);
                stage.setResizable(true);
                stage.show();

            } catch (Exception e) {
                showError("Error loading dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void close() {
        if (stage != null) {
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        // Set manager name
        managerNameLabel.setText("Manager: " + fieldManager.getName() + " " + fieldManager.getSurname());

        // Configure table columns
        fieldNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFieldName()));
        requesterColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRequesterUsername()));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getBookingDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        timeColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStartTime() + " - " + data.getValue().getEndTime()));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        priceColumn.setCellValueFactory(
                data -> new SimpleStringProperty(String.format("€%.2f", data.getValue().getTotalPrice())));

        // Set table data
        bookingsTable.setItems(bookingsList);

        // Enable buttons when row is selected
        bookingsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean hasSelection = newSelection != null;
                    approveButton.setDisable(!hasSelection);
                    rejectButton.setDisable(!hasSelection);
                });

        // Load initial data
        loadDashboardData();
    }

    private void loadDashboardData() {
        try {
            // Load dashboard stats
            FieldManagerController.DashboardData dashboardData = controller.getDashboardData();
            totalFieldsLabel.setText(String.valueOf(dashboardData.getTotalFields()));
            pendingRequestsLabel.setText(String.valueOf(dashboardData.getPendingRequests()));
            todayBookingsLabel.setText(String.valueOf(dashboardData.getTodayBookings()));

            // Load pending booking requests
            List<BookingBean> pendingBookings = controller.getPendingRequests();
            bookingsList.clear();
            bookingsList.addAll(pendingBookings);

        } catch (Exception e) {
            showError("Error loading dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleApprove() {
        BookingBean selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        try {
            controller.approveBooking(selected.getBookingId());
            showSuccess("Booking approved successfully!");
            loadDashboardData(); // Refresh
        } catch (Exception e) {
            showError("Error approving booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleReject() {
        BookingBean selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        // Show dialog for rejection reason
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Booking");
        dialog.setHeaderText("Reject booking from " + selected.getRequesterUsername());
        dialog.setContentText("Reason:");

        dialog.showAndWait().ifPresent(reason -> {
            if (reason == null || reason.trim().isEmpty()) {
                showError("Rejection reason is required");
                return;
            }

            try {
                controller.rejectBooking(selected.getBookingId(), reason);
                showSuccess("Booking rejected");
                loadDashboardData(); // Refresh
            } catch (Exception e) {
                showError("Error rejecting booking: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadDashboardData();
        showInfo("Dashboard refreshed");
    }

    @FXML
    private void handleManageFields() {
        if (applicationController != null) {
            applicationController.navigateToMyFields(controller);
        }
    }

    @FXML
    private void handleSetAvailability() {
        showInfo("Set Availability feature - To be implemented");
        // TODO: Navigate to Availability Editor view
    }

    @FXML
    private void handleAddField() {
        if (applicationController != null) {
            applicationController.navigateToAddField(controller);
        }
    }

    @FXML
    private void handleViewBookings() {
        showInfo("View All Bookings feature - To be implemented");
        // TODO: Navigate to All Bookings view
    }

    // Message helpers
    private void showError(String message) {
        messageLabel.getStyleClass().removeAll(Constants.CSS_SUCCESS, "info");
        messageLabel.getStyleClass().add(Constants.CSS_ERROR);
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.getStyleClass().removeAll(Constants.CSS_ERROR, "info");
        messageLabel.getStyleClass().add(Constants.CSS_SUCCESS);
        messageLabel.setText(message);
    }

    private void showInfo(String message) {
        messageLabel.getStyleClass().removeAll(Constants.CSS_ERROR, Constants.CSS_SUCCESS);
        messageLabel.getStyleClass().add("info");
        messageLabel.setText(message);
    }
}
