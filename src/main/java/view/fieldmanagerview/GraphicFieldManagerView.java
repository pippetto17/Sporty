package view.fieldmanagerview;

import controller.ApplicationController;
import controller.FieldManagerController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.bean.MatchBean;
import model.domain.Notification;
import model.utils.Constants;
import view.ViewUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class GraphicFieldManagerView implements FieldManagerView {
    private final FieldManagerController controller;
    private ApplicationController appController;
    private Stage stage;
    private final Logger logger = Logger.getLogger(getClass().getName());
    private boolean notificationsShown = false;

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
    private TableView<MatchBean> bookingsTable;
    @FXML
    private TableColumn<MatchBean, String> fieldNameColumn;
    @FXML
    private TableColumn<MatchBean, String> requesterColumn;
    @FXML
    private TableColumn<MatchBean, String> dateColumn;
    @FXML
    private TableColumn<MatchBean, String> timeColumn;
    @FXML
    private TableColumn<MatchBean, String> typeColumn;
    @FXML
    private Button approveButton;
    @FXML
    private Button rejectButton;
    private final ObservableList<MatchBean> bookingsList = FXCollections.observableArrayList();
    @FXML
    private ImageView managerImageView;
    @FXML
    private Button notificationButton;

    public GraphicFieldManagerView(FieldManagerController controller) {
        this.controller = controller;
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

    @Override
    public void displayDashboard() {
        try {
            var stats = controller.getDashboardData();
            totalFieldsLabel.setText(String.valueOf(stats.totalFields()));
            pendingRequestsLabel.setText(String.valueOf(stats.pendingRequests()));
            todayBookingsLabel.setText(String.valueOf(stats.todayBookings()));
            bookingsList.setAll(controller.getPendingRequests());
        } catch (Exception e) {
            displayError(e.getMessage());
        }
    }

    @Override
    public void displayPendingRequests(List<MatchBean> requests) {
        bookingsList.setAll(requests);
    }

    @Override
    public void displayNotifications() {
        showNotificationsDialog(1);
    }

    @Override
    public void displayError(String message) {
        messageLabel.getStyleClass().removeAll(Constants.CSS_ERROR, Constants.CSS_SUCCESS, Constants.CSS_INFO);
        messageLabel.getStyleClass().add(Constants.CSS_ERROR);
        messageLabel.setText(message);
    }

    @Override
    public void displaySuccess(String message) {
        messageLabel.getStyleClass().removeAll(Constants.CSS_ERROR, Constants.CSS_SUCCESS, Constants.CSS_INFO);
        messageLabel.getStyleClass().add(Constants.CSS_SUCCESS);
        messageLabel.setText(message);
    }

    @FXML
    private void initialize() {
        managerNameLabel.setText(
                "Manager: " + controller.getFieldManager().getName() + " " + controller.getFieldManager().getSurname());
        try {
            Image img = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/image/manager.jpeg")),
                    120, 120, true, true);
            managerImageView.setImage(img);
            Circle clip = new Circle(30, 30, 30);
            managerImageView.setClip(clip);
        } catch (Exception e) {
            logger.warning("Manager image not found: " + e.getMessage());
        }
        setupTable();
        loadData();
        Platform.runLater(this::showUnreadNotificationsOnce);

        // Auto-open requests if pending
        if (!controller.getPendingRequests().isEmpty()) {
            Platform.runLater(() -> showNotificationsDialog(0)); // 0 is the Requests tab index
        }
    }

    private void showUnreadNotificationsOnce() {
        if (notificationsShown) {
            return;
        }
        notificationsShown = true;

        List<Notification> unread = controller.getUnreadNotifications();
        if (unread.isEmpty()) {
            updateNotificationButton(0);
            return;
        }

        // Show popup with unread notifications
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New Notifications");
        alert.setHeaderText("🔔 You have " + unread.size() + " new notification(s)!");

        StringBuilder content = new StringBuilder();
        for (Notification n : unread) {
            content.append("• ").append(n.getTitle()).append("\n  ").append(n.getMessage()).append("\n\n");
        }
        alert.setContentText(content.toString());
        ViewUtils.applyStylesheets(alert.getDialogPane());
        alert.showAndWait();

        // Mark as read after showing
        controller.markNotificationsAsRead();
        updateNotificationButton(0);
    }

    private void updateNotificationButton(int unreadCount) {
        if (unreadCount > 0) {
            notificationButton.setStyle("-fx-text-fill: -color-danger-fg; -fx-border-color: -color-danger-emphasis;");
            notificationButton.setText("🔔 (" + unreadCount + ")");
        } else {
            notificationButton.setStyle("");
            notificationButton.setText("🔔");
        }
    }

    @FXML
    private void handleShowNotifications() {
        showNotificationsDialog(1);
    }

    private void showNotificationsDialog(int initialTab) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Notification Center");
        dialog.setHeaderText("Notifications & Requests");
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab requestsTab = createRequestsTab();
        Tab alertsTab = createAlertsTab();
        tabPane.getTabs().addAll(requestsTab, alertsTab);

        if (initialTab >= 0 && initialTab < tabPane.getTabs().size()) {
            tabPane.getSelectionModel().select(initialTab);
        }

        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ViewUtils.applyStylesheets(dialog.getDialogPane());
        dialog.showAndWait();
    }

    private Tab createAlertsTab() {
        Tab alertsTab = new Tab("Alerts");
        VBox alertsBox = new VBox(10);
        alertsBox.setPadding(new javafx.geometry.Insets(10));
        List<Notification> unread = controller.getUnreadNotifications();
        populateAlertsBox(alertsBox, unread);
        alertsTab.setContent(new ScrollPane(alertsBox));
        return alertsTab;
    }

    private void populateAlertsBox(VBox alertsBox, List<Notification> unread) {
        if (unread.isEmpty()) {
            alertsBox.getChildren().add(new Label("No new notifications."));
            return;
        }
        for (Notification note : unread) {
            Label l = new Label("🔔 " + note.getTitle() + ": " + note.getMessage());
            l.setWrapText(true);
            alertsBox.getChildren().add(l);
        }
        controller.markNotificationsAsRead();
        updateNotificationButton(0);
    }

    private Tab createRequestsTab() {
        Tab requestsTab = new Tab("Pending Requests");
        VBox requestsBox = new VBox(10);
        requestsBox.setPadding(new javafx.geometry.Insets(10));
        List<MatchBean> pendingRequests = controller.getPendingRequests();
        populateRequestsBox(requestsBox, pendingRequests);
        requestsTab.setContent(new ScrollPane(requestsBox));
        return requestsTab;
    }

    private void populateRequestsBox(VBox requestsBox, List<MatchBean> pendingRequests) {
        if (pendingRequests.isEmpty()) {
            requestsBox.getChildren().add(new Label("No pending requests."));
            return;
        }
        for (MatchBean m : pendingRequests) {
            requestsBox.getChildren().add(createMatchRequestRow(m, requestsBox));
        }
    }

    private HBox createMatchRequestRow(MatchBean m, VBox requestsBox) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        VBox info = new VBox(2);
        Label title = new Label(m.getFieldName() + " - " + m.getMatchDate());
        title.setStyle("-fx-font-weight: bold");
        Label subtitle = new Label(m.getMatchTime() + " (" + m.getOrganizerName() + ")");
        info.getChildren().addAll(title, subtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button approveBtn = new Button("✓");
        approveBtn.getStyleClass().add("success-button");
        approveBtn.setOnAction(e -> handleApproveFromDialog(m, row, requestsBox));
        Button rejectBtn = new Button("✗");
        rejectBtn.getStyleClass().add("danger-button");
        rejectBtn.setOnAction(e -> handleRejectFromDialog(m, row, requestsBox));
        row.getChildren().addAll(info, spacer, approveBtn, rejectBtn);
        return row;
    }

    private void handleApproveFromDialog(MatchBean m, HBox row, VBox requestsBox) {
        try {
            controller.approveMatch(m.getMatchId());
            requestsBox.getChildren().remove(row);
            loadData();
            displaySuccess("Request approved!");
        } catch (Exception ex) {
            displayError("Error approving: " + ex.getMessage());
        }
    }

    private void handleRejectFromDialog(MatchBean m, HBox row, VBox requestsBox) {
        try {
            controller.rejectMatch(m.getMatchId());
            requestsBox.getChildren().remove(row);
            loadData();
            displaySuccess("Request rejected");
        } catch (Exception ex) {
            displayError("Error rejecting: " + ex.getMessage());
        }
    }

    private void setupTable() {
        fieldNameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFieldName()));
        requesterColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrganizerName()));
        typeColumn.setCellValueFactory(d -> {
            var sport = d.getValue().getSport();
            return new SimpleStringProperty(sport != null ? sport.getDisplayName() : "N/A");
        });
        dateColumn.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getMatchDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        timeColumn.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getMatchTime().toString()));
        bookingsTable.setItems(bookingsList);
        bookingsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean active = selected != null;
            approveButton.setDisable(!active);
            rejectButton.setDisable(!active);
        });
    }

    private void loadData() {
        displayDashboard();
    }

    @FXML
    private void handleApprove() {
        MatchBean selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;
        try {
            controller.approveMatch(selected.getMatchId());
            displaySuccess("Match approved!");
            loadData();
        } catch (Exception e) {
            displayError("Approval failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleReject() {
        MatchBean selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;
        try {
            controller.rejectMatch(selected.getMatchId());
            displaySuccess("Match rejected");
            loadData();
        } catch (Exception e) {
            displayError("Rejection failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
        displaySuccess("Dashboard refreshed");
    }

    @FXML
    private void handleManageFields() {
        displayError("Feature disabled");
    }

    @FXML
    private void handleAddField() {
        displayError("Feature disabled");
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

    private Alert createStyledAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        ViewUtils.applyStylesheets(alert.getDialogPane());
        return alert;
    }
}