package view.paymentview;

import controller.ApplicationController;
import view.View;

/**
 * Interfaccia per la view del pagamento.
 * Segue lo stesso pattern delle altre view utilizzando setApplicationController
 * per mantenere consistenza nell'architettura.
 */
public interface PaymentView extends View {
    void setApplicationController(ApplicationController applicationController);

    void showError(String message);
}
