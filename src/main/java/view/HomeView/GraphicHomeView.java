package view.homeview;

import controller.ApplicationController;
import controller.HomeController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class GraphicHomeView implements HomeView {
    private final HomeController homeController;
    private ApplicationController applicationController;
    private Stage stage;

    // FXML fields
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private VBox organizerActionsBox;
    @FXML
    private Button organizeMatchButton;
    @FXML
    private Button bookFieldButton;
    @FXML
    private Label matchesTitle;
    @FXML
    private Button refreshButton;
    @FXML
    private VBox matchesContainer;
    @FXML
    private Label statusLabel;

    // CSS class name constants
    private static final String MATCH_INFO_STYLE = "match-info";

    public GraphicHomeView(HomeController homeController) {
        this.homeController = homeController;
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @Override
    public void display() {
        Platform.runLater(() -> {
            try {
                stage = new Stage();
                stage.setTitle("Sporty - Home");

                // Load FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
                loader.setController(this);
                Parent root = loader.load();

                // Load CSS
                Scene scene = new Scene(root, 700, 600);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

                stage.setScene(scene);
                stage.setResizable(true);

                // Initialize content
                initialize();

                stage.show();
            } catch (IOException e) {
                showError("Failed to load home view: " + e.getMessage());
            }
        });
    }

    private void initialize() {
        // Set welcome message
        displayWelcome();

        // Show/hide organizer actions based on role
        if (homeController.isOrganizer()) {
            organizerActionsBox.setVisible(true);
            organizerActionsBox.setManaged(true);
            matchesTitle.setText("My Organized Matches");
        } else {
            organizerActionsBox.setVisible(false);
            organizerActionsBox.setManaged(false);
            matchesTitle.setText("Available Matches");
        }

        // Load matches
        displayMatches(homeController.getMatches());

        updateStatus("Ready");
    }

    @Override
    public void displayWelcome() {
        String username = homeController.getCurrentUser().getUsername();
        String role = homeController.getUserRole().getDisplayName();

        welcomeLabel.setText("Welcome, " + username + "!");
        roleLabel.setText(role);
    }

    @Override
    public void displayMatches(String[] matches) {
        matchesContainer.getChildren().clear();

        if (matches == null || matches.length == 0) {
            Label emptyLabel = new Label("No matches available");
            emptyLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");
            matchesContainer.getChildren().add(emptyLabel);
            return;
        }

        // Create match cards
        for (String match : matches) {
            VBox matchCard = createMatchCard(match);
            matchesContainer.getChildren().add(matchCard);
        }

        updateStatus(matches.length + " matches loaded");
    }

    private VBox createMatchCard(String matchInfo) {
        VBox card = new VBox(8);
        card.getStyleClass().add("match-card");
        card.setPadding(new Insets(15));

        // Parse match info (format: "Match X - Sport - Date")
        String[] parts = matchInfo.split(" - ");
        String title = parts.length > 1 ? parts[1] : "Match";
        String date = parts.length > 2 ? parts[2] : "Date TBD";

        // Title
        Label titleLabel = new Label("⚽ " + title);
        titleLabel.getStyleClass().add("match-title");

        // Date info
        Label dateLabel = new Label("📅 " + date);
        dateLabel.getStyleClass().add(MATCH_INFO_STYLE);

        // Players info
        Label playersLabel = new Label("👥 Players: 6/10");
        playersLabel.getStyleClass().add(MATCH_INFO_STYLE);

        // Location
        Label locationLabel = new Label("📍 City Center Sports Complex");
        locationLabel.getStyleClass().add(MATCH_INFO_STYLE);

        // Status badge
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Label statusBadge = new Label("OPEN");
        statusBadge.getStyleClass().add("status-badge");

        Label priceLabel = new Label("€15/person");
        priceLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 12px;");

        statusBox.getChildren().addAll(statusBadge, priceLabel);

        // Add all elements
        card.getChildren().addAll(titleLabel, dateLabel, playersLabel, locationLabel, statusBox);

        // Add click handler
        card.setOnMouseClicked(e -> handleMatchClick(matchInfo));

        return card;
    }

    private void handleMatchClick(String matchInfo) {
        updateStatus("Selected: " + matchInfo);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Match Details");
        alert.setHeaderText(null);
        alert.setContentText("Detailed match view coming soon!\n\nMatch: " + matchInfo);
        alert.showAndWait();
    }

    @Override
    public void displayMenu() {
        // Menu is already displayed in the UI
    }

    @FXML
    private void handleOrganizeMatch() {
        updateStatus("Opening organize match...");
        homeController.organizeMatch();
    }

    @FXML
    private void handleBookField() {
        updateStatus("Opening book field...");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Book Field");
        alert.setHeaderText(null);
        alert.setContentText("Book field feature coming soon!");
        alert.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        updateStatus("Refreshing...");
        displayMatches(homeController.getMatches());
    }

    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Logout");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to logout?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                updateStatus("Logging out...");
                stage.close();
                applicationController.logout();
            }
        });
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    @Override
    public void close() {
        if (stage != null) {
            Platform.runLater(() -> stage.close());
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
