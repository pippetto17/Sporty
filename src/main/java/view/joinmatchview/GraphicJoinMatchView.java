package view.joinmatchview;

import controller.ApplicationController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import view.ViewUtils;

public class GraphicJoinMatchView implements JoinMatchView {
    private Stage stage;
    private ApplicationController applicationController;
    private String userChoice;

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @Override
    public void display() {
        Platform.runLater(() -> {
            stage = new Stage();
            stage.setTitle("Sporty - Join Match");

            VBox root = new VBox(15);
            root.setPadding(new Insets(20));
            root.setAlignment(Pos.TOP_CENTER);

            Label titleLabel = new Label("Join Match");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            root.getChildren().add(titleLabel);

            Scene scene = new Scene(root, 500, 450);
            ViewUtils.applyStylesheets(scene);
            stage.setScene(scene);
            stage.show();
        });
    }

    @Override
    public void displayMatchInfo(String sport, String date, String time, String city,
                                String organizer, int[] participants, double cost) {
        Platform.runLater(() -> {
            VBox root = (VBox) stage.getScene().getRoot();

            VBox detailsBox = new VBox(8);
            detailsBox.setPadding(new Insets(15));
            detailsBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

            addDetailRow(detailsBox, "Sport:", sport);
            addDetailRow(detailsBox, "Date:", date);
            addDetailRow(detailsBox, "Time:", time);
            addDetailRow(detailsBox, "City:", city);
            addDetailRow(detailsBox, "Organizer:", organizer);
            addDetailRow(detailsBox, "Participants:", participants[0] + "/" + participants[1]);
            addDetailRow(detailsBox, "Available slots:", String.valueOf(participants[2]));
            addDetailRow(detailsBox, "Cost per person:", String.format("€%.2f", cost));

            root.getChildren().add(detailsBox);

            Button joinButton = new Button("Join and Pay");
            joinButton.setStyle(
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 30;");
            joinButton.setOnAction(e -> {
                userChoice = "1";
                stage.close();
            });

            Button backButton = new Button("Back");
            backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 30;");
            backButton.setOnAction(e -> {
                userChoice = "2";
                stage.close();
            });

            root.getChildren().addAll(joinButton, backButton);
        });
    }

    @Override
    public void displayCannotJoin(String reason) {
        Platform.runLater(() -> {
            VBox root = (VBox) stage.getScene().getRoot();

            Label errorLabel = new Label(reason);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            root.getChildren().add(errorLabel);

            Button backButton = new Button("Back");
            backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 30;");
            backButton.setOnAction(e -> {
                stage.close();
                applicationController.back();
            });

            root.getChildren().add(backButton);
        });
    }

    @Override
    public String getUserChoice() {
        return userChoice;
    }

    private void addDetailRow(VBox container, String label, String value) {
        Label rowLabel = new Label(label + " " + value);
        rowLabel.setStyle("-fx-font-size: 14px;");
        container.getChildren().add(rowLabel);
    }

    @Override
    public void displayError(String message) {
        Platform.runLater(() -> ViewUtils.showError("Error", message));
    }

    @Override
    public void displaySuccess(String message) {
        Platform.runLater(() -> ViewUtils.showInfo("Success", message));
    }

    @Override
    public void close() {
        if (stage != null) {
            Platform.runLater(stage::close);
        }
    }
}
