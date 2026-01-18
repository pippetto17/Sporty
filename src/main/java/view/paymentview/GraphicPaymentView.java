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
import model.bean.MatchBean;
import model.bean.PaymentBean;
import view.ViewUtils;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Implementazione grafica della view di pagamento utilizzando JavaFX.
 * Gestisce l'input dei dati di pagamento e l'interazione con l'utente.
 */
public class GraphicPaymentView implements PaymentView {
    private static final Logger logger = Logger.getLogger(GraphicPaymentView.class.getName());

    private final PaymentController paymentController;
    private Stage stage;

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

    public GraphicPaymentView(PaymentController paymentController) {
        this.paymentController = paymentController;
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
                stage.setTitle("Sporty - Payment");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/payment.fxml"));
                loader.setController(this);
                Parent root = loader.load();

                Scene scene = new Scene(root, 600, 500);
                ViewUtils.applyStylesheets(scene);

                stage.setScene(scene);
                stage.show();

                initialize();
            } catch (IOException e) {
                logger.severe("Failed to load payment view: " + e.getMessage());
            }
        });
    }

    @FXML
    public void initialize() {
        if (paymentController.isBookingMode()) {
            initializeBookingMode();
        } else {
            initializeMatchMode();
        }
    }

    private void initializeMatchMode() {
        MatchBean match = paymentController.getMatchBean();
        if (match != null) {
            matchInfoLabel.setText(String.format("Payment for: %s - %s @ %s",
                    match.getSport(), match.getCity(), match.getMatchTime()));

            Double price = match.getPricePerPerson();
            if (price != null) {
                Integer[] shares = new Integer[match.getRequiredParticipants()];
                for (int i = 0; i < shares.length; i++) {
                    shares[i] = i + 1;
                }
                sharesComboBox.setItems(FXCollections.observableArrayList(shares));
                sharesComboBox.setValue(1);
                sharesComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) updateAmount();
                });
                updateAmount();
            }
        }
    }

    private void initializeBookingMode() {
        model.bean.FieldBean field = paymentController.getFieldBean();
        MatchBean context = paymentController.getMatchBean();

        if (field != null && context != null) {
            matchInfoLabel.setText(String.format("Booking: %s - %s on %s @ %s",
                    field.getName(), field.getCity(),
                    context.getMatchDate(), context.getMatchTime()));

            sharesComboBox.setVisible(false);
            double totalPrice = field.getPricePerHour() * 2;
            amountLabel.setText(String.format("Total Amount: €%.2f (2h slot)", totalPrice));
        }
    }

    private void updateAmount() {
        Double price = paymentController.getMatchBean().getPricePerPerson();
        Integer shares = sharesComboBox.getValue();
        if (price != null && shares != null) {
            double total = price * shares;
            amountLabel.setText(String.format("Total: € %.2f", total));
        }
    }

    @FXML
    private void handlePay() {
        errorLabel.setText("");

        PaymentBean paymentBean = new PaymentBean();
        paymentBean.setCardNumber(cardNumberField.getText());
        paymentBean.setExpiryDate(expiryField.getText());
        paymentBean.setCvv(cvvField.getText());
        paymentBean.setCardHolder(cardHolderField.getText());
        paymentBean.setSharesToPay(sharesComboBox.getValue());

        // Calculate amount
        Double price = paymentController.getMatchBean().getPricePerPerson();
        if (price != null) {
            paymentBean.setAmount(price * sharesComboBox.getValue());
        }

        payButton.setDisable(true);
        payButton.setText("Processing...");

        // Run in background to avoid freezing UI if service sleeps
        new Thread(() -> {
            try {
                boolean success = paymentController.processPayment(paymentBean);

                Platform.runLater(() -> {
                    payButton.setDisable(false);
                    payButton.setText("Pay Now");

                    if (!success) {
                        showError("Payment declined. Please check details.");
                    }
                });
            } catch (exception.ValidationException e) {
                Platform.runLater(() -> {
                    payButton.setDisable(false);
                    payButton.setText("Pay Now");
                    showError(e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleBack() {
        stage.close();
        paymentController.back();
    }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> errorLabel.setText(message));
    }

    @Override
    public void close() {
        if (stage != null) {
            Platform.runLater(() -> stage.close());
        }
    }
}
