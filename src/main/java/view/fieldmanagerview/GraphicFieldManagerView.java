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
import view.ViewUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

public class GraphicFieldManagerView implements FieldManagerView {

    // Dipendenze essenziali
    private final FieldManagerController controller;
    private final User manager; // Rinominato da fieldManager per brevità
    private ApplicationController appController;
    private model.notification.NotificationService notificationService;
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
        this.notificationService = new model.notification.NotificationService();
        this.notificationService.subscribe(new FieldManagerNotificationObserver());
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
            ViewUtils.applyStylesheets(scene);

            stage.setTitle("Field Manager Dashboard - Sporty");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.severe("Dashboard load failed: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (stage != null)
            stage.close();
    }

    @FXML
    private javafx.scene.image.ImageView managerImageView;

    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        managerNameLabel.setText("Manager: " + manager.getName() + " " + manager.getSurname());

        // Load Manager Image
        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(
                    java.util.Objects.requireNonNull(getClass().getResourceAsStream("/image/manager.jpeg")),
                    120, 120, true, true);
            managerImageView.setImage(img);

            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(30, 30, 30);
            managerImageView.setClip(clip);
        } catch (Exception e) {
            logger.warning("Manager image not found: " + e.getMessage());
        }

        setupTable();
        loadData();
        Platform.runLater(this::checkAndShowNotifications);
    }

    private void checkAndShowNotifications() {
        if (notificationService == null)
            return;

        List<String> unread = notificationService.getUnreadNotifications(manager.getUsername());
        if (!unread.isEmpty()) {
            showNotificationsPopup(unread);
        }
    }

    private void showNotificationsPopup(List<String> notifications) {
        StringBuilder content = new StringBuilder();
        content.append("Hai ").append(notifications.size()).append(" nuove notifiche:\n\n");

        for (String notification : notifications) {
            content.append("🔔 ").append(notification).append("\n\n");
        }

        Alert alert = createStyledAlert(Alert.AlertType.INFORMATION,
                "Notifiche",
                content.toString());
        alert.setHeaderText("Nuove notifiche!");
        alert.showAndWait();

        notificationService.markAllAsRead(manager.getUsername());
    }

    private void setupTable() {
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
            totalFieldsLabel.setText(String.valueOf(stats.totalFields()));
            pendingRequestsLabel.setText(String.valueOf(stats.pendingRequests()));
            todayBookingsLabel.setText(String.valueOf(stats.todayBookings()));

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

        try {
            controller.rejectBooking(selected.getBookingId());
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
    private void handleViewBookings() {
        Alert alert = createStyledAlert(Alert.AlertType.INFORMATION, "Feature Coming Soon",
                "This feature will be available in a future update.");
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        if (stage != null)
            stage.close();
        if (appController != null)
            appController.logout();
    }

    /**
     * Creates a styled Alert dialog that matches the application's dark theme.
     */
    private Alert createStyledAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        ViewUtils.applyStylesheets(alert.getDialogPane());

        return alert;
    }

    // Unificato ShowError/Success/Info in un unico metodo logico
    private void showMessage(String msg, boolean isError) {
        messageLabel.getStyleClass().removeAll(Constants.CSS_ERROR, Constants.CSS_SUCCESS, Constants.CSS_INFO);
        messageLabel.getStyleClass().add(isError ? Constants.CSS_ERROR : Constants.CSS_SUCCESS);
        messageLabel.setText(msg);
    }
}
