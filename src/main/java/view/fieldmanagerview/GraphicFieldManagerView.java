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
import javafx.scene.layout.*;
import javafx.stage.Stage;

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
    private TableView<model.bean.MatchBean> bookingsTable;
    @FXML
    private TableColumn<model.bean.MatchBean, String> fieldNameColumn;
    @FXML
    private TableColumn<model.bean.MatchBean, String> requesterColumn;
    @FXML
    private TableColumn<model.bean.MatchBean, String> dateColumn;
    @FXML
    private TableColumn<model.bean.MatchBean, String> timeColumn;
    @FXML
    private TableColumn<model.bean.MatchBean, String> typeColumn;
    @FXML
    private Button approveButton;
    @FXML
    private Button rejectButton;

    private final ObservableList<model.bean.MatchBean> bookingsList = FXCollections.observableArrayList();

    public GraphicFieldManagerView(FieldManagerController controller, User manager) {
        this.controller = controller;
        this.manager = manager;
    }

    @Override
    public void setApplicationController(ApplicationController appController) {
        this.appController = appController;
        this.notificationService = model.notification.NotificationService.getInstance();
        this.notificationService.subscribe(new model.notification.FieldManagerNotificationObserver());
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

    @FXML
    private Button notificationButton;

    private void checkAndShowNotifications() {
        if (notificationService == null)
            return;

        List<String> unread = notificationService.getUnreadNotifications(manager.getUsername());
        if (!unread.isEmpty()) {
            notificationButton.setStyle("-fx-text-fill: -color-danger-fg; -fx-border-color: -color-danger-emphasis;");
            notificationButton.setText("🔔 (" + unread.size() + ")");
        } else {
            notificationButton.setStyle("");
            notificationButton.setText("🔔");
        }
    }

    @FXML
    private void handleShowNotifications() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Notification Center");
        dialog.setHeaderText("Notifications & Requests");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(createRequestsTab(), createAlertsTab());

        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ViewUtils.applyStylesheets(dialog.getDialogPane());

        dialog.showAndWait();
    }

    private Tab createAlertsTab() {
        Tab alertsTab = new Tab("Alerts");
        VBox alertsBox = new VBox(10);
        alertsBox.setPadding(new javafx.geometry.Insets(10));

        List<String> unread = notificationService.getUnreadNotifications(manager.getUsername());
        populateAlertsBox(alertsBox, unread);

        alertsTab.setContent(new ScrollPane(alertsBox));
        return alertsTab;
    }

    private void populateAlertsBox(VBox alertsBox, List<String> unread) {
        if (unread.isEmpty()) {
            alertsBox.getChildren().add(new Label("No new notifications."));
            return;
        }

        for (String note : unread) {
            Label l = new Label("🔔 " + note);
            l.setWrapText(true);
            alertsBox.getChildren().add(l);
        }
        notificationService.markAllAsRead(manager.getUsername());
        checkAndShowNotifications();
    }

    private Tab createRequestsTab() {
        Tab requestsTab = new Tab("Pending Requests");
        VBox requestsBox = new VBox(10);
        requestsBox.setPadding(new javafx.geometry.Insets(10));

        List<model.bean.MatchBean> pendingRequests = controller.getPendingRequests();
        populateRequestsBox(requestsBox, pendingRequests);

        requestsTab.setContent(new ScrollPane(requestsBox));
        return requestsTab;
    }

    private void populateRequestsBox(VBox requestsBox, List<model.bean.MatchBean> pendingRequests) {
        if (pendingRequests.isEmpty()) {
            requestsBox.getChildren().add(new Label("No pending requests."));
            return;
        }

        for (model.bean.MatchBean m : pendingRequests) {
            requestsBox.getChildren().add(createMatchRequestRow(m, requestsBox));
        }
    }

    private HBox createMatchRequestRow(model.bean.MatchBean m, VBox requestsBox) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox info = new VBox(2);
        Label title = new Label(m.getFieldName() + " - " + m.getMatchDate());
        title.setStyle("-fx-font-weight: bold");
        Label subtitle = new Label(m.getMatchTime() + " (" + m.getOrganizerName() + ")");
        info.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button approveBtn = new Button("✓");
        approveBtn.getStyleClass().add("success-button");
        approveBtn.setOnAction(e -> handleApproveFromDialog(m, row, requestsBox));

        Button rejectBtn = new Button("✗");
        rejectBtn.getStyleClass().add("danger-button");
        rejectBtn.setOnAction(e -> handleRejectFromDialog(m, row, requestsBox));

        row.getChildren().addAll(info, spacer, approveBtn, rejectBtn);
        return row;
    }

    private void handleApproveFromDialog(model.bean.MatchBean m, HBox row, VBox requestsBox) {
        try {
            controller.approveMatch(m.getMatchId());
            requestsBox.getChildren().remove(row);
            loadData();
            showMessage("Request approved!", false);
        } catch (Exception ex) {
            showMessage("Error approving: " + ex.getMessage(), true);
        }
    }

    private void handleRejectFromDialog(model.bean.MatchBean m, HBox row, VBox requestsBox) {
        try {
            controller.rejectMatch(m.getMatchId());
            requestsBox.getChildren().remove(row);
            loadData();
            showMessage("Request rejected", false);
        } catch (Exception ex) {
            showMessage("Error rejecting: " + ex.getMessage(), true);
        }
    }

    private void setupTable() {
        fieldNameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFieldName()));
        requesterColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrganizerName()));
        typeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSport().getDisplayName()));
        dateColumn.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getMatchDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        timeColumn.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getMatchTime().toString()));

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
    private void handleApprove() {
        model.bean.MatchBean selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        try {
            controller.approveMatch(selected.getMatchId());
            showMessage("Match approved!", false);
            loadData();
        } catch (Exception e) {
            showMessage("Approval failed: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleReject() {
        model.bean.MatchBean selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        try {
            controller.rejectMatch(selected.getMatchId());
            showMessage("Match rejected", false);
            loadData();
        } catch (Exception e) {
            showMessage("Rejection failed: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
        showMessage("Dashboard refreshed", false);
    }

    @FXML
    private void handleManageFields() {
        showMessage("Feature disabled", true);
    }

    @FXML
    private void handleAddField() {
        showMessage("Feature disabled", true);
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
