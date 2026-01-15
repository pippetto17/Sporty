package view.matchdetailview;

import controller.MatchController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import model.bean.MatchBean;
import model.service.MapService;
import model.dao.FieldDAO;
import model.dao.DAOFactory;
import model.domain.Field;

/**
 * JavaFX implementation of Match Detail View.
 * Displays detailed match information with appropriate actions.
 */
public class GraphicMatchDetailView implements MatchDetailView {
    private final MatchController matchController;
    private Stage stage;
    private VBox infoContainer;
    private Button joinButton;
    private HBox organizerActionsBox;
    private int currentMatchId;
    private FieldDAO fieldDAO;

    public GraphicMatchDetailView(MatchController matchController) {
        this.matchController = matchController;
        matchController.setView(this);
        try {
            this.fieldDAO = DAOFactory.getFieldDAO(DAOFactory.PersistenceType.MEMORY);
        } catch (Exception e) {
            System.err.println("Error initializing FieldDAO: " + e.getMessage());
        }
    }

    @Override
    public void display() {
        // This method is required by View interface but match detail views
        // need the matchId parameter. This will be called after display(int matchId).
        Platform.runLater(() -> {
            if (stage != null) {
                stage.show();
            }
        });
    }

    public void display(int matchId) {
        this.currentMatchId = matchId;
        Platform.runLater(() -> {
            createUI();
            matchController.showMatchDetail(matchId);
        });
    }

    private void createUI() {
        stage = new Stage();
        stage.setTitle("Match Details");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        // Header with Back button and Title
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e -> handleBack());

        Label titleLabel = new Label("Match Details");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e7e34;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, javafx.scene.layout.Priority.ALWAYS);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);

        headerBox.getChildren().addAll(backButton, spacer1, titleLabel, spacer2);

        // Info container (will be populated by displayMatchDetails)
        infoContainer = new VBox(15);
        infoContainer.setPadding(new Insets(10));
        infoContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        ScrollPane scrollPane = new ScrollPane(infoContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent;");

        // Action buttons
        joinButton = new Button("Join Match");
        joinButton.getStyleClass().add("primary-button");
        joinButton.setOnAction(e -> handleJoin());
        joinButton.setVisible(false);
        joinButton.setManaged(false);

        Button inviteButton = new Button("Invite Players");
        inviteButton.getStyleClass().add("secondary-button");
        inviteButton.setOnAction(e -> matchController.invitePlayers(0)); // ID will be set later

        Button cancelButton = new Button("Cancel Match");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> handleCancel());

        organizerActionsBox = new HBox(10, inviteButton, cancelButton);
        organizerActionsBox.setAlignment(Pos.CENTER);
        organizerActionsBox.setVisible(false);
        organizerActionsBox.setManaged(false);

        VBox actionsBox = new VBox(10, joinButton, organizerActionsBox);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPadding(new Insets(20, 0, 0, 0));

        // Layout
        root.setTop(headerBox);
        root.setCenter(scrollPane);
        root.setBottom(actionsBox);

        Scene scene = new Scene(root, 600, 700);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    private void handleBack() {
        close();
    }

    @Override
    public void displayMatchDetails(MatchBean match) {
        Platform.runLater(() -> {
            infoContainer.getChildren().clear();

            // Header section with sport and status
            VBox header = new VBox(8);
            header.setPadding(new Insets(20, 20, 15, 20));
            header.setStyle("-fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0;");

            Label sportLabel = new Label(match.getSport().getDisplayName());
            sportLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e7e34;");

            Label statusBadge = new Label(match.getStatus().name());
            statusBadge.getStyleClass().add("status-badge");
            statusBadge.setStyle(statusBadge.getStyle() + "-fx-font-size: 11px;");

            header.getChildren().addAll(sportLabel, statusBadge);
            infoContainer.getChildren().add(header);

            // Details section - compact grid layout
            GridPane detailsGrid = new GridPane();
            detailsGrid.setPadding(new Insets(20));
            detailsGrid.setHgap(40);
            detailsGrid.setVgap(18);

            int row = 0;

            // Date
            addDetailRow(detailsGrid, row++, "Date", match.getMatchDate().toString());

            // Time
            addDetailRow(detailsGrid, row++, "Time", match.getMatchTime().toString());

            // City
            addDetailRow(detailsGrid, row++, "Location", match.getCity());

            // Participants
            int currentCount = match.getParticipants() != null ? match.getParticipants().size() : 0;
            addDetailRow(detailsGrid, row++, "Players", currentCount + " / " + match.getRequiredParticipants());

            // Price
            if (match.getPricePerPerson() != null) {
                addDetailRow(detailsGrid, row++, "Price",
                        "€" + String.format("%.2f", match.getPricePerPerson()) + " per person");
            }

            // Field info
            if (match.getFieldId() != null && !match.getFieldId().isEmpty()) {
                addDetailRow(detailsGrid, row++, "Field ID", match.getFieldId());
            }

            // Organizer
            addDetailRow(detailsGrid, row++, "Organizer", match.getOrganizerUsername());

            infoContainer.getChildren().add(detailsGrid);

            // Add map if field is available
            if (match.getFieldId() != null && !match.getFieldId().isEmpty()) {
                addFieldMap(match.getFieldId());
            }
        });
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d; -fx-font-weight: 600;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3436; -fx-font-weight: normal;");
        valueNode.setWrapText(true);
        valueNode.setMaxWidth(300);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void addFieldMap(String fieldId) {
        try {
            Field field = fieldDAO.findById(fieldId);
            if (field != null) {
                VBox mapSection = new VBox(12);
                mapSection.setPadding(new Insets(20, 20, 20, 20));
                mapSection.setStyle("-fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");

                Label mapTitle = new Label("Field Location");
                mapTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2d3436;");

                WebView mapView = new WebView();
                mapView.setPrefHeight(280);
                mapView.setMaxHeight(280);
                mapView.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6;");

                // Generate map for single field
                String mapHtml = MapService.generateSingleFieldMapHtml(field);
                mapView.getEngine().loadContent(mapHtml);

                mapSection.getChildren().addAll(mapTitle, mapView);
                infoContainer.getChildren().add(mapSection);
            }
        } catch (Exception e) {
            System.err.println("Error loading map: " + e.getMessage());
        }
    }

    private VBox createInfoLabel(String title, String value) {
        VBox box = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2d3436;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    @Override
    public void showJoinButton(boolean show) {
        Platform.runLater(() -> {
            joinButton.setVisible(show);
            joinButton.setManaged(show);
        });
    }

    @Override
    public void showOrganizerActions() {
        Platform.runLater(() -> {
            organizerActionsBox.setVisible(true);
            organizerActionsBox.setManaged(true);
        });
    }

    private void handleJoin() {
        matchController.joinMatch(currentMatchId);
    }

    private void handleCancel() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Match");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to cancel this match? All participants will be notified.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                matchController.cancelMatch(currentMatchId);
                close();
            }
        });
    }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", message));
    }

    @Override
    public void displaySuccess(String message) {
        Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Success", message));
    }

    @Override
    public void showInfo(String message) {
        Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Information", message));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void close() {
        Platform.runLater(() -> {
            if (stage != null) {
                stage.close();
            }
        });
    }
}
