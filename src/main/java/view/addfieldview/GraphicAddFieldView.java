package view.addfieldview;

import controller.FieldManagerController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.bean.FieldBean;
import model.domain.Sport;
import model.utils.Constants;

import java.util.Arrays;
import java.util.List;

/**
 * Graphic implementation of AddFieldView for adding new fields.
 */
public class GraphicAddFieldView implements AddFieldView {
    private final FieldManagerController controller;
    private Stage stage;

    // Form fields
    private TextField structureNameField;
    private TextField fieldNameField;
    private TextField addressField;
    private ComboBox<String> cityComboBox;
    private ComboBox<Sport> sportComboBox;
    private RadioButton indoorRadio;
    private TextField priceField;
    private CheckBox autoApproveCheckBox;

    private static final List<String> CITIES = Arrays.asList(
            "Rome", "Milan", "Naples", "Turin", "Palermo",
            "Genoa", "Bologna", "Florence", "Bari", "Catania",
            "Venice", "Verona", "Messina", "Padua", "Trieste");

    public GraphicAddFieldView(FieldManagerController controller) {
        this.controller = controller;
    }

    @Override
    public void display() {
        Platform.runLater(() -> {
            stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Field");

            VBox root = createLayout();

            Scene scene = new Scene(root, 500, 550);

            // Load stylesheets with null checks
            var styleResource = getClass().getResource("/css/style.css");
            if (styleResource != null) {
                scene.getStylesheets().add(styleResource.toExternalForm());
            }

            var controlsResource = getClass().getResource("/css/controls-dark.css");
            if (controlsResource != null) {
                scene.getStylesheets().add(controlsResource.toExternalForm());
            }

            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        });
    }

    private VBox createLayout() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("main-container");

        // Title
        Label titleLabel = new Label("Add New Field");
        titleLabel.getStyleClass().add("page-title");

        // Form
        GridPane form = createForm();

        // Buttons
        HBox buttonBox = createButtons();

        root.getChildren().addAll(titleLabel, form, buttonBox);
        return root;
    }

    private GridPane createForm() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        int row = 0;

        // Structure Name
        Label structureLabel = new Label("Structure Name:");
        structureLabel.getStyleClass().add(Constants.CSS_FIELD_LABEL);
        structureNameField = new TextField();
        structureNameField.setPromptText("e.g., City Sports Center");
        structureNameField.getStyleClass().add(Constants.CSS_CUSTOM_TEXT_FIELD);
        structureNameField.setPrefWidth(300);
        grid.add(structureLabel, 0, row);
        grid.add(structureNameField, 1, row++);

        // Field Name
        Label nameLabel = new Label("Field Name:");
        nameLabel.getStyleClass().add(Constants.CSS_FIELD_LABEL);
        fieldNameField = new TextField();
        fieldNameField.setPromptText("e.g., Court 1");
        fieldNameField.getStyleClass().add(Constants.CSS_CUSTOM_TEXT_FIELD);
        grid.add(nameLabel, 0, row);
        grid.add(fieldNameField, 1, row++);

        // Address
        Label addressLabel = new Label("Address:");
        addressLabel.getStyleClass().add(Constants.CSS_FIELD_LABEL);
        addressField = new TextField();
        addressField.setPromptText("e.g., Via Roma 123");
        addressField.getStyleClass().add(Constants.CSS_CUSTOM_TEXT_FIELD);
        grid.add(addressLabel, 0, row);
        grid.add(addressField, 1, row++);

        // City
        Label cityLabel = new Label("City:");
        cityLabel.getStyleClass().add(Constants.CSS_FIELD_LABEL);
        cityComboBox = new ComboBox<>();
        cityComboBox.getItems().addAll(CITIES);
        cityComboBox.setPromptText("Select city");
        cityComboBox.getStyleClass().add(Constants.CSS_CUSTOM_COMBO_BOX);
        cityComboBox.setPrefWidth(300);
        grid.add(cityLabel, 0, row);
        grid.add(cityComboBox, 1, row++);

        // Sport
        Label sportLabel = new Label("Sport:");
        sportLabel.getStyleClass().add(Constants.CSS_FIELD_LABEL);
        sportComboBox = new ComboBox<>();
        sportComboBox.getItems().addAll(Sport.values());
        sportComboBox.setPromptText("Select sport");
        sportComboBox.getStyleClass().add(Constants.CSS_CUSTOM_COMBO_BOX);
        sportComboBox.setPrefWidth(300);
        grid.add(sportLabel, 0, row);
        grid.add(sportComboBox, 1, row++);

        // Indoor/Outdoor
        Label typeLabel = new Label("Type:");
        typeLabel.getStyleClass().add(Constants.CSS_FIELD_LABEL);
        ToggleGroup typeGroup = new ToggleGroup();
        indoorRadio = new RadioButton("Indoor");
        RadioButton outdoorRadio = new RadioButton("Outdoor");
        indoorRadio.setToggleGroup(typeGroup);
        outdoorRadio.setToggleGroup(typeGroup);
        outdoorRadio.setSelected(true);
        HBox typeBox = new HBox(15, indoorRadio, outdoorRadio);
        grid.add(typeLabel, 0, row);
        grid.add(typeBox, 1, row++);

        // Price
        Label priceLabel = new Label("Price per Hour (€):");
        priceLabel.getStyleClass().add(Constants.CSS_FIELD_LABEL);
        priceField = new TextField();
        priceField.setPromptText("e.g., 25.00");
        priceField.getStyleClass().add(Constants.CSS_CUSTOM_TEXT_FIELD);
        grid.add(priceLabel, 0, row);
        grid.add(priceField, 1, row++);

        // Auto-approve
        autoApproveCheckBox = new CheckBox("Auto-approve booking requests");
        grid.add(new Label(), 0, row);
        grid.add(autoApproveCheckBox, 1, row);

        return grid;
    }

    private HBox createButtons() {
        Button addButton = new Button("Add Field");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> handleAdd());

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.setOnAction(e -> Platform.runLater(this::close));

        HBox buttonBox = new HBox(15, addButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);
        return buttonBox;
    }

    private void handleAdd() {
        try {
            // Validate
            if (!validateInput()) {
                return;
            }

            // Create bean
            FieldBean field = new FieldBean();
            field.setStructureName(structureNameField.getText().trim());
            field.setName(fieldNameField.getText().trim());
            field.setAddress(addressField.getText().trim());
            field.setCity(cityComboBox.getValue());
            field.setSport(sportComboBox.getValue());
            field.setIndoor(indoorRadio.isSelected());
            field.setPricePerHour(Double.parseDouble(priceField.getText().trim()));
            field.setAutoApprove(autoApproveCheckBox.isSelected());

            // Coordinates will be geocoded from address in future implementation
            // For now, leave them null

            // Add field
            controller.addNewField(field);

            displaySuccess("Field added successfully!");

        } catch (NumberFormatException e) {
            displayError("Invalid number format. Check price and coordinates.");
        } catch (Exception e) {
            displayError("Error adding field: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (structureNameField.getText().trim().isEmpty()) {
            displayError("Structure name is required");
            return false;
        }
        if (fieldNameField.getText().trim().isEmpty()) {
            displayError("Field name is required");
            return false;
        }
        if (addressField.getText().trim().isEmpty()) {
            displayError("Address is required");
            return false;
        }
        if (cityComboBox.getValue() == null) {
            displayError("City is required");
            return false;
        }
        if (sportComboBox.getValue() == null) {
            displayError("Sport is required");
            return false;
        }
        if (priceField.getText().trim().isEmpty()) {
            displayError("Price is required");
            return false;
        }

        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                displayError("Price must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            displayError("Invalid price format");
            return false;
        }

        return true;
    }

    @Override
    public void displaySuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            close();
        });
    }

    @Override
    public void displayError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void close() {
        if (stage != null) {
            stage.close();
        }
    }
}
