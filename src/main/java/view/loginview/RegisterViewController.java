package view.loginview;

import controller.LoginController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.bean.UserBean;
import model.domain.Role;

public class RegisterViewController {
    private final LoginController loginController;
    private final Stage stage;

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
    private void initialize() {
        // Set default role selection
        if (roleComboBox.getItems().isEmpty()) {
            roleComboBox.getItems().addAll("Player", "Organizer");
        }
        roleComboBox.setValue("Player");
    }

    @FXML
    private void handleRegisterSubmit() {
        // Clear previous messages
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("error", "success");

        try {
            // Validate inputs
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String name = nameField.getText().trim();
            String surname = surnameField.getText().trim();
            String selectedRole = roleComboBox.getValue();

            if (username.isEmpty() || password.isEmpty() || name.isEmpty() ||
                surname.isEmpty() || selectedRole == null) {
                showError("All fields are required");
                return;
            }

            // Map role string to role code
            int roleCode = selectedRole.equals("Player") ? Role.PLAYER.getCode() : Role.ORGANIZER.getCode();

            // Register user
            UserBean userBean = new UserBean(username, password);
            loginController.register(userBean, name, surname, roleCode);

            // Show success message
            showSuccess("Registration successful!");

            // Close window after 1.5 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> stage.close());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
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
}

