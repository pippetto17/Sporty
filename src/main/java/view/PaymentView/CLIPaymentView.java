package view.PaymentView;

import controller.ApplicationController;
import controller.PaymentController;

/**
 * Implementazione CLI della view di pagamento.
 * Attualmente non completamente implementata - placeholder per future
 * estensioni.
 */
public class CLIPaymentView implements PaymentView {
    private final PaymentController paymentController;

    public CLIPaymentView(PaymentController paymentController) {
        this.paymentController = paymentController;
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        // Non utilizzato in questa view
    }

    @Override
    public void showError(String message) {
        System.out.println("[ERRORE] " + message);
    }

    @Override
    public void display() {
        System.out.println("=== PAGAMENTO (CLI NON IMPLEMENTATA) ===");
        System.out.println("La funzionalità di pagamento è disponibile solo nell'interfaccia grafica.");
        paymentController.back();
    }

    @Override
    public void close() {
        // No-op per CLI
    }
}
