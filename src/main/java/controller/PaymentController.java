package controller;

import exception.DataAccessException;
import exception.ValidationException;
import model.bean.MatchBean;
import model.bean.PaymentBean;
import model.utils.Constants;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller per la gestione del pagamento.
 * Orchestra il flusso di pagamento tra la view e il DAO layer.
 */
public class PaymentController {
    private static final Logger logger = Logger.getLogger(PaymentController.class.getName());
    private final ApplicationController applicationController;
    private final model.dao.MatchDAO matchDAO;
    private final model.notification.NotificationService notificationService;
    private MatchBean matchBean;
    private model.bean.FieldBean fieldBean;
    private boolean bookingMode;
    private boolean joinMode;
    private model.domain.User joiningUser;

    public PaymentController(ApplicationController applicationController) {
        this.applicationController = applicationController;
        try {
            this.matchDAO = model.dao.DAOFactory.getMatchDAO(applicationController.getPersistenceType());
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_DAO_INIT + e.getMessage(), e);
        }
        this.notificationService = new model.notification.NotificationService();
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

    public boolean isBookingMode() {
        return bookingMode;
    }

    public boolean isJoinMode() {
        return joinMode;
    }

    public model.bean.FieldBean getFieldBean() {
        return fieldBean;
    }

    public MatchBean getMatchBean() {
        return matchBean;
    }

    public boolean processPayment(PaymentBean paymentBean) throws ValidationException {
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
        if (joinMode) return "Error joining match";
        if (bookingMode) return "Error confirming booking";
        return Constants.ERROR_MATCH_CONFIRM;
    }

    private boolean processMatchPayment() throws ValidationException {
        if (matchBean == null)
            throw new ValidationException("MatchBean cannot be null");

        matchBean.setStatus(model.domain.MatchStatus.CONFIRMED);
        model.domain.Match match = model.converter.MatchConverter.toEntity(matchBean);
        matchDAO.save(match);

        if (match.getMatchId() != null) {
            matchBean.setMatchId(match.getMatchId());
        }

        // Notifica Field Manager per creazione match
        try {
            if (match.getFieldId() != null) {
                model.domain.Field field = model.dao.DAOFactory.getFieldDAO(applicationController.getPersistenceType())
                        .findById(match.getFieldId());
                if (field != null && field.getManagerId() != null) {
                    notificationService.notifyMatchCreated(
                            field.getManagerId(),
                            match.getOrganizerUsername(),
                            field.getName(),
                            match.getMatchDate().toString(),
                            match.getMatchTime().toString(),
                            match.getSport().name());
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not send match notification: " + e.getMessage());
        }

        // Back to OrganizeMatchView e mostra recap
        applicationController.back();
        applicationController.back();
        applicationController.back();

        view.organizematchview.OrganizeMatchView organizeView =
            (view.organizematchview.OrganizeMatchView) applicationController.getCurrentView();
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
        booking.setEndTime(matchBean.getMatchTime().plusHours(2));
        booking.setType(model.domain.BookingType.PRIVATE);
        booking.setTotalPrice(fieldBean.getPricePerHour() * 2);
        booking.setStatus(model.domain.BookingStatus.CONFIRMED);

        bookingDAO.save(booking);

        // Notifica Field Manager per nuova prenotazione
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
            logger.log(Level.WARNING, "Could not send notification: " + e.getMessage());
        }

        // Torna alla home: Payment -> BookField -> Home
        applicationController.back(); // chiude Payment
        applicationController.back(); // chiude BookField, torna a Home
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

        // Aggiorna matchBean con nuova lista partecipanti
        matchBean.setParticipants(match.getParticipants());

        // Torna alla home: Payment -> JoinMatch -> Home
        applicationController.back(); // chiude Payment
        applicationController.back(); // chiude JoinMatch, torna a Home
        return true;
    }

    public void back() {
        applicationController.back();
    }
}
