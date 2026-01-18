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
    private MatchBean matchBean;
    private model.bean.FieldBean fieldBean;
    private boolean bookingMode;

    public PaymentController(ApplicationController applicationController) {
        this.applicationController = applicationController;
        try {
            this.matchDAO = model.dao.DAOFactory.getMatchDAO(applicationController.getPersistenceType());
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_DAO_INIT + e.getMessage(), e);
        }
    }

    public void setMatchBean(MatchBean matchBean) {
        this.matchBean = matchBean;
        this.bookingMode = false;
    }

    public void setBookingMode(model.bean.FieldBean fieldBean, MatchBean contextBean) {
        this.fieldBean = fieldBean;
        this.matchBean = contextBean;
        this.bookingMode = true;
    }

    public boolean isBookingMode() {
        return bookingMode;
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
            if (bookingMode) {
                return processBookingPayment();
            } else {
                return processMatchPayment();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, bookingMode ? "Error confirming booking" : Constants.ERROR_MATCH_CONFIRM, e);
            return false;
        }
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

        applicationController.navigateToRecap(matchBean);
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

        // Torna alla home: Payment -> BookField -> Home
        applicationController.back(); // chiude Payment
        applicationController.back(); // chiude BookField, torna a Home
        return true;
    }

    public void back() {
        applicationController.back();
    }
}
