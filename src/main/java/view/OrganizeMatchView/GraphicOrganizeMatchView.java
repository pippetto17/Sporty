package view.OrganizeMatchView;

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
import model.utils.ItalianCities;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class GraphicOrganizeMatchView implements OrganizeMatchView {
    private final OrganizeMatchController organizeMatchController;
    private ApplicationController applicationController;
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
        this.applicationController = applicationController;
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

                stage.setScene(scene);
                stage.setResizable(true);

                initialize();

                stage.show();
            } catch (IOException e) {
                e.printStackTrace(); // Print full stack trace
                showErrorDialog("Failed to load organize match view: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace(); // Print full stack trace
                showErrorDialog("Unexpected error: " + e.getMessage());
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

        // Initialize city combo box with Italian cities
        cityComboBox.getItems().addAll(ItalianCities.CITIES);
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
                    List<String> filtered = ItalianCities.searchByPrefix(newValue);

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
            participantsInfoLabel.setText("Select a sport first");
        }
    }

    @FXML
    private void handleSearchFields() {
        // Clear previous messages
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("error", "success");

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
            showError("Invalid match details. Please check your inputs.");
            return;
        }

        // Set match details
        organizeMatchController.setMatchDetails(sport, date, time, city, participants);

        // Show summary
        showSummary(sport, date, time, city, participants);

        // Show success message
        showSuccess("Match details saved! Proceeding to field selection...");

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
        if (sportComboBox.getValue() == null) {
            showError("Please select a sport");
            return false;
        }
        if (datePicker.getValue() == null) {
            showError("Please select a date");
            return false;
        }
        if (timeField.getText() == null || timeField.getText().trim().isEmpty()) {
            showError("Please enter a time");
            return false;
        }
        LocalTime time = parseTime(timeField.getText());
        if (time == null) {
            showError("Invalid time format. Use HH:MM (e.g., 18:30)");
            return false;
        }
        if (cityComboBox.getValue() == null || cityComboBox.getValue().trim().isEmpty()) {
            showError("Please select a city from the list");
            return false;
        }
        if (!ItalianCities.isValidCity(cityComboBox.getValue())) {
            showError("Please select a valid Italian city from the list");
            return false;
        }
        return true;
    }

    private LocalTime parseTime(String timeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return LocalTime.parse(timeStr.trim(), formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
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
        participantsInfoLabel.setText("Select a sport first");
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
        messageLabel.getStyleClass().removeAll("success");
        messageLabel.getStyleClass().add("error");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.getStyleClass().removeAll("error");
        messageLabel.getStyleClass().add("success");
        messageLabel.setText(message);
    }

    private void showErrorDialog(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
