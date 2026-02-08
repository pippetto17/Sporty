package view.organizematchview;

import controller.ApplicationController;
import controller.OrganizeMatchController;
import exception.ValidationException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.bean.MatchBean;
import model.domain.Sport;
import model.utils.Constants;
import model.utils.MapsAPI;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private StackPane recapBox;
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
    private VBox notificationBox;
    @FXML
    private Button inviteButton;
    private boolean isUpdatingCityComboBox = false;

    public GraphicOrganizeMatchView(OrganizeMatchController organizeMatchController) {
        this.organizeMatchController = organizeMatchController;
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        // Intentionally empty
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
        organizeMatchController.initializeOrganizeMatch();
        sportComboBox.getItems().addAll(organizeMatchController.getAvailableSports());
        sportComboBox.setConverter(new SportStringConverter());
        sportComboBox.setOnAction(e -> updateParticipantsInfo());
        cityComboBox.getItems().addAll(organizeMatchController.getAvailableCities());
        cityComboBox.setEditable(true);
        setupCityAutocomplete();
        String preferredCity = organizeMatchController.getPreferredCity();
        if (preferredCity != null) {
            cityComboBox.setPromptText("Suggerito: " + preferredCity);
        }
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1);
        participantsSpinner.setValueFactory(valueFactory);
        List<LocalTime> times = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            times.add(LocalTime.of(h, 0));
            times.add(LocalTime.of(h, 30));
        }
        timeComboBox.getItems().addAll(times);
        timeComboBox.setConverter(new TimeStringConverter());
        datePicker.setDayCellFactory(picker -> new FutureDateCell());
    }

    private static class SportStringConverter extends StringConverter<Sport> {
        @Override
        public String toString(Sport sport) {
            return sport != null ? sport.getDisplayName() : "";
        }

        @Override
        public Sport fromString(String string) {
            return null;
        }
    }

    private static class TimeStringConverter extends StringConverter<LocalTime> {
        @Override
        public String toString(LocalTime time) {
            return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        }

        @Override
        public LocalTime fromString(String string) {
            return LocalTime.parse(string);
        }
    }

    private static class FutureDateCell extends DateCell {
        @Override
        public void updateItem(LocalDate date, boolean empty) {
            super.updateItem(date, empty);
            setDisable(empty || date.isBefore(LocalDate.now()));
        }
    }

    private void setupCityAutocomplete() {
        cityComboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (isUpdatingCityComboBox) {
                return;
            }
            isUpdatingCityComboBox = true;
            try {
                if (newValue == null || newValue.isEmpty()) {
                    cityComboBox.hide();
                } else {
                    List<String> filtered = MapsAPI.searchCitiesByPrefix(newValue);
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
        cityComboBox.setOnAction(e -> {
            if (cityComboBox.getValue() != null) {
                cityComboBox.hide();
            }
        });
    }

    private void updateParticipantsInfo() {
        Sport selected = sportComboBox.getValue();
        if (selected != null) {
            participantsInfoLabel.setText(organizeMatchController.getParticipantsInfoText(selected));
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
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll(Constants.CSS_ERROR, Constants.CSS_SUCCESS);
        if (!validateInputs()) {
            return;
        }
        Sport sport = sportComboBox.getValue();
        LocalDate date = datePicker.getValue();
        LocalTime time = timeComboBox.getValue();
        String city = cityComboBox.getValue();
        int participants = participantsSpinner.getValue();
        try {
            organizeMatchController.validateMatchDetails(sport, date, time, city, participants);
        } catch (ValidationException e) {
            showError(e.getMessage());
            return;
        }
        organizeMatchController.setMatchDetails(sport, date, time, city, participants);
        showSummary(sport, date, time, city, participants);
        showSuccess(Constants.SUCCESS_MATCH_DETAILS_FIELD_SELECTION);
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
    private VBox inputContainer;

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

        if (inputContainer != null) {
            inputContainer.setVisible(true);
            inputContainer.setManaged(true);
        }
        if (recapBox != null) {
            recapBox.setVisible(false);
            recapBox.setManaged(false);
        }
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
        // Intentionally empty
    }

    @Override
    public void displayNewMatchForm() {
        // Intentionally empty
    }

    @Override
    public void displayError(String message) {
        showError(message);
    }

    @Override
    public void displaySuccess(String message) {
        showSuccess(message);
    }

    @FXML
    private VBox recapHeader;
    @FXML
    private ImageView recapImage;

    @Override
    public void displayRecap(MatchBean matchBean) {
        Platform.runLater(() -> {
            if (matchBean == null) {
                showError(Constants.ERROR_MATCHBEAN_NULL);
                return;
            }

            if (inputContainer != null) {
                inputContainer.setVisible(false);
                inputContainer.setManaged(false);
            }

            messageLabel.setVisible(false);
            messageLabel.setManaged(false);

            Sport sport = matchBean.getSport();
            applySportTheme(sport);

            recapSportLabel.setText(sport != null ? sport.getDisplayName() : "N/A");
            recapDateLabel.setText(matchBean.getMatchDate() != null ? matchBean.getMatchDate().toString() : "N/A");
            recapTimeLabel.setText(matchBean.getMatchTime() != null ? matchBean.getMatchTime().toString() : "N/A");
            recapCityLabel.setText(matchBean.getCity());

            String statusText = matchBean.getStatus() != null ? matchBean.getStatus().getDisplayName() : "Pending";
            recapStatusLabel.setText(statusText);

            boolean isPending = matchBean.getStatus() != null && matchBean.getStatus().toString().equals("Pending");
            if (isPending) {
                recapStatusLabel.setStyle("-fx-text-fill: #f59e0b;");
            } else {
                recapStatusLabel.setStyle("-fx-text-fill: #10b981;");
            }

            if (notificationBox != null) {
                notificationBox.setVisible(isPending);
                notificationBox.setManaged(isPending);
            }

            recapBox.setVisible(true);
            recapBox.setManaged(true);
        });
    }

    private void applySportTheme(Sport sport) {
        if (recapHeader == null)
            return;

        recapHeader.getStyleClass().removeIf(style -> style.startsWith("sport-"));

        String styleClass = "sport-default";
        String imagePath = Constants.IMAGE_MEDAL_PATH;

        if (sport != null) {
            switch (sport) {
                case FOOTBALL_5, FOOTBALL_8, FOOTBALL_11:
                    styleClass = "sport-soccer";
                    imagePath = Constants.IMAGE_FOOTBALL_PATH;
                    break;
                case BASKETBALL:
                    styleClass = "sport-basket";
                    imagePath = Constants.IMAGE_BASKETBALL_PATH;
                    break;
                case TENNIS_SINGLE, TENNIS_DOUBLE:
                    styleClass = "sport-tennis";
                    imagePath = Constants.IMAGE_TENNIS_PATH;
                    break;
                case PADEL_SINGLE, PADEL_DOUBLE:
                    styleClass = "sport-padel";
                    imagePath = Constants.IMAGE_PADEL_PATH;
                    break;
                default:
                    break;
            }
        }

        recapHeader.getStyleClass().add(styleClass);

        if (recapImage != null) {
            try {
                Image img = new Image(getClass().getResourceAsStream(imagePath));
                recapImage.setImage(img);
            } catch (Exception e) {
                logger.warning("Could not load image: " + imagePath);
            }
        }
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
        organizeMatchController.navigateBack();
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