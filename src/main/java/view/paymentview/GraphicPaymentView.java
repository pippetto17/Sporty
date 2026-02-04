package view.paymentview;

import controller.ApplicationController;
import controller.PaymentController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.bean.FieldBean;
import model.bean.MatchBean;
import model.bean.PaymentBean;
import view.ViewUtils;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class GraphicPaymentView implements PaymentView {
    private static final Logger logger = Logger.getLogger(GraphicPaymentView.class.getName());
    private Stage stage;
    private PaymentBean collectedPaymentData;
    private int maxAvailableShares;
    private PaymentController controller;
    private ApplicationController applicationController;
    private double currentPricePerHour;
    private int currentTotalPlayers;
    @FXML
    private Label matchInfoLabel;
    @FXML
    private Label amountLabel;
    @FXML
    private ComboBox<Integer> sharesComboBox;
    @FXML
    private TextField cardNumberField;
    @FXML
    private TextField expiryField;
    @FXML
    private TextField cvvField;
    @FXML
    private TextField cardHolderField;
    @FXML
    private Button payButton;
    @FXML
    private Label errorLabel;

    public GraphicPaymentView() {
        // Intentionally empty
    }

    public void setController(PaymentController controller) {
        this.controller = controller;
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @Override
    public void display() {
        Platform.runLater(() -> {
            try {
                if (stage == null) {
                    stage = new Stage();
                    stage.setTitle("Sporty - Payment");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/payment.fxml"));
                    loader.setController(this);
                    Parent root = loader.load();
                    Scene scene = new Scene(root, 600, 550);
                    ViewUtils.applyStylesheets(scene);
                    stage.setScene(scene);
                }
                stage.show();
                stage.toFront();
            } catch (IOException e) {
                logger.severe("Failed to load payment view: " + e.getMessage());
            }
        });
    }

    @Override
    public void displayMatchInfo(MatchBean match, int availableShares) {
        Platform.runLater(() -> {
            matchInfoLabel.setText(String.format("Payment for: %s - %s @ %s",
                    match.getSport().getDisplayName(), match.getCity(), match.getMatchTime()));
            maxAvailableShares = availableShares;
            currentPricePerHour = match.getPricePerHour();
            currentTotalPlayers = match.getSport().getRequiredPlayers();
            sharesComboBox.setItems(FXCollections.observableArrayList(
                    IntStream.rangeClosed(1, availableShares).boxed().toList()));
            sharesComboBox.setValue(1);
            sharesComboBox.setOnAction(e -> updateAmountLabel());
            updateAmountLabel();
        });
    }

    private void updateAmountLabel() {
        int shares = sharesComboBox.getValue() != null ? sharesComboBox.getValue() : 1;
        double costPerPerson = model.service.PricingService.calculateCostPerPerson(currentPricePerHour,
                currentTotalPlayers);
        double totalAmount = model.service.PricingService.calculateTotalToPay(shares, currentPricePerHour,
                currentTotalPlayers);
        amountLabel.setText(String.format("Cost per person: €%.2f | Total (%d shares): €%.2f",
                costPerPerson, shares, totalAmount));
    }

    @Override
    public void displayBookingInfo(FieldBean field, MatchBean context) {
        Platform.runLater(() -> {
            String addressInfo = (field.getAddress() != null && !field.getAddress().isEmpty())
                    ? " - " + field.getAddress()
                    : "";
            matchInfoLabel.setText(String.format("Booking: %s%s on %s @ %s",
                    field.getName(), addressInfo, context.getMatchDate(), context.getMatchTime()));
            sharesComboBox.setVisible(false);
            double costPerPerson = field.getPricePerPerson();
            amountLabel.setText(String.format("Field: €%.2f/hour | Per person: €%.2f",
                    field.getPricePerHour(), costPerPerson));
            maxAvailableShares = 0;
        });
    }

    @Override
    public void showSuccess(String message) {
        Platform.runLater(() -> {
            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText(message);
        });
    }

    @Override
    public PaymentBean collectPaymentData(int maxShares) {
        if (cardNumberField.getText().trim().isEmpty() ||
                expiryField.getText().trim().isEmpty() ||
                cvvField.getText().trim().isEmpty() ||
                cardHolderField.getText().trim().isEmpty()) {
            return null;
        }
        PaymentBean paymentBean = new PaymentBean();
        paymentBean.setCardNumber(cardNumberField.getText().trim());
        paymentBean.setExpiryDate(expiryField.getText().trim());
        paymentBean.setCvv(cvvField.getText().trim());
        paymentBean.setCardHolder(cardHolderField.getText().trim());
        paymentBean.setSharesToPay(sharesComboBox.getValue() != null ? sharesComboBox.getValue() : 1);
        return paymentBean;
    }

    @FXML
    private void handlePay() {
        errorLabel.setText("");
        errorLabel.setStyle("");
        PaymentBean data = collectPaymentData(maxAvailableShares);
        if (data == null) {
            showError("Please fill all payment fields.");
            return;
        }
        if (controller != null) {
            controller.processPaymentFromView(data);
        } else {
            collectedPaymentData = data;
            stage.close();
        }
    }

    @FXML
    private void handleBack() {
        if (applicationController != null) {
            applicationController.back();
        }
        if (stage != null) {
            stage.close();
        }
    }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText(message);
        });
    }

    @Override
    public void close() {
        if (stage != null) {
            Platform.runLater(stage::close);
        }
    }

    public PaymentBean getCollectedPaymentData() {
        return collectedPaymentData;
    }
}