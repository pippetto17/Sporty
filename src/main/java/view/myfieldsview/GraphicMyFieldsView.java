package view.myfieldsview;

import controller.FieldManagerController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.bean.FieldBean;
import view.ViewUtils;

import java.util.List;

/**
 * Graphic implementation of MyFieldsView showing all fields owned by manager.
 */
public class GraphicMyFieldsView implements MyFieldsView {
    private final FieldManagerController controller;
    private Stage stage;
    private VBox fieldsContainer;
    private Label messageLabel;
    private controller.ApplicationController applicationController;

    // CSS class constants
    private static final String CSS_FIELD_DETAIL = "field-detail";

    public GraphicMyFieldsView(FieldManagerController controller) {
        this.controller = controller;
    }

    @Override
    public void setApplicationController(controller.ApplicationController appController) {
        this.applicationController = appController;
    }

    @Override
    public void display() {
        Platform.runLater(() -> {
            stage = new Stage();
            stage.setTitle("My Fields");

            VBox root = createLayout();

            Scene scene = new Scene(root, 800, 600);
            ViewUtils.applyStylesheets(scene);

            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();

            // Load fields
            loadFields();
        });
    }

    private VBox createLayout() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("main-container");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("My Fields");
        titleLabel.getStyleClass().add("page-title");

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setOnAction(e -> loadFields());

        // Add spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button backButton = new Button("← Back");
        backButton.setOnAction(e -> {
            if (applicationController != null) {
                Platform.runLater(() -> {
                    close();
                    applicationController.back();
                });
            } else {
                Platform.runLater(this::close);
            }
        });

        header.getChildren().addAll(titleLabel, refreshButton, spacer, backButton);

        // Fields container
        fieldsContainer = new VBox(15);
        ScrollPane scrollPane = new ScrollPane(fieldsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("matches-scroll");
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        // Message label
        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        root.getChildren().addAll(header, scrollPane, messageLabel);
        return root;
    }

    private void loadFields() {
        try {
            List<FieldBean> fields = controller.getMyFields();
            displayFields(fields);
        } catch (Exception e) {
            displayError("Error loading fields: " + e.getMessage());
        }
    }

    @Override
    public void displayFields(List<FieldBean> fields) {
        Platform.runLater(() -> {
            fieldsContainer.getChildren().clear();

            if (fields == null || fields.isEmpty()) {
                Label emptyLabel = new Label("No fields yet. Add your first field to get started!");
                emptyLabel.getStyleClass().add("subtitle-label");
                fieldsContainer.getChildren().add(emptyLabel);
                return;
            }

            for (FieldBean field : fields) {
                VBox fieldCard = createFieldCard(field);
                fieldsContainer.getChildren().add(fieldCard);
            }
        });
    }

    private VBox createFieldCard(FieldBean field) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("field-card");

        // Field name and type
        HBox nameRow = new HBox(15);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(field.getName());
        nameLabel.getStyleClass().add("field-name");

        Label typeLabel = new Label(field.isIndoor() ? "Indoor" : "Outdoor");
        typeLabel.getStyleClass().add("status-badge");
        if (!field.isIndoor()) {
            typeLabel.setStyle("-fx-background-color: #3b82f6;");
        }

        nameRow.getChildren().addAll(nameLabel, typeLabel);

        // Sport and structure
        Label sportLabel = new Label(field.getSport().getDisplayName());
        sportLabel.getStyleClass().add(CSS_FIELD_DETAIL);

        if (field.getStructureName() != null) {
            Label structureLabel = new Label("📍 " + field.getStructureName());
            structureLabel.getStyleClass().add(CSS_FIELD_DETAIL);
            card.getChildren().add(structureLabel);
        }

        // Location
        Label locationLabel = new Label(field.getAddress() + ", " + field.getCity());
        locationLabel.getStyleClass().add(CSS_FIELD_DETAIL);

        // Price
        Label priceLabel = new Label(String.format("€%.2f/hour", field.getPricePerHour()));
        priceLabel.getStyleClass().add("field-price");

        // Auto-approve status
        if (field.isAutoApprove()) {
            Label autoApproveLabel = new Label("✓ Auto-approve enabled");
            autoApproveLabel.getStyleClass().add("match-info");
            autoApproveLabel.setStyle("-fx-text-fill: #10b981;");
            card.getChildren().add(autoApproveLabel);
        }

        // Action buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("secondary-button");
        editButton.setOnAction(e -> handleEdit(field));

        Button availabilityButton = new Button("Availability");
        availabilityButton.setOnAction(e -> handleAvailability(field));

        Button bookingsButton = new Button("View Bookings");
        bookingsButton.setOnAction(e -> handleViewBookings(field));

        buttons.getChildren().addAll(editButton, availabilityButton, bookingsButton);

        card.getChildren().addAll(nameRow, sportLabel, locationLabel, priceLabel, buttons);
        return card;
    }

    @SuppressWarnings("unused") // Parameter will be used when edit functionality is implemented
    private void handleEdit(FieldBean field) {
        displayError("Edit functionality coming soon!");
    }

    @SuppressWarnings("unused") // Parameter will be used when availability functionality is implemented
    private void handleAvailability(FieldBean field) {
        displayError("Availability management coming in Phase 2!");
    }

    @SuppressWarnings("unused") // Parameter will be used when view bookings functionality is implemented
    private void handleViewBookings(FieldBean field) {
        displayError("View bookings functionality coming soon!");
    }

    @Override
    public void displayError(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("success");
        messageLabel.getStyleClass().add("error");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);

        // Auto-hide after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    messageLabel.setVisible(false);
                    messageLabel.setManaged(false);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public void close() {
        if (stage != null) {
            stage.close();
        }
    }
}
