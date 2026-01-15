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
    private VBox mainContentBox;
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

        // Add role switch toggle if user is organizer
        if (homeController.isOrganizer()) {
            addRoleSwitchToggle();
        }

        // Add filter UI
        addFilterUI();

        // Show/hide organizer actions based on role
        updateViewMode();

        // Load matches
        displayMatches(homeController.getMatches());

        updateStatus("Ready");
    }

    private void updateViewMode() {
        if (homeController.isViewingAsPlayer()) {
            organizerActionsBox.setVisible(false);
            organizerActionsBox.setManaged(false);
            matchesTitle.setText("Available Matches");
        } else {
            organizerActionsBox.setVisible(true);
            organizerActionsBox.setManaged(true);
            matchesTitle.setText("My Organized Matches");
        }
    }

    private void addRoleSwitchToggle() {
        HBox toggleContainer = new HBox(10);
        toggleContainer.setAlignment(Pos.CENTER_LEFT);
        toggleContainer.setPadding(new Insets(10, 0, 10, 0));

        Label toggleLabel = new Label("View as:");
        toggleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        javafx.scene.control.ToggleButton playerToggle = new javafx.scene.control.ToggleButton("Player");
        javafx.scene.control.ToggleButton organizerToggle = new javafx.scene.control.ToggleButton("Organizer");

        ToggleGroup group = new ToggleGroup();
        playerToggle.setToggleGroup(group);
        organizerToggle.setToggleGroup(group);

        playerToggle.getStyleClass().add("secondary-button");
        organizerToggle.getStyleClass().add("secondary-button");

        // Set initial selection
        if (homeController.isViewingAsPlayer()) {
            playerToggle.setSelected(true);
        } else {
            organizerToggle.setSelected(true);
        }

        // Handle toggle
        playerToggle.setOnAction(e -> {
            if (playerToggle.isSelected() && !homeController.isViewingAsPlayer()) {
                homeController.switchRole();
                updateViewMode();
                displayMatches(homeController.getMatches());
            }
        });

        organizerToggle.setOnAction(e -> {
            if (organizerToggle.isSelected() && homeController.isViewingAsPlayer()) {
                homeController.switchRole();
                updateViewMode();
                displayMatches(homeController.getMatches());
            }
        });

        toggleContainer.getChildren().addAll(toggleLabel, playerToggle, organizerToggle);

        // Insert before matches section
        mainContentBox.getChildren().add(0, toggleContainer);
    }

    private javafx.scene.control.ComboBox<model.domain.Sport> sportFilter;
    private javafx.scene.control.TextField cityFilter;
    private javafx.scene.control.DatePicker dateFilter;

    private void addFilterUI() {
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(10, 0, 10, 0));

        Label filterLabel = new Label("Filters:");
        filterLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        // Sport filter
        sportFilter = new javafx.scene.control.ComboBox<>();
        sportFilter.setPromptText("Sport");
        sportFilter.setPrefWidth(120);
        sportFilter.getItems().add(null); // "All" option
        sportFilter.getItems().addAll(model.domain.Sport.values());
        sportFilter.setValue(null);

        // City filter
        cityFilter = new javafx.scene.control.TextField();
        cityFilter.setPromptText("City");
        cityFilter.setPrefWidth(120);

        // Date filter
        dateFilter = new javafx.scene.control.DatePicker();
        dateFilter.setPromptText("Date");
        dateFilter.setPrefWidth(140);

        Button applyFilters = new Button("Apply");
        applyFilters.getStyleClass().add("primary-button");
        applyFilters.setOnAction(e -> applyFilters());

        Button clearFilters = new Button("Clear");
        clearFilters.getStyleClass().add("secondary-button");
        clearFilters.setOnAction(e -> clearFilters());

        filterBox.getChildren().addAll(filterLabel, sportFilter, cityFilter, dateFilter, applyFilters, clearFilters);

        // Insert after role toggle (if exists) or at beginning
        int insertIndex = homeController.isOrganizer() ? 1 : 0;
        mainContentBox.getChildren().add(insertIndex, filterBox);
    }

    private void applyFilters() {
        model.domain.Sport sport = sportFilter.getValue();
        String city = cityFilter.getText();
        java.time.LocalDate date = dateFilter.getValue();

        java.util.List<model.bean.MatchBean> filtered = homeController.filterMatches(sport, city, date);
        displayMatches(filtered);
        updateStatus("Filters applied - " + filtered.size() + " matches found");
    }

    private void clearFilters() {
        sportFilter.setValue(null);
        cityFilter.clear();
        dateFilter.setValue(null);
        displayMatches(homeController.getMatches());
        updateStatus("Filters cleared");
    }

    @Override
    public void displayWelcome() {
        String username = homeController.getCurrentUser().getUsername();
        String role = homeController.getUserRole().getDisplayName();

        welcomeLabel.setText("Welcome, " + username + "!");
        roleLabel.setText(role);
    }

    @Override
    public void displayMatches(java.util.List<model.bean.MatchBean> matches) {
        matchesContainer.getChildren().clear();

        if (matches == null || matches.isEmpty()) {
            Label emptyLabel = new Label("No matches available");
            emptyLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");
            matchesContainer.getChildren().add(emptyLabel);
            return;
        }

        // Create match cards
        for (model.bean.MatchBean match : matches) {
            VBox matchCard = createMatchCard(match);
            matchesContainer.getChildren().add(matchCard);
        }

        updateStatus(matches.size() + " matches loaded");
    }

    private VBox createMatchCard(model.bean.MatchBean match) {
        VBox card = new VBox(8);
        card.getStyleClass().add("match-card");
        card.setPadding(new Insets(15));

        // Title with sport
        Label titleLabel = new Label("⚽ " + match.getSport().getDisplayName());
        titleLabel.getStyleClass().add("match-title");

        // Date and time
        Label dateLabel = new Label("📅 " + match.getMatchDate() + " at " + match.getMatchTime());
        dateLabel.getStyleClass().add(MATCH_INFO_STYLE);

        // Players info
        int currentPlayers = match.getParticipants() != null ? match.getParticipants().size() : 0;
        Label playersLabel = new Label("👥 Players: " + currentPlayers + "/" + match.getRequiredParticipants());
        playersLabel.getStyleClass().add(MATCH_INFO_STYLE);

        // Location
        Label locationLabel = new Label("📍 " + match.getCity());
        locationLabel.getStyleClass().add(MATCH_INFO_STYLE);

        // Status and price
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Label statusBadge = new Label(match.getStatus().name());
        statusBadge.getStyleClass().add("status-badge");

        if (match.getPricePerPerson() != null) {
            Label priceLabel = new Label(String.format("€%.2f/person", match.getPricePerPerson()));
            priceLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 12px;");
            statusBox.getChildren().addAll(statusBadge, priceLabel);
        } else {
            statusBox.getChildren().add(statusBadge);
        }

        // Add all elements
        card.getChildren().addAll(titleLabel, dateLabel, playersLabel, locationLabel, statusBox);

        // Add click handler - navigate to detail
        card.setOnMouseClicked(e -> {
            if (match.getMatchId() != null) {
                homeController.viewMatchDetail(match.getMatchId());
            }
        });

        return card;
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
