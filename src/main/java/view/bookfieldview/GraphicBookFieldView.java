package view.bookfieldview;

import controller.ApplicationController;
import controller.BookFieldController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.bean.FieldBean;
import model.bean.MatchBean;
import model.service.MapService;
import model.utils.Constants;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

public class GraphicBookFieldView implements BookFieldView {
    private final BookFieldController bookFieldController;
    private Stage stage;
    private static final Logger logger = Logger.getLogger(GraphicBookFieldView.class.getName());

    @FXML
    private Button backButton;
    @FXML
    private Label matchInfoLabel;
    @FXML
    private ComboBox<model.service.FieldService.SortCriteria> sortComboBox;
    @FXML
    private Button refreshButton;
    @FXML
    private Label resultsLabel;
    @FXML
    private VBox fieldsContainer;
    @FXML
    private Label selectedFieldLabel;
    @FXML
    private Button confirmButton;

    private FieldBean selectedField;

    public GraphicBookFieldView(BookFieldController bookFieldController) {
        this.bookFieldController = bookFieldController;
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        // Non utilizzato in questa view
    }

    @Override
    public void display() {
        Platform.runLater(() -> {
            try {
                stage = new Stage();
                stage.setTitle("Sporty - Select Field");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/book_field.fxml"));
                loader.setController(this);
                Parent root = loader.load();

                Scene scene = new Scene(root, 800, 700);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/css/controls-dark.css").toExternalForm());

                stage.setScene(scene);
                stage.setResizable(true);

                initialize();

                stage.show();
            } catch (IOException e) {
                logger.severe("Failed to load book field view: " + e.getMessage());
                showErrorDialog("Failed to load view: " + e.getMessage());
            }

            catch (Exception e) {
                logger.severe("Unexpected error loading book field view: " + e.getMessage());
                showErrorDialog("Unexpected error: " + e.getMessage());
            }
        });
    }

    private void initialize() {
        // Display match info
        displayMatchInfo();

        // Initialize sort combo box
        sortComboBox.getItems().addAll(model.service.FieldService.SortCriteria.values());
        sortComboBox.setConverter(new StringConverter<model.service.FieldService.SortCriteria>() {
            @Override
            public String toString(model.service.FieldService.SortCriteria criteria) {
                return criteria != null ? criteria.getDisplayName() : "";
            }

            @Override
            public model.service.FieldService.SortCriteria fromString(String string) {
                return null;
            }
        });
        sortComboBox.setValue(model.service.FieldService.SortCriteria.PRICE_ASC);

        // Load fields
        searchFields();
    }

    private void displayMatchInfo() {
        MatchBean match = bookFieldController.getCurrentMatchBean();
        if (match != null) {
            String info = String.format("%s - %s - %s %s - %d players",
                    match.getSport().getDisplayName(),
                    match.getCity(),
                    match.getMatchDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    match.getMatchTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    match.getRequiredParticipants());
            matchInfoLabel.setText("Match: " + info);
        }
    }

    private void searchFields() {
        resultsLabel.setText("Searching available fields...");
        fieldsContainer.getChildren().clear();

        new Thread(() -> {
            try {
                List<FieldBean> fields = bookFieldController.searchAvailableFields();

                Platform.runLater(() -> {
                    if (fields.isEmpty()) {
                        resultsLabel.setText("No fields available for the selected criteria");
                        Label emptyLabel = new Label("😕 No fields found");
                        emptyLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");
                        fieldsContainer.getChildren().add(emptyLabel);
                    } else {
                        resultsLabel.setText(fields.size() + " field(s) found");
                        displayFieldsList(fields);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    resultsLabel.setText("Error loading fields");
                    showError("Error searching fields: " + e.getMessage());
                });
            }
        }).start();
    }

    @Override
    public void displayAvailableFields() {
        searchFields();
    }

    private void displayFieldsList(List<FieldBean> fields) {
        fieldsContainer.getChildren().clear();

        for (FieldBean field : fields) {
            VBox fieldCard = createFieldCard(field);
            fieldsContainer.getChildren().add(fieldCard);
        }
    }

    private VBox createFieldCard(FieldBean field) {
        VBox card = new VBox(10);
        card.getStyleClass().add("field-card");
        card.setPadding(new Insets(15));

        // Header with name and indoor/outdoor badge
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(field.getName());
        nameLabel.getStyleClass().add("field-name");

        Label indoorLabel = new Label(field.isIndoor() ? "🏠 Indoor" : "🌤️ Outdoor");
        indoorLabel.setStyle("-fx-background-color: " + (field.isIndoor() ? "#17a2b8" : "#ffc107") +
                "; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 10px;");

        header.getChildren().addAll(nameLabel, indoorLabel);
        card.getChildren().add(header);

        // Sport
        Label sportLabel = new Label("⚽ " + field.getSport().getDisplayName());
        sportLabel.getStyleClass().add(Constants.CSS_FIELD_DETAIL);
        card.getChildren().add(sportLabel);

        // Address
        Label addressLabel = new Label("📍 " + field.getAddress() + ", " + field.getCity());
        addressLabel.getStyleClass().add(Constants.CSS_FIELD_DETAIL);
        card.getChildren().add(addressLabel);

        // Distance (if coordinates available)
        if (field.getLatitude() != null && field.getLongitude() != null) {
            double distance = MapService.calculateDistance(
                    MapService.getDefaultLat(), MapService.getDefaultLon(),
                    field.getLatitude(), field.getLongitude());
            Label distanceLabel = new Label(String.format("🚗 %.1f km from center", distance));
            distanceLabel.getStyleClass().add(Constants.CSS_FIELD_DETAIL);
            distanceLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
            card.getChildren().add(distanceLabel);
        }

        // Price section
        HBox priceBox = new HBox(15);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        VBox priceInfo = new VBox(2);
        Label pricePerHourLabel = new Label("€" + String.format("%.2f", field.getPricePerHour()) + "/hour");
        pricePerHourLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");

        Label pricePerPersonLabel = new Label("€" + String.format("%.2f", field.getPricePerPerson()) + " per person");
        pricePerPersonLabel.getStyleClass().add("field-price");

        priceInfo.getChildren().addAll(pricePerHourLabel, pricePerPersonLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button selectButton = new Button("Select");
        selectButton.getStyleClass().add("primary-button");
        selectButton.setPrefWidth(100);
        selectButton.setOnAction(e -> selectField(field, card));

        priceBox.getChildren().addAll(priceInfo, spacer, selectButton);
        card.getChildren().add(priceBox);

        // Click on card to select
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                selectField(field, card);
            }
        });

        return card;
    }

    private void selectField(FieldBean field, VBox card) {
        // Remove selection from all cards
        fieldsContainer.getChildren().forEach(node -> {
            if (node instanceof VBox) {
                node.getStyleClass().remove("selected");
            }
        });

        // Select this card
        card.getStyleClass().add("selected");
        selectedField = field;
        bookFieldController.setSelectedField(field);

        // Update UI
        selectedFieldLabel.setText("Selected: " + field.getName() + " - €" +
                String.format("%.2f", field.getPricePerPerson()) + "/person");
        confirmButton.setDisable(false);
    }

    // Mappa visuale rimossa - usiamo solo calcolo distanze per ordinamento

    @FXML
    private void handleSort() {
        model.service.FieldService.SortCriteria criteria = sortComboBox.getValue();
        if (criteria != null) {
            List<FieldBean> sortedFields = bookFieldController.sortFields(criteria);
            displayFieldsList(sortedFields);
        }
    }

    @FXML
    private void handleRefresh() {
        selectedField = null;
        selectedFieldLabel.setText("No field selected");
        confirmButton.setDisable(true);
        searchFields();
    }

    @FXML
    private void handleConfirm() {
        if (selectedField == null) {
            showError("Please select a field first");
            return;
        }

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Booking");
        alert.setHeaderText("Confirm Field Booking");
        alert.setContentText(String.format(
                "Field: %s%nPrice: €%.2f per person%n%nProceed with booking?",
                selectedField.getName(),
                selectedField.getPricePerPerson()));

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                confirmBooking();
            }
        });
    }

    private void confirmBooking() {
        try {
            // Navigate to payment
            bookFieldController.proceedToPayment();

        } catch (Exception e) {
            showError("Error proceeding to payment: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        stage.close();
        bookFieldController.navigateBack();
    }

    @Override
    public void displayFieldDetails(int fieldIndex) {
        // Not used in graphic version - details shown in cards
    }

    @Override
    public void displaySortOptions() {
        // Already displayed in UI
    }

    @Override
    public void displayFilterOptions() {
        // Already displayed in UI
    }

    @Override
    public void displayError(String message) {
        showError(message);
    }

    @Override
    public void displaySuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void close() {
        if (stage != null) {
            Platform.runLater(() -> stage.close());
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
