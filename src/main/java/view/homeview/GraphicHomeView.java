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
import model.utils.Constants;

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

    // FXML fields
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label roleLabel;
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
    private Label statusLabel;
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
                scene.getStylesheets()
                        .add(Objects.requireNonNull(getClass().getResource("/css/controls-dark.css")).toExternalForm());

                stage.setScene(scene);
                stage.setResizable(true);

                initialize();
                stage.show();
            } catch (IOException e) {
                showError("Impossibile caricare la home: " + e.getMessage());
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
        updateStatus("Pronto");
    }

    // --- TOGGLE SWITCH ---
    private void addRoleSwitchToggle() {
        roleSwitchContainer.getChildren().clear();

        HBox toggleBox = new HBox(0);
        toggleBox.getStyleClass().add("toggle-container");

        Button btnPlayer = new Button("Player");
        btnPlayer.getStyleClass().add("toggle-button");
        btnPlayer.setPrefWidth(90);

        Button btnOrg = new Button("Organizer");
        btnOrg.getStyleClass().add("toggle-button");
        btnOrg.setPrefWidth(90);

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
        capsule.getStyleClass().add("search-capsule");
        capsule.setAlignment(Pos.CENTER_LEFT);

        // 1. CITTÀ
        VBox cityBox = new VBox(0);
        cityBox.getStyleClass().add("search-field-container");
        cityBox.setAlignment(Pos.CENTER_LEFT);
        Label cityLbl = new Label("Dove");
        cityLbl.getStyleClass().add("search-label-small");

        cityFilter = new ComboBox<>();
        cityFilter.setEditable(true);
        cityFilter.setPromptText("Cerca destinazione");
        cityFilter.getStyleClass().add("integrated-combo");
        cityFilter.setMaxWidth(Double.MAX_VALUE); // Full width
        cityFilter.setPrefWidth(250); // Larghezza esplicita maggiore
        cityFilter.getItems().addAll(ALL_CITIES);

        cityFilter.getEditor().textProperty().addListener((obs, old, newVal) -> {
            if (!isUpdatingCityComboBox)
                updateCityAutocomplete(newVal);
        });

        cityBox.getChildren().addAll(cityLbl, cityFilter);
        HBox.setHgrow(cityBox, Priority.ALWAYS); // Occupa spazio extra

        // Separatore
        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep1.getStyleClass().add("search-separator");

        // 2. SPORT
        VBox sportBox = new VBox(0);
        sportBox.getStyleClass().add("search-field-container");
        sportBox.setAlignment(Pos.CENTER_LEFT);
        Label sportLbl = new Label("Sport");
        sportLbl.getStyleClass().add("search-label-small");

        sportFilter = new ComboBox<>();
        sportFilter.setPromptText("Qualsiasi");
        sportFilter.getStyleClass().add("integrated-combo");
        sportFilter.setPrefWidth(160);
        sportFilter.getItems().add(null);
        sportFilter.getItems().addAll(model.domain.Sport.values());

        sportBox.getChildren().addAll(sportLbl, sportFilter);

        // Separatore
        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep2.getStyleClass().add("search-separator");

        // 3. DATA
        VBox dateBox = new VBox(0);
        dateBox.getStyleClass().add("search-field-container");
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label dateLbl = new Label("Quando");
        dateLbl.getStyleClass().add("search-label-small");

        dateFilter = new DatePicker();
        dateFilter.setPromptText("Aggiungi date");
        dateFilter.getStyleClass().add("integrated-date");
        dateFilter.setPrefWidth(160);

        dateBox.getChildren().addAll(dateLbl, dateFilter);

        // 4. BOTTONE CERCA
        Button searchBtn = new Button("Cerca");
        searchBtn.getStyleClass().add("search-action-button");
        searchBtn.setOnAction(e -> applyFilters());

        capsule.getChildren().addAll(cityBox, sep1, sportBox, sep2, dateBox, searchBtn);
        filterContainer.getChildren().add(capsule);
    }

    // --- CARDS ---
    private VBox createGridMatchCard(model.bean.MatchBean match) {
        VBox card = new VBox(0);
        card.getStyleClass().add("match-card");

        // 1. IMMAGINE/HEADER (Dinamica in base allo sport)
        StackPane imageHeader = new StackPane();
        String sportClass = getSportStyleClass(match.getSport()); // Ottieni classe dinamica
        imageHeader.getStyleClass().addAll("card-image-area", sportClass);

        // Icona specifica
        Label sportIcon = new Label(getSportIcon(match.getSport()));
        sportIcon.setStyle(
                "-fx-font-size: 52px; -fx-text-fill: rgba(255,255,255,0.9); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 1);");

        Label priceBadge = new Label(
                match.getPricePerPerson() != null ? String.format("€%.0f", match.getPricePerPerson()) : "Free");
        priceBadge.getStyleClass().add("card-price-badge");
        StackPane.setAlignment(priceBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(priceBadge, new Insets(10));

        imageHeader.getChildren().addAll(sportIcon, priceBadge);

        // 2. CONTENUTO
        VBox content = new VBox(5);
        content.getStyleClass().add("card-content");

        Label locationTitle = new Label(match.getCity() + " • " + match.getSport().getDisplayName());
        locationTitle.getStyleClass().add("card-title");
        locationTitle.setWrapText(true);

        Label dateLabel = new Label("📅 " + match.getMatchDate() + "  🕒 " + match.getMatchTime());
        dateLabel.getStyleClass().add("card-subtitle");

        int current = match.getParticipants() != null ? match.getParticipants().size() : 0;
        int max = match.getRequiredParticipants();
        ProgressBar capacityBar = new ProgressBar((double) current / max);
        capacityBar.getStyleClass().addAll("card-progress-bar", sportClass + "-bar"); // Barra progressiva coordinata
        capacityBar.setPrefWidth(Double.MAX_VALUE);

        Label playersLabel = new Label(current + "/" + max + " iscritti");
        playersLabel.getStyleClass().add("card-detail-text");
        HBox playersBox = new HBox(5, new Label("👥"), playersLabel);
        playersBox.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(locationTitle, dateLabel, new Region(), playersBox, capacityBar);

        // Aggiungi pulsante Join se l'utente sta visualizzando come player
        if (homeController.isViewingAsPlayer() && !match.isFull()) {
            Button joinButton = new Button("Join Match");
            joinButton.getStyleClass().add("join-button");
            joinButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                              "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16;");
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
        String name = sport.name().toUpperCase();
        if (name.contains("SOCCER") || name.contains("CALCIO"))
            return "sport-soccer";
        if (name.contains("BASKET"))
            return "sport-basket";
        if (name.contains("TENNIS"))
            return "sport-tennis";
        if (name.contains("VOLLEY"))
            return "sport-volley";
        if (name.contains("RUGBY"))
            return "sport-rugby";
        return "sport-default";
    }

    // Helper per determinare l'icona in base all'enum Sport
    private String getSportIcon(model.domain.Sport sport) {
        String name = sport.name().toUpperCase();
        if (name.contains("SOCCER") || name.contains("CALCIO"))
            return "⚽";
        if (name.contains("BASKET"))
            return "🏀";
        if (name.contains("TENNIS"))
            return "🎾";
        if (name.contains("VOLLEY"))
            return "🏐";
        if (name.contains("RUGBY"))
            return "🏉";
        if (name.contains("GOLF"))
            return "⛳";
        if (name.contains("SWIM"))
            return "🏊";
        return "🏅";
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
        welcomeLabel.setText("Ciao, " + homeController.getCurrentUser().getUsername());
        roleLabel.setText(homeController.getUserRole().getDisplayName());
    }

    @Override
    public void displayMatches(List<model.bean.MatchBean> matches) {
        matchesContainer.getChildren().clear();
        if (matches == null || matches.isEmpty()) {
            Label empty = new Label("Nessuna partita trovata.");
            empty.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 16px; -fx-padding: 20;");
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
            matchesTitle.setText("Esplora partite");
        } else {
            organizerActionsBox.setVisible(true);
            organizerActionsBox.setManaged(true);
            matchesTitle.setText("Le tue partite");
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
        /* ... */ }

    @Override
    public void displayMenu() {
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

    private void updateStatus(String msg) {
        if (statusLabel != null)
            statusLabel.setText(msg);
    }

    private void showError(String msg) {
        /* Alert */ }

    @Override
    public void close() {
        if (stage != null)
            stage.close();
    }
}