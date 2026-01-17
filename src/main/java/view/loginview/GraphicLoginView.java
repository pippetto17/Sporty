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
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.bean.UserBean;
import model.domain.User;
import model.utils.Constants;

import java.io.IOException;
import java.util.logging.Logger;

public class GraphicLoginView extends Application implements LoginView {
    private static final Logger logger = Logger.getLogger(GraphicLoginView.class.getName());

    // Static fields per passare i controller all'istanza creata da launch()
    private static LoginController staticLoginController;
    private static ApplicationController staticApplicationController;

    private LoginController loginController;
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
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Sporty - Login");

        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            // Load CSS
            Scene scene = new Scene(root, 450, 550);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/controls-dark.css").toExternalForm());

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
            User user = loginController.login(userBean);
            if (user != null) {
                displayLoginSuccess(user.getUsername());
                // Navigate to home
                Platform.runLater(() -> {
                    primaryStage.close();
                    applicationController.navigateToHome(user);
                });
            } else {
                displayLoginError(Constants.ERROR_INVALID_CREDENTIALS);
            }
        } catch (IllegalArgumentException e) {
            displayLoginError(e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            Stage registerStage = new Stage();
            registerStage.initModality(Modality.APPLICATION_MODAL);
            registerStage.initOwner(primaryStage);
            registerStage.setTitle("Sporty - Register");

            // Create GraphicRegisterView
            GraphicRegisterView registerController = new GraphicRegisterView(loginController, registerStage);

            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            loader.setController(registerController);
            Parent root = loader.load();

            // Load CSS
            Scene scene = new Scene(root, 450, 650);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/controls-dark.css").toExternalForm());

            registerStage.setScene(scene);
            registerStage.setResizable(false);
            registerStage.showAndWait();

        } catch (IOException e) {
            showError("Failed to load register view: " + e.getMessage());
        }
    }

    @Override
    public UserBean getUserCredentials() {
        model.domain.User tempUser = new model.domain.User();
        tempUser.setUsername(usernameField.getText());
        tempUser.setPassword(passwordField.getText());
        return model.converter.UserConverter.toUserBean(tempUser);
    }

    @Override
    public void displayLoginSuccess(String username) {
        messageLabel.getStyleClass().removeAll("error");
        messageLabel.getStyleClass().add("success");
        messageLabel.setText("Welcome, " + username + "!");
    }

    @Override
    public void displayLoginError(String message) {
        messageLabel.getStyleClass().removeAll("success");
        messageLabel.getStyleClass().add("error");
        messageLabel.setText(message);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
