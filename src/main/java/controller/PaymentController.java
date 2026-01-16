package controller;

import model.bean.MatchBean;
import model.bean.PaymentBean;
import model.service.PaymentService;
import model.service.MatchService;
import model.utils.Constants;
import exception.ServiceInitializationException;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller per la gestione del pagamento.
 * Orchestrare il flusso di pagamento tra la view e i service,
 * delegando la logica di business ai service appropriati.
 */
public class PaymentController {
    private static final Logger logger = Logger.getLogger(PaymentController.class.getName());
    private final ApplicationController applicationController;
    private final PaymentService paymentService;
    private final MatchService matchService;
    private MatchBean matchBean;

    public PaymentController(ApplicationController applicationController) {
        this.applicationController = applicationController;
        this.paymentService = new PaymentService();
        try {
            this.matchService = new MatchService(applicationController.getPersistenceType());
        } catch (SQLException e) {
            throw new ServiceInitializationException(Constants.ERROR_MATCH_SERVICE_INIT + e.getMessage(),
                    e);
        }
    }

    public void setMatchBean(MatchBean matchBean) {
        this.matchBean = matchBean;
    }

    public MatchBean getMatchBean() {
        return matchBean;
    }

    /**
     * Processa il pagamento e, se ha successo, conferma il match.
     * Utilizza PaymentService per il pagamento e MatchService per il salvataggio.
     */
    public boolean processPayment(PaymentBean paymentBean) {
        boolean success = paymentService.processPayment(paymentBean);

        if (success) {
            try {
                matchService.confirmMatch(matchBean);
                applicationController.navigateToRecap(matchBean);
            } catch (Exception e) {
                logger.log(Level.SEVERE, Constants.ERROR_MATCH_CONFIRM, e);
            }
        }

        return success;
    }

    public void back() {
        applicationController.back();
    }
}
