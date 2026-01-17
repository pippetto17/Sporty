package view.recapview;

import controller.ApplicationController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.bean.MatchBean;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class GraphicRecapView implements RecapView {
    private static final Logger logger = Logger.getLogger(GraphicRecapView.class.getName());
    private ApplicationController applicationController;
    private MatchBean matchBean;
    private Stage stage;

    @FXML
    private Label sportLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Button inviteButton;

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @Override
    public void setMatchBean(MatchBean matchBean) {
        this.matchBean = matchBean;
    }

    @Override
    public void display() {
        Platform.runLater(() -> {
            try {
                stage = new Stage();
                stage.setTitle("Sporty - Match Recap");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/recap.fxml"));
                loader.setController(this);
                Parent root = loader.load();

                Scene scene = new Scene(root, 600, 500);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/css/controls-dark.css").toExternalForm());

                stage.setScene(scene);
                stage.show();

                initialize();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Errore caricamento view", e);
            }
        });
    }

    @FXML
    public void initialize() {
        if (matchBean != null) {
            sportLabel.setText("Sport: " + (matchBean.getSport() != null ? matchBean.getSport().toString() : "N/A"));
            dateLabel.setText(
                    "Date: " + (matchBean.getMatchDate() != null ? matchBean.getMatchDate().toString() : "N/A"));
            timeLabel.setText(
                    "Time: " + (matchBean.getMatchTime() != null ? matchBean.getMatchTime().toString() : "N/A"));
            cityLabel.setText("Location: " + matchBean.getCity());
            statusLabel.setText(
                    "Status: " + (matchBean.getStatus() != null ? matchBean.getStatus().toString() : "CONFIRMED"));
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

    /**
     * Gestisce il click sul pulsante Home.
     * Torna alla home navigando indietro nello stack delle view.
     */
    @FXML
    private void handleHome() {
        stage.close();

        // Naviga indietro 4 volte per tornare alla Home
        // Stack: Home -> OrganizeMatch -> BookField -> Payment -> Recap
        if (applicationController != null) {
            Platform.runLater(() -> {
                try {
                    for (int i = 0; i < 4; i++) {
                        applicationController.back();
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Errore tornando alla home", e);
                }
            });
        }
    }

    @Override
    public void close() {
        if (stage != null) {
            Platform.runLater(() -> stage.close());
        }
    }
}
