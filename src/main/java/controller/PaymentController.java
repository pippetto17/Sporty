package controller;

import exception.ValidationException;
import model.bean.FieldBean;
import model.bean.MatchBean;
import model.bean.PaymentBean;
import model.dao.MatchDAO;
import model.notification.NotificationService;
import model.utils.Constants;
import view.paymentview.PaymentView;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentController {
    private static final Logger logger = Logger.getLogger(PaymentController.class.getName());
    private final ApplicationController applicationController;
    private final MatchDAO matchDAO;
    private final NotificationService notificationService;
    private final MatchBean matchBean;
    private final FieldBean fieldBean;
    private final boolean bookingMode;
    private final boolean joinMode;
    private final model.domain.User joiningUser;
    private PaymentView view;

    /**
     * Constructor for standard match payment mode.
     */
    public PaymentController(ApplicationController applicationController, MatchBean matchBean) {
        this.applicationController = applicationController;
        this.matchDAO = applicationController.getDaoFactory().getMatchDAO();
        this.notificationService = applicationController.getNotificationService();
        this.matchBean = matchBean;
        this.fieldBean = null;
        this.bookingMode = false;
        this.joinMode = false;
        this.joiningUser = null;
    }

    /**
     * Constructor for field booking mode.
     */
    public PaymentController(ApplicationController applicationController, FieldBean fieldBean, MatchBean contextBean) {
        this.applicationController = applicationController;
        this.matchDAO = applicationController.getDaoFactory().getMatchDAO();
        this.notificationService = applicationController.getNotificationService();
        this.matchBean = contextBean;
        this.fieldBean = fieldBean;
        this.bookingMode = true;
        this.joinMode = false;
        this.joiningUser = null;
    }

    /**
     * Constructor for join match mode.
     */
    public PaymentController(ApplicationController applicationController, MatchBean matchBean, model.domain.User user) {
        this.applicationController = applicationController;
        this.matchDAO = applicationController.getDaoFactory().getMatchDAO();
        this.notificationService = applicationController.getNotificationService();
        this.matchBean = matchBean;
        this.fieldBean = null;
        this.bookingMode = false;
        this.joinMode = true;
        this.joiningUser = user;
    }

    public void setView(PaymentView view) {
        this.view = view;
        if (view instanceof view.paymentview.GraphicPaymentView graphicView) {
            graphicView.setController(this);
        }
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
        double totalAmount = calculateTotalToPay(shares, pricePerHour, totalPlayers);
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
        if (joinMode) {
            return matchBean.getMissingPlayers();
        }
        return getTotalPlayers();
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

        // Validate and save match
        try {
            model.domain.Match match = model.converter.MatchConverter.toEntity(matchBean);
            match.setStatus(model.domain.MatchStatus.PENDING);
            // Ensure ID is handled if it's new (should be 0)
            matchDAO.save(match);
            matchBean.setMatchId(match.getId()); // Update bean with assigned ID
        } catch (Exception e) {
            throw new ValidationException("Could not save match: " + e.getMessage());
        }

        // Send booking request notification to field manager
        if (field != null && field.getManager() != null) {
            try {
                notificationService.notifyBookingRequest(
                        field.getManager().getUsername(),
                        matchBean.getOrganizerName(),
                        field.getName(),
                        matchBean.getMatchDate().toString(),
                        matchBean.getMatchTime().toString(),
                        matchBean.getSport().getDisplayName());
                logger.info("Booking request notification sent to field manager: " + field.getManager().getUsername());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not send booking request notification: {0}", e.getMessage());
            }
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

        // addJoinedPlayer already checks if match is full and if user already joined
        match.addJoinedPlayer(joiningUser.getId());

        // Update the match with new joined_players and missing_players
        matchDAO.update(match);

        // Update the bean as well
        matchBean.setMissingPlayers(match.getMissingPlayers());
        matchBean.setJoinedPlayers(match.getJoinedPlayers());

        // Only one back() needed for join flow: PaymentView -> HomeView
        applicationController.back();
        return true;
    }

    public static double calculateCostPerPerson(double pricePerHour, int totalPlayers) {
        if (totalPlayers <= 0) {
            return 0.0;
        }
        return pricePerHour / totalPlayers;
    }

    public static double calculateTotalToPay(int numberOfShares, double pricePerHour, int totalPlayers) {
        return calculateCostPerPerson(pricePerHour, totalPlayers) * numberOfShares;
    }
}