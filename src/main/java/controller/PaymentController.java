package controller;

import exception.ValidationException;
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
        this.matchDAO = model.dao.DAOFactory.getMatchDAO(applicationController.getPersistenceType());
        this.notificationService = new model.notification.NotificationService();
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

    public void setBookingMode(model.bean.FieldBean fieldBean, MatchBean contextBean) {
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

        // For CLI views, collect data synchronously. For GUI views, wait for button callback
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
            view.showError("Dati di pagamento non validi");
            return;
        }

        double amount = calculateAmount(paymentData.getSharesToPay());
        paymentData.setAmount(amount);

        try {
            boolean success = processPayment(paymentData);
            if (success) {
                handlePaymentSuccess();
            } else {
                view.showError("Pagamento rifiutato. Verifica i dati.");
            }
        } catch (ValidationException e) {
            view.showError(e.getMessage());
        }
    }

    private void handlePaymentSuccess() {
        view.showSuccess("Pagamento completato con successo!");
        if (view instanceof view.paymentview.GraphicPaymentView) {
            handleGraphicViewSuccess();
        } else {
            handleCliViewSuccess();
        }
    }

    private void handleGraphicViewSuccess() {
        new Thread(() -> {
            try {
                Thread.sleep(1500);
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
        int totalRequired = matchBean.getRequiredParticipants();
        int currentParticipants = matchBean.getParticipants() != null ? matchBean.getParticipants().size() : 0;
        return Math.max(1, totalRequired - currentParticipants);
    }

    private double calculateAmount(int shares) {
        if (bookingMode && fieldBean != null) {
            return fieldBean.getPricePerHour() * 2;
        }
        return matchBean.getPricePerPerson() * shares;
    }

    private boolean processPayment(PaymentBean paymentBean) throws ValidationException {
        if (paymentBean == null || paymentBean.getCardNumber() == null || paymentBean.getCardNumber().isEmpty())
            return false;

        try {
            if (joinMode) {
                return processJoinPayment();
            } else if (bookingMode) {
                return processBookingPayment();
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
        if (bookingMode)
            return "Error confirming booking";
        return Constants.ERROR_MATCH_CONFIRM;
    }

    private boolean processMatchPayment() throws ValidationException {
        if (matchBean == null)
            throw new ValidationException("MatchBean cannot be null");

        model.domain.Field field = null;
        if (matchBean.getFieldId() != null) {
            field = model.dao.DAOFactory.getFieldDAO(applicationController.getPersistenceType())
                    .findById(matchBean.getFieldId());
        }

        if (field != null) {
            model.dao.BookingDAO bookingDAO = model.dao.DAOFactory.getBookingDAO(
                    applicationController.getPersistenceType());

            model.domain.Booking booking = new model.domain.Booking();
            booking.setFieldId(field.getFieldId());
            booking.setRequesterUsername(matchBean.getOrganizerUsername());
            booking.setBookingDate(matchBean.getMatchDate());
            booking.setStartTime(matchBean.getMatchTime());
            booking.setEndTime(matchBean.getMatchTime().plusMinutes(matchBean.getSport().getDuration()));
            booking.setType(model.domain.BookingType.MATCH);
            booking.setTotalPrice(field.getPricePerHour() * 2);

            if (field.getAutoApprove() != null && field.getAutoApprove()) {
                booking.setStatus(model.domain.BookingStatus.CONFIRMED);
            } else {
                booking.setStatus(model.domain.BookingStatus.PENDING);
            }

            bookingDAO.save(booking);

            if (booking.getBookingId() != null) {
                matchBean.setBookingId(booking.getBookingId());
            }
        }

        matchBean.setStatus(model.domain.MatchStatus.CONFIRMED);
        model.domain.Match match = model.converter.MatchConverter.toEntity(matchBean);
        matchDAO.save(match);

        if (match.getMatchId() != null) {
            matchBean.setMatchId(match.getMatchId());
        }

        try {
            if (field != null && field.getManagerId() != null) {
                notificationService.notifyMatchCreated(
                        field.getManagerId(),
                        match.getOrganizerUsername(),
                        field.getName(),
                        match.getMatchDate().toString(),
                        match.getMatchTime().toString(),
                        match.getSport().name());
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

    private boolean processBookingPayment() throws ValidationException {
        if (fieldBean == null || matchBean == null)
            throw new ValidationException("Field and context required for booking");

        model.dao.BookingDAO bookingDAO = model.dao.DAOFactory.getBookingDAO(
                applicationController.getPersistenceType());

        model.domain.Booking booking = new model.domain.Booking();
        booking.setFieldId(fieldBean.getFieldId());
        booking.setRequesterUsername(matchBean.getOrganizerUsername());
        booking.setBookingDate(matchBean.getMatchDate());
        booking.setStartTime(matchBean.getMatchTime());
        int duration = matchBean.getSport() != null ? matchBean.getSport().getDuration() : 120;
        booking.setEndTime(matchBean.getMatchTime().plusMinutes(duration));
        booking.setType(model.domain.BookingType.PRIVATE);
        booking.setTotalPrice(fieldBean.getPricePerHour() * 2);
        booking.setStatus(model.domain.BookingStatus.CONFIRMED);

        bookingDAO.save(booking);

        try {
            model.domain.Field field = model.dao.DAOFactory.getFieldDAO(applicationController.getPersistenceType())
                    .findById(booking.getFieldId());
            if (field != null && field.getManagerId() != null) {
                notificationService.notifyBookingCreated(
                        field.getManagerId(),
                        booking.getRequesterUsername(),
                        field.getName(),
                        booking.getBookingDate().toString(),
                        booking.getStartTime().toString());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not send notification: {0}", e.getMessage());
        }

        applicationController.back();
        applicationController.back();
        return true;
    }

    private boolean processJoinPayment() throws ValidationException {
        if (matchBean == null || joiningUser == null)
            throw new ValidationException("Match and user required for join");

        model.domain.Match match = matchDAO.findById(matchBean.getMatchId());
        if (match == null)
            throw new ValidationException(Constants.ERROR_MATCH_NOT_FOUND);

        if (!match.addParticipant(joiningUser.getUsername())) {
            throw new ValidationException("Cannot join match");
        }

        matchDAO.save(match);

        matchBean.setParticipants(match.getParticipants());

        applicationController.back();
        applicationController.back();
        return true;
    }
}
