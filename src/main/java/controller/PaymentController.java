package controller;

import exception.ValidationException;
import model.bean.FieldBean;
import model.bean.MatchBean;
import model.bean.PaymentBean;
import model.utils.Constants;
import view.paymentview.PaymentView;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentController {
    private static final Logger logger = Logger.getLogger(PaymentController.class.getName());
    private final ApplicationController applicationController;
    private final model.dao.MatchDAO matchDAO;
    private final model.notification.NotificationService notificationService;
    private PaymentView view;
    private MatchBean matchBean;
    private model.bean.FieldBean fieldBean;
    private boolean bookingMode;
    private boolean joinMode;
    private model.domain.User joiningUser;

    public PaymentController(ApplicationController applicationController) {
        this.applicationController = applicationController;
        this.matchDAO = applicationController.getDaoFactory().getMatchDAO();
        this.notificationService = applicationController.getNotificationService();
    }

    public void setView(PaymentView view) {
        this.view = view;
        if (view instanceof view.paymentview.GraphicPaymentView graphicView) {
            graphicView.setController(this);
        }
    }

    public void setMatchBean(MatchBean matchBean) {
        this.matchBean = matchBean;
        this.bookingMode = false;
        this.joinMode = false;
    }

    public void setBookingMode(FieldBean fieldBean, MatchBean contextBean) {
        this.fieldBean = fieldBean;
        this.matchBean = contextBean;
        this.bookingMode = true;
        this.joinMode = false;
    }

    public void setJoinMode(MatchBean matchBean, model.domain.User user) {
        this.matchBean = matchBean;
        this.joiningUser = user;
        this.joinMode = true;
        this.bookingMode = false;
    }

    public void start() {
        view.display();
        if (bookingMode) {
            view.displayBookingInfo(fieldBean, matchBean);
        } else {
            int availableShares = calculateAvailableShares();
            view.displayMatchInfo(matchBean, availableShares);
        }
        if (!(view instanceof view.paymentview.GraphicPaymentView)) {
            PaymentBean paymentData = view.collectPaymentData(bookingMode ? 0 : calculateAvailableShares());
            if (paymentData == null) {
                applicationController.back();
                return;
            }
            processPaymentFromView(paymentData);
        }
    }

    public void processPaymentFromView(PaymentBean paymentData) {
        if (paymentData == null) {
            view.showError(model.utils.Constants.ERROR_PAYMENT_INVALID);
            return;
        }
        double pricePerHour = getPricePerHour();
        int totalPlayers = getTotalPlayers();
        int shares = paymentData.getSharesToPay() > 0 ? paymentData.getSharesToPay() : 1;
        double totalAmount = model.service.PricingService.calculateTotalToPay(shares, pricePerHour, totalPlayers);
        paymentData.setAmount(totalAmount);
        try {
            boolean success = processPayment(paymentData);
            if (success) {
                handlePaymentSuccess();
            } else {
                view.showError(model.utils.Constants.ERROR_PAYMENT_REJECTED);
            }
        } catch (ValidationException e) {
            view.showError(e.getMessage());
        }
    }

    private double getPricePerHour() {
        if (bookingMode && fieldBean != null) {
            return fieldBean.getPricePerHour();
        }
        if (matchBean != null) {
            return matchBean.getPricePerHour();
        }
        return 0.0;
    }

    private int getTotalPlayers() {
        if (bookingMode && fieldBean != null && fieldBean.getSport() != null) {
            return fieldBean.getSport().getRequiredPlayers();
        }
        if (matchBean != null && matchBean.getSport() != null) {
            return matchBean.getSport().getRequiredPlayers();
        }
        return 1;
    }

    private void handlePaymentSuccess() {
        view.showSuccess(model.utils.Constants.SUCCESS_PAYMENT);
        if (view instanceof view.paymentview.GraphicPaymentView) {
            handleGraphicViewSuccess();
        } else {
            handleCliViewSuccess();
        }
    }

    private void handleGraphicViewSuccess() {
        new Thread(() -> {
            try {
                Thread.sleep(model.utils.Constants.PAYMENT_SUCCESS_DELAY_MS);
                javafx.application.Platform.runLater(this::closeAndNavigateBack);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void closeAndNavigateBack() {
        view.close();
        navigateBackIfNeeded();
    }

    private void handleCliViewSuccess() {
        navigateBackIfNeeded();
    }

    private void navigateBackIfNeeded() {
        if (joinMode || bookingMode) {
            applicationController.back();
        }
    }

    private int calculateAvailableShares() {
        if (matchBean == null)
            return 1;
        return matchBean.getMissingPlayers();
    }

    private boolean processPayment(PaymentBean paymentBean) throws ValidationException {
        if (paymentBean == null || paymentBean.getCardNumber() == null || paymentBean.getCardNumber().isEmpty())
            return false;
        try {
            if (joinMode) {
                return processJoinPayment();
            } else if (bookingMode) {
                return true;
            } else {
                return processMatchPayment();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, getErrorMessage(), e);
            return false;
        }
    }

    private String getErrorMessage() {
        if (joinMode)
            return "Error joining match";
        return Constants.ERROR_MATCH_CONFIRM;
    }

    private boolean processMatchPayment() throws ValidationException {
        if (matchBean == null)
            throw new ValidationException("MatchBean cannot be null");
        model.domain.Field field = null;
        if (matchBean.getFieldId() != 0) {
            field = applicationController.getDaoFactory().getFieldDAO().findById(matchBean.getFieldId());
        }
        try {
            if (field != null) {
                notificationService.notifyMatchCreated(
                        String.valueOf(field.getManagerId()),
                        matchBean.getOrganizerName(),
                        field.getName(),
                        matchBean.getMatchDate().toString(),
                        matchBean.getMatchTime().toString(),
                        matchBean.getSport().name());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not send match notification: {0}", e.getMessage());
        }
        applicationController.back();
        applicationController.back();
        view.organizematchview.OrganizeMatchView organizeView = (view.organizematchview.OrganizeMatchView) applicationController
                .getCurrentView();
        if (organizeView != null) {
            organizeView.displayRecap(matchBean);
        }
        return true;
    }

    private boolean processJoinPayment() throws ValidationException {
        if (matchBean == null || joiningUser == null)
            throw new ValidationException("Match and user required for join");
        model.domain.Match match = matchDAO.findById(matchBean.getMatchId());
        if (match == null)
            throw new ValidationException(Constants.ERROR_MATCH_NOT_FOUND);
        if (match.getMissingPlayers() <= 0) {
            throw new ValidationException("Match is full");
        }
        match.setMissingPlayers(match.getMissingPlayers() - 1);
        matchDAO.save(match);
        matchBean.setMissingPlayers(match.getMissingPlayers());
        applicationController.back();
        applicationController.back();
        return true;
    }
}