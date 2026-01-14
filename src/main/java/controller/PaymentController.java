package controller;

import model.bean.MatchBean;
import model.bean.PaymentBean;
import model.service.PaymentService;
import model.service.MatchService;

import java.sql.SQLException;

/**
 * Controller per la gestione del pagamento.
 * Orchestrare il flusso di pagamento tra la view e i service,
 * delegando la logica di business ai service appropriati.
 */
public class PaymentController {
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
            throw new RuntimeException("Errore nell'inizializzazione di MatchService: " + e.getMessage(), e);
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
                e.printStackTrace();
                return false;
            }
        }

        return success;
    }

    public void back() {
        applicationController.back();
    }
}
