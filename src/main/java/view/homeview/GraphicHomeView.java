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
    private FlowPane matchesContainer;
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
                showError(model.utils.Constants.ERROR_LOAD_HOME_VIEW_IT + e.getMessage());
            }
        });
    }

    private void initialize() {
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
    private VBox createGridMatchCard(model.bean.MatchBean match) {
        VBox card = new VBox(0);
        card.getStyleClass().add("match-card");

        // 1. HEADER
        StackPane imageHeader = new StackPane();
        String sportClass = getSportStyleClass(match.getSport()); // Ottieni classe dinamica
        imageHeader.getStyleClass().addAll("card-image-area", sportClass);

        // Icona specifica (Immagine)
        String imagePath = getSportImagePath(match.getSport());
        javafx.scene.image.ImageView sportIcon = new javafx.scene.image.ImageView();
        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            sportIcon.setImage(img);
            sportIcon.setFitWidth(80);
            sportIcon.setFitHeight(80);
            sportIcon.setPreserveRatio(true);
            sportIcon.setSmooth(true);
            // Effect similar to previous: dropshadow
            sportIcon.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.3)));
        } catch (Exception e) {
            // Fallback to text if image missing (rare)
            // We can just leave empty or put a placeholder label, but for now log it
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, () -> String.format("Sport image not found: %s", imagePath));
        }

        Label priceBadge = new Label(
                match.getPricePerPerson() != null ? String.format("€%.0f", match.getPricePerPerson())
                        : model.utils.Constants.MATCH_DETAIL_FREE);
        priceBadge.getStyleClass().add(model.utils.Constants.CSS_CARD_PRICE_BADGE);
        StackPane.setAlignment(priceBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(priceBadge, new Insets(10));

        imageHeader.getChildren().addAll(sportIcon, priceBadge);

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

        int current = match.getParticipants() != null ? match.getParticipants().size() : 0;
        int max = match.getRequiredParticipants();
        ProgressBar capacityBar = new ProgressBar((double) current / max);
        capacityBar.getStyleClass().addAll(model.utils.Constants.CSS_CARD_PROGRESS_BAR, sportClass + "-bar"); // Barra
        // progressiva
        // coordinata
        capacityBar.setPrefWidth(Double.MAX_VALUE);

        Label playersLabel = new Label(current + "/" + max + " joined");
        playersLabel.getStyleClass().add(model.utils.Constants.CSS_CARD_DETAIL_TEXT);
        HBox playersBox = new HBox(5, new Label(model.utils.Constants.ICON_PLAYERS), playersLabel);
        playersBox.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(locationTitle, dateLabel, new Region(), playersBox, capacityBar);

        // Aggiungi pulsante Join se l'utente sta visualizzando come player
        if (homeController.isViewingAsPlayer() && !match.isFull()) {
            Button joinButton = new Button(model.utils.Constants.BTN_JOIN_MATCH);
            joinButton.getStyleClass().addAll(model.utils.Constants.CSS_SUCCESS, model.utils.Constants.CSS_SMALL); // AtlantaFX
            // classes
            joinButton.setOnAction(e -> {
                e.consume();
                homeController.joinMatch(match.getMatchId());
            });
            content.getChildren().add(joinButton);
        }

        VBox.setVgrow(content, Priority.ALWAYS);

        card.getChildren().addAll(imageHeader, content);
        card.setOnMouseClicked(e -> homeController.viewMatchDetail(match.getMatchId()));

        return card;
    }

    // Helper per determinare lo stile CSS in base all'enum Sport
    private String getSportStyleClass(model.domain.Sport sport) {
        if (sport == null)
            return "sport-default";
        String name = sport.name().toUpperCase();

        if (name.contains("FOOTBALL"))
            return "sport-soccer";
        if (name.contains("BASKET"))
            return "sport-basket";
        if (name.contains("TENNIS") || name.contains("PADEL"))
            return "sport-tennis";

        return "sport-default";
    }

    // Helper per determinare il percorso immagine in base all'enum Sport
    private String getSportImagePath(model.domain.Sport sport) {
        if (sport == null)
            return model.utils.Constants.IMAGE_MEDAL_PATH;
        String name = sport.name().toUpperCase();

        if (name.contains("FOOTBALL"))
            return model.utils.Constants.IMAGE_FOOTBALL_PATH;
        if (name.contains("BASKET"))
            return model.utils.Constants.IMAGE_BASKETBALL_PATH;
        if (name.contains("TENNIS"))
            return model.utils.Constants.IMAGE_TENNIS_PATH;
        if (name.contains("PADEL"))
            return model.utils.Constants.IMAGE_PADEL_PATH;

        return model.utils.Constants.IMAGE_MEDAL_PATH;
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
            matchesContainer.getChildren().add(empty);
            return;
        }
        for (model.bean.MatchBean match : matches)
            matchesContainer.getChildren().add(createGridMatchCard(match));
    }

    private void updateViewMode() {
        if (homeController.isViewingAsPlayer()) {
            organizerActionsBox.setVisible(false);
            organizerActionsBox.setManaged(false);
            matchesTitle.setText(model.utils.Constants.LABEL_EXPLORE_MATCHES);
        } else {
            organizerActionsBox.setVisible(true);
            organizerActionsBox.setManaged(true);
            matchesTitle.setText(model.utils.Constants.LABEL_YOUR_MATCHES);
        }
    }

    @Override
    public void refreshMatches() {
        displayMatches(homeController.getMatches());
    }

    @Override
    public List<model.bean.MatchBean> getMatches() {
        return homeController.getMatches();
    }

    @Override
    public void showMatchDetails(int matchId) {
        // Implementation delegated to controller or other view
    }

    @Override
    public void displayMenu() {
        // Intentionally empty: menu interactions are handled directly in the graphic UI
        // (toolbar/buttons)
        // This method exists to satisfy the View interface used by other
        // implementations (e.g., CLI).
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

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @Override
    public void close() {
        if (stage != null)
            stage.close();
    }
}
