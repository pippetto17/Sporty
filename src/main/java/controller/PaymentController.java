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
    }

    public MatchBean getMatchBean() {
        return matchBean;
    }

    public boolean processPayment(PaymentBean paymentBean) throws ValidationException {
        // Simple mock payment logic (KISS)
        boolean success = paymentBean != null &&
                paymentBean.getCardNumber() != null &&
                !paymentBean.getCardNumber().isEmpty();

        if (success) {
            try {
                if (matchBean == null)
                    throw new ValidationException("MatchBean cannot be null");
                matchBean.setStatus(model.domain.MatchStatus.CONFIRMED);

                model.domain.Match match = model.converter.MatchConverter.toEntity(matchBean);
                matchDAO.save(match);

                if (match.getMatchId() != null) {
                    matchBean.setMatchId(match.getMatchId());
                }

                applicationController.navigateToRecap(matchBean);
            } catch (Exception e) {
                logger.log(Level.SEVERE, Constants.ERROR_MATCH_CONFIRM, e);
                return false;
            }
        }
        return success;
    }

    public void back() {
        applicationController.back();
    }
}
