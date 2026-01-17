package view.homeview;

import controller.ApplicationController;
import controller.HomeController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import model.utils.Constants;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
                scene.getStylesheets().add(getClass().getResource("/css/controls-dark.css").toExternalForm());

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

    @Override
    public void refreshMatches() {
        displayMatches(homeController.getMatches());
    }

    @Override
    public void showMatchDetails(int matchId) {
        try {
            // Get match details from controller
            model.bean.MatchBean match = homeController.getMatches().stream()
                    .filter(m -> m.getMatchId() == matchId)
                    .findFirst()
                    .orElse(null);

            if (match == null) {
                updateStatus("Match not found");
                return;
            }

            // Create dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Match Details");
            dialog.setHeaderText(match.getSport().getDisplayName());

            // Create content
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));
            content.getChildren().addAll(
                    new Label("📅 Date: " + match.getMatchDate() + " at " + match.getMatchTime()),
                    new Label("📍 City: " + match.getCity()),
                    new Label("👤 Organizer: " + match.getOrganizerUsername()),
                    new Label("👥 Players: " + (match.getParticipants() != null ? match.getParticipants().size() : 0)
                            + "/" + match.getRequiredParticipants()),
                    new Label("💰 Price: €"
                            + (match.getPricePerPerson() != null ? String.format("%.2f", match.getPricePerPerson())
                                    : "Free")),
                    new Label("📊 Status: " + match.getStatus().name()));

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

            dialog.showAndWait();

        } catch (Exception e) {
            updateStatus("Error showing match details: " + e.getMessage());
        }
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
        // Create toggle switch container
        HBox switchContainer = new HBox(0);
        switchContainer.setAlignment(Pos.CENTER_LEFT);
        switchContainer.setPadding(new Insets(0, 0, 15, 0));
        switchContainer.setMaxWidth(Region.USE_PREF_SIZE); // Fit to content
        switchContainer.getStyleClass().add("role-switch-container");

        // Create button elements
        Button playerButton = new Button("Player");
        playerButton.getStyleClass().add("role-switch-button");
        playerButton.setPrefWidth(100);

        Button organizerButton = new Button("Organizer");
        organizerButton.getStyleClass().add("role-switch-button");
        organizerButton.setPrefWidth(100);

        // Set initial active state
        if (homeController.isViewingAsPlayer()) {
            playerButton.getStyleClass().add("active");
        } else {
            organizerButton.getStyleClass().add("active");
        }

        // Handle player button click
        playerButton.setOnAction(e -> {
            if (!homeController.isViewingAsPlayer()) {
                homeController.switchRole();
                playerButton.getStyleClass().add("active");
                organizerButton.getStyleClass().remove("active");
                updateViewMode();
                displayMatches(homeController.getMatches());
            }
        });

        // Handle organizer button click
        organizerButton.setOnAction(e -> {
            if (homeController.isViewingAsPlayer()) {
                homeController.switchRole();
                organizerButton.getStyleClass().add("active");
                playerButton.getStyleClass().remove("active");
                updateViewMode();
                displayMatches(homeController.getMatches());
            }
        });

        switchContainer.getChildren().addAll(playerButton, organizerButton);

        // Insert at the beginning of content
        mainContentBox.getChildren().add(0, switchContainer);
    }

    private javafx.scene.control.ComboBox<model.domain.Sport> sportFilter;
    private javafx.scene.control.ComboBox<String> cityFilter;
    private boolean isUpdatingCityComboBox = false;

    // List of Italian cities for autocomplete
    private static final List<String> ALL_CITIES = Arrays.asList(
            "Rome", "Milan", "Naples", "Turin", "Palermo",
            "Genoa", "Bologna", "Florence", "Bari", "Catania",
            "Venice", "Verona", "Messina", "Padua", "Trieste",
            "Brescia", "Parma", "Prato", "Modena", "Reggio Calabria");
    private javafx.scene.control.DatePicker dateFilter;

    private void addFilterUI() {
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(10, 0, 10, 0));

        Label filterLabel = new Label("Filters:");
        filterLabel.getStyleClass().add("field-label");

        // Sport filter
        sportFilter = new javafx.scene.control.ComboBox<>();
        sportFilter.setPromptText("Sport");
        sportFilter.setPrefWidth(150);
        sportFilter.setPrefHeight(40);
        sportFilter.getStyleClass().add("custom-combo-box");
        sportFilter.getItems().add(null); // "All" option
        sportFilter.getItems().addAll(model.domain.Sport.values());
        sportFilter.setValue(null);

        // City filter with autocomplete
        cityFilter = new javafx.scene.control.ComboBox<>();
        cityFilter.setEditable(true);
        cityFilter.setPromptText("City");
        cityFilter.setPrefWidth(150);
        cityFilter.setPrefHeight(40);
        cityFilter.getStyleClass().add("custom-combo-box");
        cityFilter.getItems().addAll(ALL_CITIES);

        // Add autocomplete listener
        cityFilter.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!isUpdatingCityComboBox) {
                updateCityComboBox(newValue);
            }
        });

        // Date filter
        dateFilter = new javafx.scene.control.DatePicker();
        dateFilter.setPromptText("Date");
        dateFilter.setPrefWidth(150);
        dateFilter.setPrefHeight(40);
        dateFilter.getStyleClass().add("custom-date-picker");

        Button applyFilters = new Button("Apply");
        applyFilters.setPrefHeight(40);
        applyFilters.getStyleClass().add("primary-button");
        applyFilters.setOnAction(e -> applyFilters());

        Button clearFilters = new Button("Clear");
        clearFilters.setPrefHeight(40);
        clearFilters.getStyleClass().add(Constants.CSS_SECONDARY_BUTTON);
        clearFilters.setOnAction(e -> clearFilters());

        filterBox.getChildren().addAll(filterLabel, sportFilter, cityFilter, dateFilter, applyFilters, clearFilters);

        // Insert after role toggle (if exists) or at beginning
        int insertIndex = homeController.isOrganizer() ? 1 : 0;
        mainContentBox.getChildren().add(insertIndex, filterBox);
    }

    private void applyFilters() {
        model.domain.Sport sport = sportFilter.getValue();
        String city = cityFilter.getEditor().getText();
        java.time.LocalDate date = dateFilter.getValue();

        java.util.List<model.bean.MatchBean> filtered = homeController.filterMatches(sport, city, date);
        displayMatches(filtered);
        updateStatus("Filters applied - " + filtered.size() + " matches found");
    }

    private void clearFilters() {
        sportFilter.setValue(null);
        cityFilter.getEditor().clear();
        cityFilter.setValue(null);
        dateFilter.setValue(null);
        displayMatches(homeController.getMatches());
        updateStatus("Filters cleared");
    }

    /**
     * Updates the city ComboBox items based on user input for autocomplete
     * functionality.
     * 
     * @param input the current text input from the user
     */
    private void updateCityComboBox(String input) {
        isUpdatingCityComboBox = true;

        ObservableList<String> filteredCities = FXCollections.observableArrayList();

        if (input == null || input.isEmpty()) {
            // Show all cities if input is empty
            filteredCities.addAll(ALL_CITIES);
        } else {
            // Filter cities that start with the input (case-insensitive)
            filteredCities.addAll(
                    ALL_CITIES.stream()
                            .filter(city -> city.toLowerCase().startsWith(input.toLowerCase()))
                            .collect(Collectors.toList()));
        }

        cityFilter.setItems(filteredCities);
        if (!filteredCities.isEmpty()) {
            cityFilter.show();
        }

        isUpdatingCityComboBox = false;
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
