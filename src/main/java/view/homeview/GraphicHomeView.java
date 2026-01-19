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
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class GraphicHomeView implements HomeView {
    private final HomeController homeController;
    private ApplicationController applicationController;
    private Stage stage;

    private static final String CSS_ACTIVE = "active";
    // Use centralized constants
    private static final String CSS_SEARCH_FIELD_CONTAINER = model.utils.Constants.CSS_SEARCH_FIELD_CONTAINER;
    private static final String CSS_TEXT_CAPTION = model.utils.Constants.CSS_TEXT_CAPTION;
    private static final String CSS_TEXT_MUTED = model.utils.Constants.CSS_TEXT_MUTED;

    // FXML fields
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private javafx.scene.image.ImageView userImageView;
    @FXML
    private HBox roleSwitchContainer; // Container per il toggle
    @FXML
    private VBox filterContainer;
    @FXML
    private VBox organizerActionsBox;
    @FXML
    private Label matchesTitle;
    @FXML
    private GridPane matchesContainer;
    @FXML
    private ScrollPane mainScrollPane;

    // Filtri
    private ComboBox<String> cityFilter;
    private ComboBox<model.domain.Sport> sportFilter;
    private DatePicker dateFilter;
    private boolean isUpdatingCityComboBox = false;

    private static final List<String> ALL_CITIES = Arrays.asList(
            "Roma", "Milano", "Napoli", "Torino", "Palermo",
            "Genova", "Bologna", "Firenze", "Bari", "Catania");

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
                stage.setTitle("Sporty");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
                loader.setController(this);
                Parent root = loader.load();

                Scene scene = new Scene(root, 1000, 750);
                scene.getStylesheets()
                        .add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
                // controls-dark.css removed

                stage.setScene(scene);
                stage.setResizable(true);

                initialize();
                stage.show();
            } catch (IOException e) {
                displayError(model.utils.Constants.ERROR_LOAD_HOME_VIEW_IT + e.getMessage());
            }
        });
    }

    private void initialize() {
        homeController.setHomeView(this);
        displayWelcome();

        // Toggle Switch (se organizzatore)
        if (homeController.getCurrentUser().isOrganizer()) {
            addRoleSwitchToggle();
        }

        buildSearchCapsule();
        updateViewMode();
        displayMatches(homeController.getMatches());
    }

    // --- TOGGLE SWITCH ---
    private void addRoleSwitchToggle() {
        roleSwitchContainer.getChildren().clear();

        HBox toggleBox = new HBox(0);
        toggleBox.getStyleClass().add(model.utils.Constants.CSS_TOGGLE_CONTAINER);
        toggleBox.setAlignment(Pos.CENTER); // Center content vertically/horizontally
        toggleBox.setMaxHeight(Region.USE_PREF_SIZE); // Prevent stretching to header height

        Button btnPlayer = new Button(model.utils.Constants.BTN_PLAYER);
        btnPlayer.getStyleClass().add("toggle-button");
        btnPlayer.setPrefWidth(120);

        Button btnOrg = new Button(model.utils.Constants.BTN_ORGANIZER);
        btnOrg.getStyleClass().add("toggle-button");
        btnOrg.setPrefWidth(120);

        // Stato Iniziale
        if (homeController.isViewingAsPlayer()) {
            btnPlayer.getStyleClass().add(CSS_ACTIVE);
        } else {
            btnOrg.getStyleClass().add(CSS_ACTIVE);
        }

        // Actions
        btnPlayer.setOnAction(e -> {
            if (!homeController.isViewingAsPlayer()) {
                homeController.switchRole();
                btnPlayer.getStyleClass().add(CSS_ACTIVE);
                btnOrg.getStyleClass().remove(CSS_ACTIVE);
                updateViewMode();
                displayMatches(homeController.getMatches());
            }
        });

        btnOrg.setOnAction(e -> {
            if (homeController.isViewingAsPlayer()) {
                homeController.switchRole();
                btnOrg.getStyleClass().add(CSS_ACTIVE);
                btnPlayer.getStyleClass().remove(CSS_ACTIVE);
                updateViewMode();
                displayMatches(homeController.getMatches());
            }
        });

        toggleBox.getChildren().addAll(btnPlayer, btnOrg);
        roleSwitchContainer.getChildren().add(toggleBox);
    }

    // --- SEARCH BAR (Capsula) ---
    private void buildSearchCapsule() {
        filterContainer.getChildren().clear();

        HBox capsule = new HBox(0);
        capsule.getStyleClass().add(model.utils.Constants.CSS_SEARCH_CAPSULE); // Kept custom for specific shape
        capsule.setAlignment(Pos.CENTER_LEFT);

        // 1. CITY
        VBox cityBox = new VBox(0);
        cityBox.getStyleClass().add(model.utils.Constants.CSS_SEARCH_FIELD_CONTAINER);
        cityBox.setAlignment(Pos.CENTER_LEFT);
        Label cityLbl = new Label("Where");
        cityLbl.getStyleClass().addAll(CSS_TEXT_CAPTION, CSS_TEXT_MUTED);

        cityFilter = new ComboBox<>();
        cityFilter.setEditable(true);
        cityFilter.setPromptText("Search destination");
        cityFilter.getStyleClass().add(model.utils.Constants.CSS_INTEGRATED_COMBO);
        cityFilter.setMaxWidth(Double.MAX_VALUE);
        cityFilter.setPrefWidth(250);
        cityFilter.getItems().addAll(ALL_CITIES);

        cityFilter.getEditor().textProperty().addListener((obs, old, newVal) -> {
            if (!isUpdatingCityComboBox)
                updateCityAutocomplete(newVal);
        });

        cityBox.getChildren().addAll(cityLbl, cityFilter);
        HBox.setHgrow(cityBox, Priority.ALWAYS);

        // Separator
        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        // Standard Separator

        // 2. SPORT
        VBox sportBox = new VBox(0);
        sportBox.getStyleClass().add(CSS_SEARCH_FIELD_CONTAINER);
        sportBox.setAlignment(Pos.CENTER_LEFT);
        Label sportLbl = new Label("Sport");
        sportLbl.getStyleClass().addAll(CSS_TEXT_CAPTION, CSS_TEXT_MUTED);

        sportFilter = new ComboBox<>();
        sportFilter.setPromptText("Any");
        sportFilter.getStyleClass().add("integrated-combo");
        sportFilter.setPrefWidth(160);
        sportFilter.getItems().add(null);
        sportFilter.getItems().addAll(model.domain.Sport.values());

        sportBox.getChildren().addAll(sportLbl, sportFilter);

        // Separator
        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);

        // 3. DATE
        VBox dateBox = new VBox(0);
        dateBox.getStyleClass().add(CSS_SEARCH_FIELD_CONTAINER);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label dateLbl = new Label("When");
        dateLbl.getStyleClass().addAll(CSS_TEXT_CAPTION, CSS_TEXT_MUTED);

        dateFilter = new DatePicker();
        dateFilter.setPromptText("Add dates");
        dateFilter.getStyleClass().add(model.utils.Constants.CSS_INTEGRATED_DATE);
        dateFilter.setPrefWidth(160);

        dateBox.getChildren().addAll(dateLbl, dateFilter);

        // 4. SEARCH BUTTON
        Button searchBtn = new Button(model.utils.Constants.BTN_SEARCH);
        searchBtn.getStyleClass().addAll(model.utils.Constants.CSS_SEARCH_ACTION_BUTTON,
                model.utils.Constants.CSS_SUCCESS);
        searchBtn.setOnAction(e -> applyFilters());

        capsule.getChildren().addAll(cityBox, sep1, sportBox, sep2, dateBox, searchBtn);
        filterContainer.getChildren().add(capsule);
    }

    // --- CARDS ---
    // --- CARDS ---
    // --- CARDS ---
    private VBox createGridMatchCard(model.bean.MatchBean match) {
        VBox card = new VBox(0);
        card.getStyleClass().add("match-card");

        // 1. HEADER
        StackPane imageHeader = new StackPane();
        String sportClass = homeController.getSportStyleClass(match.getSport());
        imageHeader.getStyleClass().addAll("card-image-area", sportClass);

        // Icona specifica (Immagine)
        String imagePath = homeController.getSportImagePath(match.getSport());
        javafx.scene.image.ImageView sportIcon = new javafx.scene.image.ImageView();
        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            sportIcon.setImage(img);
            sportIcon.setFitWidth(80);
            sportIcon.setFitHeight(80);
            sportIcon.setPreserveRatio(true);
            sportIcon.setSmooth(true);
            sportIcon.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.3)));
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, () -> String.format("Sport image not found: %s", imagePath));
        }
        imageHeader.getChildren().add(sportIcon);

        // PRICE BADGE - Show ONLY if viewing as Player
        if (homeController.shouldShowPriceBadge()) {
            Label priceBadge = new Label(
                    match.getPricePerPerson() != null ? String.format("€%.0f", match.getPricePerPerson())
                            : model.utils.Constants.MATCH_DETAIL_FREE);
            priceBadge.getStyleClass().add(model.utils.Constants.CSS_CARD_PRICE_BADGE);
            StackPane.setAlignment(priceBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(priceBadge, new Insets(10));
            imageHeader.getChildren().add(priceBadge);
        }

        // 2. CONTENT
        VBox content = new VBox(5);
        content.getStyleClass().add(model.utils.Constants.CSS_CARD_CONTENT);

        Label locationTitle = new Label(
                match.getCity() + model.utils.Constants.BULLET + match.getSport().getDisplayName());
        locationTitle.getStyleClass().add(model.utils.Constants.CSS_CARD_TITLE);
        locationTitle.setWrapText(true);

        Label dateLabel = new Label(model.utils.Constants.ICON_CALENDAR + match.getMatchDate() + "  "
                + model.utils.Constants.ICON_CLOCK + match.getMatchTime());
        dateLabel.getStyleClass().add(model.utils.Constants.CSS_CARD_SUBTITLE);

        int current = homeController.getCurrentParticipants(match);
        int max = match.getRequiredParticipants();
        ProgressBar capacityBar = new ProgressBar(homeController.getCapacityBarProgress(match));
        capacityBar.getStyleClass().addAll(model.utils.Constants.CSS_CARD_PROGRESS_BAR, sportClass + "-bar");
        capacityBar.setPrefWidth(Double.MAX_VALUE);

        Label playersLabel = new Label(current + "/" + max + " joined");
        playersLabel.getStyleClass().add(model.utils.Constants.CSS_CARD_DETAIL_TEXT);
        HBox playersBox = new HBox(5, new Label(model.utils.Constants.ICON_PLAYERS), playersLabel);
        playersBox.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(locationTitle, dateLabel, new Region(), playersBox, capacityBar);

        // Add join button if appropriate
        if (homeController.shouldShowJoinButton(match)) {
            Button joinButton = new Button(model.utils.Constants.BTN_JOIN_MATCH);
            joinButton.getStyleClass().addAll(model.utils.Constants.CSS_SUCCESS, model.utils.Constants.CSS_SMALL);
            joinButton.setOnAction(e -> {
                e.consume();
                handleJoinMatch(match.getMatchId());
            });
            content.getChildren().add(joinButton);
        }

        VBox.setVgrow(content, Priority.ALWAYS);

        card.getChildren().addAll(imageHeader, content);
        card.setOnMouseClicked(e -> homeController.viewMatchDetail(match.getMatchId()));

        return card;
    }

    private void handleJoinMatch(int matchId) {
        try {
            homeController.joinMatch(matchId);
        } catch (exception.ValidationException e) {
            displayError(e.getMessage());
        }
    }

    private void updateCityAutocomplete(String input) {
        isUpdatingCityComboBox = true;
        ObservableList<String> filtered = FXCollections.observableArrayList();
        if (input == null || input.isEmpty())
            filtered.addAll(ALL_CITIES);
        else
            for (String c : ALL_CITIES)
                if (c.toLowerCase().startsWith(input.toLowerCase()))
                    filtered.add(c);
        cityFilter.setItems(filtered);
        if (!filtered.isEmpty())
            cityFilter.show();
        isUpdatingCityComboBox = false;
    }

    private void applyFilters() {
        displayMatches(homeController.filterMatches(sportFilter.getValue(), cityFilter.getEditor().getText(),
                dateFilter.getValue()));
    }

    @Override
    public void displayWelcome() {
        model.domain.User u = homeController.getCurrentUser();
        welcomeLabel.setText(model.utils.Constants.LABEL_HELLO_PREFIX + u.getName() + " " + u.getSurname());
        roleLabel.setText(homeController.getUserRole().getDisplayName());

        // Load User Image
        try {
            // Allow overriding the image paths via system properties so the URI is
            // configurable
            String playerPath = System.getProperty("sporty.image.player", model.utils.Constants.IMAGE_PLAYER_PATH);
            String organizerPath = System.getProperty("sporty.image.organizer",
                    model.utils.Constants.IMAGE_ORGANIZER_PATH);
            String imagePath = homeController.getUserRole() == model.domain.Role.ORGANIZER ? organizerPath : playerPath;

            javafx.scene.image.Image img = new javafx.scene.image.Image(
                    java.util.Objects.requireNonNull(getClass().getResourceAsStream(imagePath)),
                    120, 120, true, true); // Load at higher res for sharpness
            userImageView.setImage(img);

            // Circular Clip
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(30, 30, 30);
            userImageView.setClip(clip);
        } catch (Exception e) {
            // Fallback
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.WARNING,
                    String.format("User image not found: %s", e.getMessage()));
        }
    }

    @Override
    public void displayMatches(List<model.bean.MatchBean> matches) {
        matchesContainer.getChildren().clear();
        if (matches == null || matches.isEmpty()) {
            Label empty = new Label(model.utils.Constants.LABEL_NO_MATCHES_FOUND);
            empty.getStyleClass().addAll(model.utils.Constants.CSS_TEXT_MUTED, model.utils.Constants.CSS_TITLE_4);
            empty.setPadding(new Insets(20));
            matchesContainer.add(empty, 0, 0);
            return;
        }

        // Add matches to grid with 3 columns per row
        int column = 0;
        int row = 0;
        for (model.bean.MatchBean match : matches) {
            VBox card = createGridMatchCard(match);
            matchesContainer.add(card, column, row);
            column++;
            if (column >= 3) {
                column = 0;
                row++;
            }
        }
    }

    private void updateViewMode() {
        boolean showOrganizerActions = homeController.shouldShowOrganizerActions();
        organizerActionsBox.setVisible(showOrganizerActions);
        organizerActionsBox.setManaged(showOrganizerActions);
        matchesTitle.setText(homeController.getMatchesTitle());
    }

    @Override
    public void refreshMatches() {
        displayMatches(homeController.getMatches());
    }

    @Override
    public void showMatchDetails(int matchId) {
        // Find match from current matches list
        model.bean.MatchBean match = homeController.getMatches().stream()
                .filter(m -> m.getMatchId() == matchId)
                .findFirst()
                .orElse(null);

        if (match == null) {
            displayError("Match not found!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Match Details");
        alert.setHeaderText(match.getSport().getDisplayName() + " - " + match.getCity());

        // Custom Content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Common Info
        content.getChildren().add(new Label("Date: " + match.getMatchDate()));
        content.getChildren().add(new Label("Time: " + match.getMatchTime()));
        content.getChildren().add(new Label("Field: " + (match.getFieldId() != null ? match.getFieldId() : "TBD")));

        // Role Specific Info
        double pricePerHead = (match.getPricePerPerson() != null) ? match.getPricePerPerson() : 0.0;
        int totalPlayers = match.getRequiredParticipants();

        if (homeController.isViewingAsPlayer()) {
            // PLAYER VIEW
            double totalPrice = pricePerHead * totalPlayers;
            int joined = (match.getParticipants() != null) ? match.getParticipants().size() : 0;

            content.getChildren().add(new Label(String.format("Price to Pay: €%.2f", pricePerHead)));
            content.getChildren().add(new Label(String.format("Total Match Cost: €%.2f", totalPrice)));
            content.getChildren().add(new Label(String.format("Players Joined: %d/%d", joined, totalPlayers)));

            ButtonType joinBtn = new ButtonType("Join Match", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(joinBtn, ButtonType.CLOSE);

            // Set action later
            alert.setResultConverter(btn -> {
                if (btn == joinBtn) {
                    handleJoinMatch(matchId);
                }
                return null;
            });

        } else {
            // ORGANIZER VIEW
            double totalPrice = pricePerHead * totalPlayers;

            content.getChildren().add(new Label(String.format("Total Match Cost: €%.2f", totalPrice)));
            content.getChildren().add(new Label(String.format("My Cost: €%.2f", pricePerHead))); // Assuming 1 share

            ButtonType inviteBtn = new ButtonType("Invite Player", ButtonBar.ButtonData.OTHER);
            ButtonType cancelBtn = new ButtonType("Cancel Match", ButtonBar.ButtonData.FINISH);

            alert.getButtonTypes().setAll(inviteBtn, cancelBtn, ButtonType.CLOSE);

            // Hacky way to handle custom buttons action without closing immediately or
            // handling result
            alert.setResultConverter(btn -> {
                if (btn == cancelBtn) {
                    // Call logic to cancel
                    displayError("Cancel feature coming soon!");
                } else if (btn == inviteBtn) {
                    displayError("Invite feature coming soon!");
                }
                return null;
            });
        }

        alert.getDialogPane().setContent(content);
        view.ViewUtils.applyStylesheets(alert.getDialogPane());
        alert.showAndWait();
    }

    @Override
    public void displayMenu() {
        // Intentionally empty``
    }

    @FXML
    private void handleOrganizeMatch() {
        homeController.organizeMatch();
    }

    @FXML
    private void handleBookField() {
        homeController.bookFieldStandalone();
    }

    @FXML
    private void handleRefresh() {
        displayMatches(homeController.getMatches());
    }

    @FXML
    private void handleLogout() {
        stage.close();
        applicationController.logout();
    }

    @Override
    public void displayError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        view.ViewUtils.applyStylesheets(alert.getDialogPane());
        alert.showAndWait();
    }

    @Override
    public void displaySuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        view.ViewUtils.applyStylesheets(alert.getDialogPane());
        alert.showAndWait();
    }

    @Override
    public void close() {
        if (stage != null)
            stage.close();
    }
}
