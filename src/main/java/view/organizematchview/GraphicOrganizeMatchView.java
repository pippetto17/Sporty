package view.organizematchview;

import controller.ApplicationController;
import controller.OrganizeMatchController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.bean.MatchBean;
import model.domain.Sport;
import model.utils.Constants;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

public class GraphicOrganizeMatchView implements OrganizeMatchView {
    private static final Logger logger = Logger.getLogger(GraphicOrganizeMatchView.class.getName());

    private final OrganizeMatchController organizeMatchController;
    private Stage stage;

    @FXML
    private Button backButton;
    @FXML
    private ComboBox<Sport> sportComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<LocalTime> timeComboBox;
    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private Spinner<Integer> participantsSpinner;
    @FXML
    private Label participantsInfoLabel;
    @FXML
    private VBox summaryBox;
    @FXML
    private Label summarySport;
    @FXML
    private Label summaryDateTime;
    @FXML
    private Label summaryCity;
    @FXML
    private Label summaryParticipants;
    @FXML
    private Label messageLabel;
    @FXML
    private Button searchFieldsButton;
    @FXML
    private VBox recapBox;
    @FXML
    private Label recapSportLabel;
    @FXML
    private Label recapDateLabel;
    @FXML
    private Label recapTimeLabel;
    @FXML
    private Label recapCityLabel;
    @FXML
    private Label recapStatusLabel;
    @FXML
    private Button inviteButton;

    private boolean isUpdatingCityComboBox = false;

    public GraphicOrganizeMatchView(OrganizeMatchController organizeMatchController) {
        this.organizeMatchController = organizeMatchController;
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        // Not used in this view
    }

    @Override
    public void display() {
        Platform.runLater(() -> {
            try {
                if (stage == null) {
                    stage = new Stage();
                    stage.setTitle("Sporty - Organize Match");

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/organize_match.fxml"));
                    loader.setController(this);
                    Parent root = loader.load();

                    Scene scene = new Scene(root, 700, 750);
                    scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

                    stage.setScene(scene);
                    stage.setResizable(true);

                    initialize();
                }

                stage.show();
                stage.toFront();
            } catch (IOException e) {
                logger.severe(Constants.ERROR_LOAD_ORGANIZE_MATCH_VIEW + e.getMessage());
                showErrorDialog(Constants.ERROR_LOAD_ORGANIZE_MATCH_VIEW + e.getMessage());
            } catch (Exception e) {
                logger.severe(Constants.ERROR_UNEXPECTED_LOADING_VIEW + e.getMessage());
                showErrorDialog(Constants.ERROR_UNEXPECTED + e.getMessage());
            }
        });
    }

    private void initialize() {
        // BCE Criterion 3: First interaction loads all required entities into memory
        organizeMatchController.initializeOrganizeMatch();

        // Initialize sport combo box from controller
        sportComboBox.getItems().addAll(organizeMatchController.getAvailableSports());
        sportComboBox.setConverter(new StringConverter<Sport>() {
            @Override
            public String toString(Sport sport) {
                return sport != null ? sport.getDisplayName() : "";
            }

            @Override
            public Sport fromString(String string) {
                return null;
            }
        });

        // Listen to sport selection to update participants info
        sportComboBox.setOnAction(e -> updateParticipantsInfo());

        // Initialize city combo box
        cityComboBox.getItems().addAll(organizeMatchController.getAvailableCities());
        cityComboBox.setEditable(true);
        setupCityAutocomplete();

        // Show preferred city suggestion if available
        String preferredCity = organizeMatchController.getPreferredCity();
        if (preferredCity != null) {
            cityComboBox.setPromptText("Suggerito: " + preferredCity);
        }

        // Initialize participants spinner (default 1-20, will be updated by sport
        // selection)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1);
        participantsSpinner.setValueFactory(valueFactory);

        // Initialize time combo box (every 30 minutes)
        List<LocalTime> times = new java.util.ArrayList<>();
        for (int h = 0; h < 24; h++) {
            times.add(LocalTime.of(h, 0));
            times.add(LocalTime.of(h, 30));
        }
        timeComboBox.getItems().addAll(times);
        timeComboBox.setConverter(new StringConverter<LocalTime>() {
            @Override
            public String toString(LocalTime time) {
                return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
            }

            @Override
            public LocalTime fromString(String string) {
                return LocalTime.parse(string);
            }
        });

        // Set date picker to today as minimum
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    private void setupCityAutocomplete() {
        // Filtro in tempo reale mentre l'utente digita
        cityComboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (isUpdatingCityComboBox) {
                return; // Evita loop infinito
            }

            isUpdatingCityComboBox = true;
            try {
                if (newValue == null || newValue.isEmpty()) {
                    cityComboBox.hide();
                } else {
                    List<String> filtered = organizeMatchController.searchCitiesByPrefix(newValue);

                    // Aggiorna solo se ci sono risultati diversi
                    if (!filtered.equals(cityComboBox.getItems())) {
                        cityComboBox.getItems().setAll(filtered);
                    }

                    if (!filtered.isEmpty() && !cityComboBox.isShowing()) {
                        cityComboBox.show();
                    }
                }
            } finally {
                isUpdatingCityComboBox = false;
            }
        });

        // Auto-completa quando l'utente seleziona o esce dal campo
        cityComboBox.setOnAction(e -> {
            if (cityComboBox.getValue() != null) {
                cityComboBox.hide();
            }
        });
    }

    private void updateParticipantsInfo() {
        Sport selected = sportComboBox.getValue();
        if (selected != null) {
            // Get info text from controller
            participantsInfoLabel.setText(organizeMatchController.getParticipantsInfoText(selected));

            // Get max additional participants from controller
            int maxAdditional = organizeMatchController.getMaxAdditionalParticipants(selected);
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,
                    maxAdditional, maxAdditional);
            participantsSpinner.setValueFactory(valueFactory);
        } else {
            participantsInfoLabel.setText(Constants.ERROR_SELECT_SPORT_FIRST);
        }
    }

    @FXML
    private void handleSearchFields() {
        // Clear previous messages
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll(Constants.CSS_ERROR, Constants.CSS_SUCCESS);

        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Get values
        Sport sport = sportComboBox.getValue();
        LocalDate date = datePicker.getValue();
        LocalTime time = timeComboBox.getValue();
        String city = cityComboBox.getValue();
        int participants = participantsSpinner.getValue();

        // Validate with controller
        if (!organizeMatchController.validateMatchDetails(sport, date, time, city, participants)) {
            showError(Constants.ERROR_INVALID_MATCH_DETAILS);
            return;
        }

        // Set match details
        organizeMatchController.setMatchDetails(sport, date, time, city, participants);

        // Show summary
        showSummary(sport, date, time, city, participants);

        // Show success message
        showSuccess(Constants.SUCCESS_MATCH_DETAILS_FIELD_SELECTION);

        // Navigate to field selection after a short delay
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                Platform.runLater(() -> {
                    stage.close();
                    organizeMatchController.proceedToFieldSelection();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private boolean validateInputs() {
        // Only basic UI validation - business validation is in controller
        if (sportComboBox.getValue() == null) {
            showError(Constants.ERROR_PLEASE_SELECT_SPORT);
            return false;
        }
        if (datePicker.getValue() == null) {
            showError(Constants.ERROR_PLEASE_SELECT_DATE);
            return false;
        }
        if (timeComboBox.getValue() == null) {
            showError(Constants.ERROR_PLEASE_ENTER_TIME);
            return false;
        }
        // Removed manual text parsing check
        if (cityComboBox.getValue() == null || cityComboBox.getValue().trim().isEmpty()) {
            showError(Constants.ERROR_PLEASE_SELECT_CITY);
            return false;
        }
        return true;
    }

    private void showSummary(Sport sport, LocalDate date, LocalTime time, String city, int participants) {
        summaryBox.setVisible(true);
        summaryBox.setManaged(true);

        summarySport.setText(sport.getDisplayName());
        summaryDateTime.setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " at " + time.format(DateTimeFormatter.ofPattern("HH:mm")));
        summaryCity.setText(city);
        summaryParticipants.setText(String.valueOf(participants));
    }

    @FXML
    private void handleClear() {
        sportComboBox.setValue(null);
        datePicker.setValue(null);
        timeComboBox.setValue(null);
        cityComboBox.setValue(null);
        cityComboBox.getEditor().clear();
        participantsSpinner.getValueFactory().setValue(1);
        summaryBox.setVisible(false);
        summaryBox.setManaged(false);
        messageLabel.setText("");
        participantsInfoLabel.setText(Constants.ERROR_SELECT_SPORT_FIRST);
    }

    @FXML
    private void handleBack() {
        stage.close();
        organizeMatchController.navigateBack();
    }

    @Override
    public void close() {
        if (stage != null) {
            Platform.runLater(() -> stage.close());
        }
    }

    @Override
    public void displayMatchList() {
        // Not used in graphic version - shown in Home
    }

    @Override
    public void displayNewMatchForm() {
        // Already displayed
    }

    @Override
    public void displayError(String message) {
        showError(message);
    }

    @Override
    public void displaySuccess(String message) {
        showSuccess(message);
    }

    @Override
    public void displayRecap(MatchBean matchBean) {
        Platform.runLater(() -> {
            if (matchBean == null) {
                showError(Constants.ERROR_MATCHBEAN_NULL);
                return;
            }

            // Nascondi form e mostra recap
            sportComboBox.setVisible(false);
            sportComboBox.setManaged(false);
            datePicker.setVisible(false);
            datePicker.setManaged(false);
            timeComboBox.setVisible(false);
            timeComboBox.setManaged(false);
            cityComboBox.setVisible(false);
            cityComboBox.setManaged(false);
            participantsSpinner.setVisible(false);
            participantsSpinner.setManaged(false);
            participantsInfoLabel.setVisible(false);
            participantsInfoLabel.setManaged(false);
            summaryBox.setVisible(false);
            summaryBox.setManaged(false);
            searchFieldsButton.setVisible(false);
            searchFieldsButton.setManaged(false);
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);

            // Popolamento recap
            recapSportLabel.setText(matchBean.getSport() != null ? matchBean.getSport().getDisplayName() : "N/A");
            recapDateLabel.setText(matchBean.getMatchDate() != null ? matchBean.getMatchDate().toString() : "N/A");
            recapTimeLabel.setText(matchBean.getMatchTime() != null ? matchBean.getMatchTime().toString() : "N/A");
            recapCityLabel.setText(matchBean.getCity());
            recapStatusLabel.setText(matchBean.getStatus() != null ? matchBean.getStatus().toString() : "CONFIRMED");

            recapBox.setVisible(true);
            recapBox.setManaged(true);
        });
    }

    @FXML
    private void handleInvite() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invite Player");
        alert.setHeaderText(null);
        alert.setContentText("Invite player feature coming soon!");
        alert.showAndWait();
    }

    @FXML
    private void handleHomeFromRecap() {
        stage.close();
        for (int i = 0; i < 4; i++) {
            organizeMatchController.navigateBack();
        }
    }

    private void showError(String message) {
        messageLabel.getStyleClass().removeAll(Constants.CSS_SUCCESS);
        messageLabel.getStyleClass().add(Constants.CSS_ERROR);
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.getStyleClass().removeAll(Constants.CSS_ERROR);
        messageLabel.getStyleClass().add(Constants.CSS_SUCCESS);
        messageLabel.setText(message);
    }

    private void showErrorDialog(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Constants.DIALOG_TITLE_ERROR);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
