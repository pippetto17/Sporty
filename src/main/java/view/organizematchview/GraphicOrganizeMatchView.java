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
import model.bean.FieldBean;
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
    private TextField timeField;
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
    private FieldBean selectedField;

    private boolean isUpdatingCityComboBox = false; // Flag per prevenire loop infinito

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
                stage = new Stage();
                stage.setTitle("Sporty - Organize Match");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/organize_match.fxml"));
                loader.setController(this);
                Parent root = loader.load();

                Scene scene = new Scene(root, 650, 700);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/css/controls-dark.css").toExternalForm());

                stage.setScene(scene);
                stage.setResizable(true);

                initialize();

                stage.show();
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
        // Initialize sport combo box
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

        // Initialize city combo box with Italian cities from controller
        cityComboBox.getItems().addAll(organizeMatchController.getCities());
        cityComboBox.setEditable(true);
        setupCityAutocomplete();

        // Initialize participants spinner (default 1-20, will be updated by sport
        // selection)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1);
        participantsSpinner.setValueFactory(valueFactory);

        // Set date picker to today as minimum
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        organizeMatchController.startNewMatch();
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
            int totalPlayers = selected.getRequiredPlayers();
            int maxAdditional = selected.getAdditionalParticipantsNeeded();

            participantsInfoLabel.setText(String.format(
                    "Need %d more players (Total: %d for %s)",
                    maxAdditional, totalPlayers, selected.getDisplayName()));

            // Update spinner range: da 1 a (requiredPlayers - 1)
            // L'organizer è già il primo, quindi cerca da 1 a max-1 altri giocatori
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
        LocalTime time = parseTime(timeField.getText());
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
        if (timeField.getText() == null || timeField.getText().trim().isEmpty()) {
            showError(Constants.ERROR_PLEASE_ENTER_TIME);
            return false;
        }
        LocalTime time = parseTime(timeField.getText());
        if (time == null) {
            showError(Constants.ERROR_INVALID_TIME_FORMAT);
            return false;
        }
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
        timeField.clear();
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
