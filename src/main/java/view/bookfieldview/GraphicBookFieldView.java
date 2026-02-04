package view.bookfieldview;

import controller.BookFieldController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.bean.FieldBean;
import model.bean.MatchBean;
import model.utils.Constants;
import view.ViewUtils;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class GraphicBookFieldView implements BookFieldView {
    private final BookFieldController controller;
    private Stage stage;
    private final Logger logger = Logger.getLogger(getClass().getName());
    @FXML
    private Label matchInfoLabel;
    @FXML
    private Label resultsLabel;
    @FXML
    private Label selectedFieldLabel;
    @FXML
    private VBox fieldsContainer;
    @FXML
    private Button confirmButton;
    @FXML
    private javafx.scene.control.ComboBox<String> sortComboBox;
    private FieldBean selectedField;

    public GraphicBookFieldView(BookFieldController controller) {
        this.controller = controller;
    }

    @Override
    public void setApplicationController(controller.ApplicationController app) {
        // Intentionally empty
    }

    @Override
    public void display() {
        Platform.runLater(this::initStage);
    }

    private void initStage() {
        try {
            if (stage == null) {
                stage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/book_field.fxml"));
                loader.setController(this);
                Scene scene = new Scene(loader.load(), 800, 700);
                scene.getStylesheets().addAll(
                        getClass().getResource("/css/style.css").toExternalForm());
                stage.setTitle("Sporty - " + (controller.isStandaloneMode() ? "Book Field" : "Select Field"));
                stage.setScene(scene);
                if (controller.isStandaloneMode()) {
                    showStandaloneSearchForm();
                } else {
                    displayMatchInfo();
                    searchFields();
                }
                sortComboBox.getItems().addAll(
                        "Name: A-Z",
                        "Distance (Coming Soon)");
                sortComboBox.setOnAction(e -> handleSort());
            }
            stage.show();
            stage.toFront();
        } catch (Exception e) {
            logger.severe("View load failed: " + e.getMessage());
            showAlert("Critical Error", "Could not load view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleSort() {
        String selected = sortComboBox.getValue();
        if (selected == null)
            return;
        if (selected.equals("Distance (Coming Soon)")) {
            showAlert("Info", "Distance sorting is coming soon!", Alert.AlertType.INFORMATION);
        }
    }

    private void displayMatchInfo() {
        MatchBean match = controller.getCurrentMatchBean();
        if (match == null)
            return;
        String info = String.format("%s - %s - %s %s - %d players",
                match.getSport().getDisplayName(),
                match.getCity(),
                match.getMatchDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                match.getMatchTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                match.getSport().getRequiredPlayers());
        matchInfoLabel.setText("Match: " + info);
    }

    private void showStandaloneSearchForm() {
        matchInfoLabel.setText("Select booking parameters:");
        javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Book Field - Search Parameters");
        dialog.setHeaderText("Enter search criteria");
        javafx.scene.control.ComboBox<model.domain.Sport> sportCombo = new javafx.scene.control.ComboBox<>();
        sportCombo.getItems().addAll(model.domain.Sport.values());
        sportCombo.setPromptText("Select Sport");
        javafx.scene.control.TextField cityField = new javafx.scene.control.TextField();
        cityField.setPromptText("City (e.g., Milan)");
        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker();
        datePicker.setPromptText("Select Date");
        datePicker.setValue(java.time.LocalDate.now());
        javafx.scene.control.ComboBox<String> timeCombo = new javafx.scene.control.ComboBox<>();
        for (int h = 8; h <= 22; h++) {
            timeCombo.getItems().add(String.format("%02d:00", h));
        }
        timeCombo.setPromptText("Select Time");
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Sport:"), 0, 0);
        grid.add(sportCombo, 1, 0);
        grid.add(new Label("City:"), 0, 1);
        grid.add(cityField, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Time:"), 0, 3);
        grid.add(timeCombo, 1, 3);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ViewUtils.applyStylesheets(dialog.getDialogPane());
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && sportCombo.getValue() != null &&
                    !cityField.getText().trim().isEmpty() && datePicker.getValue() != null &&
                    timeCombo.getValue() != null) {
                controller.updateMatchParameters(
                        sportCombo.getValue(),
                        cityField.getText().trim(),
                        datePicker.getValue(),
                        java.time.LocalTime.parse(timeCombo.getValue()));
                MatchBean contextBean = controller.getCurrentMatchBean();
                matchInfoLabel.setText(String.format("Booking: %s - %s - %s %s",
                        contextBean.getSport().getDisplayName(),
                        contextBean.getCity(),
                        contextBean.getMatchDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        contextBean.getMatchTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
                searchFieldsStandalone(contextBean.getSport(), contextBean.getCity());
            } else {
                controller.navigateBack();
            }
        });
    }

    private void searchFieldsStandalone(model.domain.Sport sport, String city) {
        if (resultsLabel != null) {
            resultsLabel.setText("Searching available fields...");
        }
        if (fieldsContainer != null) {
            fieldsContainer.getChildren().clear();
        }
        CompletableFuture.supplyAsync(() -> {
            MatchBean mb = controller.getCurrentMatchBean();
            return controller.searchFieldsForDirectBooking(sport, city, mb.getMatchDate(), mb.getMatchTime());
        })
                .thenAcceptAsync(this::handleSearchResults, Platform::runLater)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        if (resultsLabel != null) {
                            resultsLabel.setText("Error loading fields");
                        }
                        showAlert(ERROR_TITLE, "Search failed: " + ex.getMessage(), Alert.AlertType.ERROR);
                    });
                    return null;
                });
    }

    private void searchFields() {
        resultsLabel.setText("Searching available fields...");
        fieldsContainer.getChildren().clear();
        CompletableFuture.supplyAsync(controller::searchAvailableFields)
                .thenAcceptAsync(this::handleSearchResults, Platform::runLater)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        resultsLabel.setText("Error loading fields");
                        showAlert(ERROR_TITLE, "Search failed: " + ex.getMessage(), Alert.AlertType.ERROR);
                    });
                    return null;
                });
    }

    private void handleSearchResults(List<FieldBean> fields) {
        if (fields.isEmpty()) {
            resultsLabel.setText("No fields found");
            fieldsContainer.getChildren().add(new Label("😕 No fields found for this criteria"));
        } else {
            resultsLabel.setText(fields.size() + " field(s) found");
            fields.forEach(field -> fieldsContainer.getChildren().add(createFieldCard(field)));
        }
    }

    private VBox createFieldCard(FieldBean field) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "elevated-1");
        card.setPadding(new Insets(20));
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(
                createLabel(field.getName(), "title-4"));
        Label sportLabel = createLabel("⚽ " + field.getSport().getDisplayName(), "text-bold");
        Label cityLabel = createLabel("📍 " + field.getCity(), "text-small");
        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(sportLabel, cityLabel);
        if (field.getAddress() != null && !field.getAddress().isEmpty()) {
            Label addressLabel = createLabel("🏠 " + field.getAddress(), "text-small");
            infoBox.getChildren().add(addressLabel);
        }
        HBox pricingBox = new HBox(20);
        pricingBox.setAlignment(Pos.CENTER_LEFT);
        Label priceHourLabel = createLabel(String.format("💰 €%.2f/ora", field.getPricePerHour()), "text-bold");
        Label pricePersonLabel = createLabel(String.format("👤 €%.2f/persona", field.getPricePerPerson()), "accent");
        pricingBox.getChildren().addAll(priceHourLabel, pricePersonLabel);
        HBox footer = createCardFooter(field, card);
        card.getChildren().addAll(header, infoBox, pricingBox, footer);
        card.setOnMouseClicked(e -> selectField(field, card));
        return card;
    }

    private HBox createCardFooter(FieldBean field, VBox card) {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button selectBtn = new Button("Select");
        selectBtn.getStyleClass().add("button-outlined");
        selectBtn.setOnAction(e -> selectField(field, card));
        footer.getChildren().addAll(spacer, selectBtn);
        return footer;
    }

    private Label createLabel(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }

    private void selectField(FieldBean field, VBox card) {
        fieldsContainer.getChildren().stream()
                .filter(VBox.class::isInstance)
                .forEach(n -> {
                    n.getStyleClass().remove(Constants.CSS_ACCENT);
                    n.setStyle("");
                });
        card.getStyleClass().add(Constants.CSS_ACCENT);
        card.setStyle(
                "-fx-border-color: -color-accent-emphasis; -fx-border-width: 2px; -fx-background-color: -color-bg-subtle;");
        this.selectedField = field;
        controller.setSelectedField(field);
        selectedFieldLabel
                .setText(String.format("Selected: %s", field.getName()));
        confirmButton.setDisable(false);
    }

    @FXML
    private void handleConfirm() {
        if (selectedField == null)
            return;
        Alert confirm = createStyledAlert(Alert.AlertType.CONFIRMATION, "Confirm Booking",
                "Book " + selectedField.getName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    controller.proceedToPayment();
                } catch (exception.ValidationException e) {
                    showAlert(ERROR_TITLE, e.getMessage(), Alert.AlertType.ERROR);
                } catch (Exception e) {
                    showAlert(ERROR_TITLE, "Cannot proceed: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        selectedField = null;
        selectedFieldLabel.setText("No field selected");
        confirmButton.setDisable(true);
        searchFields();
    }

    @FXML
    private void handleBack() {
        if (stage != null)
            stage.close();
        controller.navigateBack();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = createStyledAlert(type, title, content);
            alert.showAndWait();
        });
    }

    @Override
    public void close() {
        if (stage != null)
            Platform.runLater(stage::close);
    }

    @Override
    public void displayAvailableFields() {
        searchFields();
    }

    @Override
    public void displayFieldDetails(int fieldIndex) {
        // Intentionally empty
    }

    private static final String ERROR_TITLE = "Error";

    private Alert createStyledAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        ViewUtils.applyStylesheets(alert.getDialogPane());
        return alert;
    }

    @Override
    public void displayError(String message) {
        showAlert(ERROR_TITLE, message, Alert.AlertType.ERROR);
    }

    @Override
    public void displaySuccess(String message) {
        showAlert("Success", message, Alert.AlertType.INFORMATION);
    }
}