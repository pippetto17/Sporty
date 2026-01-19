package view.joinmatchview;
import controller.ApplicationController;
import controller.JoinMatchController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.domain.Match;
import view.ViewUtils;
public class GraphicJoinMatchView implements JoinMatchView {
    private final JoinMatchController controller;
    private Stage stage;
    private ApplicationController applicationController;
    public GraphicJoinMatchView(JoinMatchController controller) {
        this.controller = controller;
    }
    @Override
    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }
    @Override
    public void display() {
        Platform.runLater(this::initStage);
    }
    private void initStage() {
        stage = new Stage();
        stage.setTitle("Sporty - Join Match");
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        Label titleLabel = new Label("Join Match");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        root.getChildren().add(titleLabel);
        displayMatchDetails(root);
        if (controller.canJoin()) {
            addJoinButton(root);
        } else {
            Label errorLabel = new Label("Cannot join this match");
            errorLabel.setStyle("-fx-text-fill: red;");
            root.getChildren().add(errorLabel);
        }
        addBackButton(root);
        Scene scene = new Scene(root, 500, 450);
        ViewUtils.applyStylesheets(scene);
        stage.setScene(scene);
        stage.show();
    }
    @Override
    public void displayMatchDetails() {
        /*TO DO*/
    }
    private void displayMatchDetails(VBox root) {
        Match match = controller.getMatch();
        VBox detailsBox = new VBox(8);
        detailsBox.setPadding(new Insets(15));
        detailsBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");
        addDetailRow(detailsBox, "Sport:", match.getSport().getDisplayName());
        addDetailRow(detailsBox, "Date:", match.getMatchDate().toString());
        addDetailRow(detailsBox, "Time:", match.getMatchTime().toString());
        addDetailRow(detailsBox, "City:", match.getCity());
        addDetailRow(detailsBox, "Organizer:", match.getOrganizerUsername());
        addDetailRow(detailsBox, "Participants:", 
            match.getParticipantCount() + "/" + match.getRequiredParticipants());
        addDetailRow(detailsBox, "Available slots:", 
            String.valueOf(controller.getAvailableSlots()));
        addDetailRow(detailsBox, "Cost per person:", 
            String.format("€%.2f", controller.calculatePlayerCost()));
        root.getChildren().add(detailsBox);
    }
    private void addDetailRow(VBox container, String label, String value) {
        Label rowLabel = new Label(label + " " + value);
        rowLabel.setStyle("-fx-font-size: 14px;");
        container.getChildren().add(rowLabel);
    }
    private void addJoinButton(VBox root) {
        Button joinButton = new Button("Join and Pay");
        joinButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 30;");
        joinButton.setOnAction(e -> handleJoin());
        root.getChildren().add(joinButton);
    }
    private void addBackButton(VBox root) {
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 30;");
        backButton.setOnAction(e -> handleBack());
        root.getChildren().add(backButton);
    }
    private void handleJoin() {
        try {
            controller.proceedToPayment();
        } catch (Exception e) {
            displayError(e.getMessage());
        }
    }
    private void handleBack() {
        applicationController.back();
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
            Platform.runLater(() -> stage.close());
        }
    }
}
