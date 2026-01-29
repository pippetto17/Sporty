package view.loginview;

import controller.ApplicationController;
import controller.LoginController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.bean.UserBean;
import model.utils.Constants;
import view.ViewUtils;

import java.io.IOException;
import java.util.logging.Logger;

public class GraphicLoginView extends Application implements LoginView {
    private static final Logger logger = Logger.getLogger(GraphicLoginView.class.getName());

    // Static fields per passare i controller all'istanza creata da launch()
    private static LoginController staticLoginController;
    private static ApplicationController staticApplicationController;

    private final LoginController loginController;
    private ApplicationController applicationController;
    private Stage primaryStage;

    // FXML fields - Login
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;

    // FXML fields - Register
    @FXML
    private TextField registerUsernameField;
    @FXML
    private PasswordField registerPasswordField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Label registerMessageLabel;
    @FXML
    private Button submitRegisterButton;
    @FXML
    private Button cancelRegisterButton;

    public GraphicLoginView() {
        // Constructor vuoto per JavaFX - recupera i controller dalle variabili statiche
        this.loginController = staticLoginController;
        this.applicationController = staticApplicationController;
    }

    public GraphicLoginView(LoginController loginController) {
        this.loginController = loginController;
    }

    public static void setStaticLoginController(LoginController loginController) {
        staticLoginController = loginController;
    }

    public static void setStaticApplicationController(ApplicationController appController) {
        staticApplicationController = appController;
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
        // Note: static fields are set only when needed by JavaFX launch
    }

    @Override
    public void display() {
        // Controlla se JavaFX è già inizializzato
        try {
            // Se Platform è già inizializzato, possiamo usare runLater direttamente
            Platform.runLater(() -> {
                try {
                    Stage stage = new Stage();
                    this.primaryStage = stage;
                    start(stage);
                } catch (Exception e) {
                    logger.severe("Failed to start login view: " + e.getMessage());
                }
            });
        } catch (IllegalStateException e) {
            // Se JavaFX non è inizializzato, dobbiamo usare launch()
            setStaticLoginController(this.loginController);
            setStaticApplicationController(this.applicationController);
            new Thread(() -> Application.launch(GraphicLoginView.class)).start();
        }
    }

    @Override
    public void close() {
        if (primaryStage != null) {
            Platform.runLater(() -> primaryStage.close());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerDark().getUserAgentStylesheet());
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Sporty - Login");

        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            Scene scene = new Scene(root, 450, 550);
            ViewUtils.applyStylesheets(scene);

            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            showError("Failed to load login view: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        UserBean userBean = getUserCredentials();
        try {
            UserBean loggedInUser = loginController.login(userBean);
            if (loggedInUser != null) {
                displayLoginSuccess(loggedInUser.getUsername());
                // Navigate to home
                Platform.runLater(() -> {
                    primaryStage.close();
                    try {
                        applicationController.navigateToHome(loggedInUser);
                    } catch (exception.ValidationException e) {
                        displayLoginError(e.getMessage());
                    }
                });
            } else {
                displayLoginError(Constants.ERROR_INVALID_CREDENTIALS);
            }
        } catch (exception.ValidationException e) {
            displayLoginError(e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            // Load FXML per la registrazione
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            // Inizializza il ComboBox delle role dopo il caricamento
            if (roleComboBox != null && roleComboBox.getItems().isEmpty()) {
                roleComboBox.getItems().addAll(
                        Constants.ROLE_PLAYER,
                        Constants.ROLE_ORGANIZER,
                        Constants.ROLE_FIELD_MANAGER);
                roleComboBox.setValue(Constants.ROLE_PLAYER);
            }

            Scene scene = new Scene(root, 450, 650);
            ViewUtils.applyStylesheets(scene);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Sporty - Register");
        } catch (IOException e) {
            showError("Failed to load register view: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegisterSubmit() {
        registerMessageLabel.setText("");
        registerMessageLabel.setVisible(false);
        registerMessageLabel.getStyleClass().removeAll(Constants.CSS_ERROR, Constants.CSS_SUCCESS);

        try {
            String username = registerUsernameField.getText().trim();
            String password = registerPasswordField.getText();
            String name = nameField.getText().trim();
            String surname = surnameField.getText().trim();
            String selectedRole = roleComboBox.getValue();

            // Validate inputs through controller
            String validationError = loginController.validateRegistrationInputs(
                    username, password, name, surname, selectedRole);
            if (validationError != null) {
                showRegisterError(validationError);
                return;
            }

            // Get role code from controller (no longer static)
            int roleCode = loginController.getRoleCodeFromString(selectedRole);

            UserBean userBean = new UserBean(username, password);

            loginController.register(userBean, name, surname, roleCode);

            showRegisterSuccess(Constants.SUCCESS_REGISTRATION);

            // Torna al login dopo 1.5 secondi
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(this::handleCancelRegister);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (exception.ValidationException e) {
            showRegisterError(e.getMessage());
        }
    }

    @FXML
    private void handleCancelRegister() {
        try {
            // Ricarica la view del login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            Scene scene = new Scene(root, 450, 550);
            ViewUtils.applyStylesheets(scene);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Sporty - Login");
        } catch (IOException e) {
            showError("Failed to load login view: " + e.getMessage());
        }
    }

    @Override
    public UserBean getUserCredentials() {
        UserBean userBean = new UserBean();
        userBean.setUsername(usernameField.getText());
        userBean.setPassword(passwordField.getText());
        return userBean;
    }

    @Override
    public void displayLoginSuccess(String username) {
        messageLabel.setVisible(true);
        messageLabel.getStyleClass().removeAll(Constants.CSS_ERROR);
        messageLabel.getStyleClass().add(Constants.CSS_SUCCESS);
        messageLabel.setText("Welcome, " + username + "!");
    }

    @Override
    public void displayLoginError(String message) {
        messageLabel.setVisible(true);
        messageLabel.getStyleClass().removeAll(Constants.CSS_SUCCESS);
        messageLabel.getStyleClass().add(Constants.CSS_ERROR);
        messageLabel.setText(message);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showRegisterError(String message) {
        registerMessageLabel.setVisible(true);
        registerMessageLabel.getStyleClass().removeAll(Constants.CSS_SUCCESS);
        registerMessageLabel.getStyleClass().add(Constants.CSS_ERROR);
        registerMessageLabel.setText(message);
    }

    private void showRegisterSuccess(String message) {
        registerMessageLabel.setVisible(true);
        registerMessageLabel.getStyleClass().removeAll(Constants.CSS_ERROR);
        registerMessageLabel.getStyleClass().add(Constants.CSS_SUCCESS);
        registerMessageLabel.setText(message);
    }
}
