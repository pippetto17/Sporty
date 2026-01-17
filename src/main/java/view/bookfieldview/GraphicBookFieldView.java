package view.bookfieldview;

import controller.BookFieldController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.bean.FieldBean;
import model.bean.MatchBean;

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
                    getClass().getResource("/css/style.css").toExternalForm(),
                    getClass().getResource("/css/controls-dark.css").toExternalForm());

            stage.setTitle("Sporty - Select Field");
            stage.setScene(scene);
            stage.show();

            // Post-show initialization
            displayMatchInfo();
            searchFields();

        } catch (Exception e) {
            logger.severe("View load failed: " + e.getMessage());
            showAlert("Critical Error", "Could not load view: " + e.getMessage(), Alert.AlertType.ERROR);
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
                match.getRequiredParticipants());

        matchInfoLabel.setText("Match: " + info);
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
        card.getStyleClass().add("field-card"); // CSS gestisce padding e background
        card.setPadding(new Insets(15));

        // 1. Header (Nome + Badge Indoor/Outdoor)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(
                createLabel(field.getName(), "field-name"),
                createBadge(field.isIndoor()));

        // 2. Info Body
        Label sportLabel = createLabel("⚽ " + field.getSport().getDisplayName(), "field-detail");
        Label addressLabel = createLabel("📍 " + field.getAddress() + ", " + field.getCity(), "field-detail");

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
        perHour.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");

        Label perPerson = new Label(String.format("€%.2f per person", field.getPricePerPerson()));
        perPerson.getStyleClass().add("field-price");

        prices.getChildren().addAll(perHour, perPerson);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button selectBtn = new Button("Select");
        selectBtn.getStyleClass().add("primary-button");
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
        Label l = new Label(isIndoor ? "🏠 Indoor" : "🌤️ Outdoor");
        // Piccolo inline style per badge dinamico, accettabile qui
        String color = isIndoor ? "#17a2b8" : "#ffc107";
        l.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 10px;");
        return l;
    }

    // --- LOGICA DI SELEZIONE ---

    private void selectField(FieldBean field, VBox card) {
        // Deseleziona visivamente tutti
        fieldsContainer.getChildren().stream()
                .filter(VBox.class::isInstance)
                .forEach(n -> n.getStyleClass().remove("selected"));

        card.getStyleClass().add("selected");
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

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Booking");
        confirm.setHeaderText("Book " + selectedField.getName() + "?");
        confirm.setContentText("Total per person: €" + String.format("%.2f", selectedField.getPricePerPerson()));

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    controller.proceedToPayment();
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
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
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

    @Override
    public void displayError(String message) {
        showAlert(ERROR_TITLE, message, Alert.AlertType.ERROR);
    }

    @Override
    public void displaySuccess(String message) {
        showAlert("Success", message, Alert.AlertType.INFORMATION);
    }
}