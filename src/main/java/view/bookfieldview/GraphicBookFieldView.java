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
import view.ViewUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class GraphicBookFieldView implements BookFieldView {

    private final BookFieldController controller;
    private Stage stage;
    private final Logger logger = Logger.getLogger(getClass().getName());

    // UI Elements
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
    public void display() {
        Platform.runLater(this::initStage);
    }

    private void initStage() {
        try {
            stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/book_field.fxml"));
            loader.setController(this);

            Scene scene = new Scene(loader.load(), 800, 700);
            scene.getStylesheets().addAll(
                    getClass().getResource("/css/style.css").toExternalForm());

            stage.setTitle("Sporty - " + (controller.isStandaloneMode() ? "Book Field" : "Select Field"));
            stage.setScene(scene);
            stage.show();

            if (controller.isStandaloneMode()) {
                showStandaloneSearchForm();
            } else {
                displayMatchInfo();
                searchFields();
            }

            // Initialize Sort ComboBox
            sortComboBox.getItems().addAll(
                    "Distance (Coming Soon)",
                    "Price: Low to High",
                    "Price: High to Low");
            sortComboBox.setOnAction(e -> handleSort());

        } catch (Exception e) {
            logger.severe("View load failed: " + e.getMessage());
            showAlert("Critical Error", "Could not load view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleSort() {
        String selected = sortComboBox.getValue();
        if (selected == null)
            return;

        switch (selected) {
            case "Price: Low to High" -> controller.sortFieldsByPrice(true);
            case "Price: High to Low" -> controller.sortFieldsByPrice(false);
            case "Distance (Coming Soon)" ->
                showAlert("Info", "Distance sorting is coming soon!", Alert.AlertType.INFORMATION);
            default -> {
                /* Do nothing for unknown values */ }
        }
        // Refresh view with sorted list
        handleSearchResults(controller.getAvailableFields());
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
                match.getRequiredParticipants());

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

                MatchBean contextBean = controller.getCurrentMatchBean();
                contextBean.setSport(sportCombo.getValue());
                contextBean.setCity(cityField.getText().trim());
                contextBean.setMatchDate(datePicker.getValue());
                contextBean.setMatchTime(java.time.LocalTime.parse(timeCombo.getValue()));

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

        CompletableFuture.supplyAsync(() -> controller.searchFieldsForDirectBooking(sport, city))
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

    // --- LOGICA DI RICERCA (Async Clean) ---

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

    // --- UI FACTORY (Creazione Card) ---

    private VBox createFieldCard(FieldBean field) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "elevated-1");
        card.setPadding(new Insets(20));

        // 1. Header (Nome + Badge Indoor/Outdoor)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(
                createLabel(field.getName(), "title-4"),
                createBadge(field.isIndoor()));

        // 2. Info Body
        Label sportLabel = createLabel("⚽ " + field.getSport().getDisplayName(), "text-bold");
        Label addressLabel = createLabel("📍 " + field.getAddress() + ", " + field.getCity(), "text-small");

        // 3. Footer (Prezzo + Bottone)
        HBox footer = createCardFooter(field, card);

        card.getChildren().addAll(header, sportLabel, addressLabel, footer);

        // Interazione
        card.setOnMouseClicked(e -> selectField(field, card));
        return card;
    }

    private HBox createCardFooter(FieldBean field, VBox card) {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox prices = new VBox(2);
        Label perHour = new Label(String.format("€%.2f/hour", field.getPricePerHour()));
        perHour.getStyleClass().add("text-caption");

        Label perPerson = new Label(String.format("€%.2f per person", field.getPricePerPerson()));
        perPerson.getStyleClass().addAll("text-bold", "accent");

        prices.getChildren().addAll(perHour, perPerson);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button selectBtn = new Button("Select");
        selectBtn.getStyleClass().add("button-outlined");
        selectBtn.setOnAction(e -> selectField(field, card));

        footer.getChildren().addAll(prices, spacer, selectBtn);
        return footer;
    }

    // --- HELPERS UI ---

    private Label createLabel(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }

    private Label createBadge(boolean isIndoor) {
        Label l = new Label(isIndoor ? "In" : "Out");
        l.getStyleClass().addAll("text-small", "pill");
        if (isIndoor) {
            l.getStyleClass().add("success"); // Greenish
        } else {
            l.getStyleClass().add("warning"); // Orangeish
        }
        return l;
    }

    // --- LOGICA DI SELEZIONE ---

    private void selectField(FieldBean field, VBox card) {
        // Deseleziona visivamente tutti
        fieldsContainer.getChildren().stream()
                .filter(VBox.class::isInstance)
                .forEach(n -> {
                    n.getStyleClass().remove("accent"); // Remove accent border/glow
                    n.setStyle(""); // Reset inline styles
                });

        // Seleziona nuovo
        card.getStyleClass().add("accent"); // Highlights border in AtlantaFX
        // Add subtle background change to indicate selection
        card.setStyle(
                "-fx-border-color: -color-accent-emphasis; -fx-border-width: 2px; -fx-background-color: -color-bg-subtle;");

        this.selectedField = field;
        controller.setSelectedField(field);

        selectedFieldLabel
                .setText(String.format("Selected: %s - €%.2f/person", field.getName(), field.getPricePerPerson()));
        confirmButton.setDisable(false);
    }

    @FXML
    private void handleConfirm() {
        if (selectedField == null)
            return;

        Alert confirm = createStyledAlert(Alert.AlertType.CONFIRMATION, "Confirm Booking",
                "Book " + selectedField.getName() + "?\n\nTotal per person: €"
                        + String.format("%.2f", selectedField.getPricePerPerson()));

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

    // Unico metodo helper per tutti i messaggi
    private void showAlert(String title, String content, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = createStyledAlert(type, title, content);
            alert.showAndWait();
        });
    }

    // Metodi dell'interfaccia non usati dalla GUI rimossi o lasciati vuoti se
    // obbligatori
    @Override
    public void setApplicationController(controller.ApplicationController app) {
        // Not needed for this view as it uses BookFieldController
    }

    @Override
    public void close() {
        if (stage != null)
            Platform.runLater(stage::close);
    }
    // --- IMPLEMENTAZIONE INTERFACCIA BookFieldView ---

    @Override
    public void displayAvailableFields() {
        searchFields();
    }

    @Override
    public void displayFieldDetails(int fieldIndex) {
        // Not used in graphic version - details shown in cards
    }

    private static final String ERROR_TITLE = "Error";

    /**
     * Creates a styled Alert dialog that matches the application's dark theme.
     */
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