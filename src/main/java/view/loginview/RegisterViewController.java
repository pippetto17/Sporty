package view.loginview;

import controller.LoginController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.bean.UserBean;
import model.domain.Role;
import model.utils.Constants;

@SuppressWarnings("unused") // Class used by FXML loader
public class RegisterViewController {
    private final LoginController loginController;
    private final Stage stage;

    // Role options constants
    private static final String ROLE_PLAYER_LABEL = "Player";
    private static final String ROLE_ORGANIZER_LABEL = "Organizer";

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Label messageLabel;
    @FXML
    private Button registerButton;
    @FXML
    private Button cancelButton;

    public RegisterViewController(LoginController loginController, Stage stage) {
        this.loginController = loginController;
        this.stage = stage;
    }

    @FXML
    @SuppressWarnings("unused") // Called by FXML
    private void initialize() {
        // Set default role selection
        if (roleComboBox.getItems().isEmpty()) {
            roleComboBox.getItems().addAll(ROLE_PLAYER_LABEL, ROLE_ORGANIZER_LABEL);
        }
        roleComboBox.setValue(ROLE_PLAYER_LABEL);
    }

    @FXML
    @SuppressWarnings("unused") // Called by FXML
    private void handleRegisterSubmit() {
        // Clear previous messages
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll(Constants.CSS_ERROR, Constants.CSS_SUCCESS);

        try {
            // Validate inputs
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String name = nameField.getText().trim();
            String surname = surnameField.getText().trim();
            String selectedRole = roleComboBox.getValue();

            if (username.isEmpty() || password.isEmpty() || name.isEmpty() ||
                surname.isEmpty() || selectedRole == null) {
                showError(Constants.ERROR_ALL_FIELDS_REQUIRED);
                return;
            }

            // Map role string to role code
            int roleCode = selectedRole.equals(ROLE_PLAYER_LABEL) ? Role.PLAYER.getCode() : Role.ORGANIZER.getCode();

            // Register user
            UserBean userBean = new UserBean(username, password);
            loginController.register(userBean, name, surname, roleCode);

            // Show success message
            showSuccess(Constants.SUCCESS_REGISTRATION);

            // Close window after 1.5 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(stage::close);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    @SuppressWarnings("unused") // Called by FXML
    private void handleCancel() {
        stage.close();
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
}

