package view.paymentview;

import controller.ApplicationController;
import model.bean.FieldBean;
import model.bean.MatchBean;
import model.bean.PaymentBean;
import java.util.Scanner;

public class CLIPaymentView implements PaymentView {
    private final Scanner scanner;

    public CLIPaymentView() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
    }

    @Override
    public void showError(String message) {
        System.out.println("[ERRORE] " + message);
    }

    @Override
    public void display() {
        System.out.println("\n=== PAGAMENTO ===");
    }

    @Override
    public void displayMatchInfo(MatchBean match, int availableShares) {
        System.out.printf("Pagamento per: %s - %s @ %s%n",
                match.getSport().getDisplayName(), match.getCity(), match.getMatchTime());
        double costPerPerson = match.getCostPerPerson();
        System.out.printf("💰 Quota a persona: €%.2f%n", costPerPerson);
        System.out.printf("📊 Giocatori totali: %d | Mancanti: %d%n",
                match.getSport().getRequiredPlayers(), match.getMissingPlayers());
        System.out.printf("Quote disponibili: 1-%d%n", availableShares);
    }

    @Override
    public void displayBookingInfo(FieldBean field, MatchBean context) {
        System.out.printf("Prenotazione: %s - %s il %s @ %s%n",
                field.getName(), field.getCity(), context.getMatchDate(), context.getMatchTime());
        if (field.getAddress() != null && !field.getAddress().isEmpty()) {
            System.out.printf("📍 Indirizzo: %s%n", field.getAddress());
        }
        System.out.printf("💰 Prezzo totale campo: €%.2f/ora%n", field.getPricePerHour());
        System.out.printf("👤 Prezzo a persona: €%.2f%n", field.getPricePerPerson());
    }

    @Override
    public void showSuccess(String message) {
        System.out.println("✓ " + message);
    }

    @Override
    public PaymentBean collectPaymentData(int maxShares) {
        PaymentBean paymentBean = new PaymentBean();
        if (maxShares > 0) {
            System.out.print("Numero di quote da acquistare (1-" + maxShares + "): ");
            try {
                int shares = Integer.parseInt(scanner.nextLine().trim());
                if (shares < 1 || shares > maxShares) {
                    return null;
                }
                paymentBean.setSharesToPay(shares);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            paymentBean.setSharesToPay(1);
        }
        System.out.print("Numero carta: ");
        String cardNumber = scanner.nextLine().trim();
        System.out.print("Scadenza (MM/YY): ");
        String expiry = scanner.nextLine().trim();
        System.out.print("CVV: ");
        String cvv = scanner.nextLine().trim();
        System.out.print("Intestatario: ");
        String cardHolder = scanner.nextLine().trim();
        if (cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty() || cardHolder.isEmpty()) {
            return null;
        }
        paymentBean.setCardNumber(cardNumber);
        paymentBean.setExpiryDate(expiry);
        paymentBean.setCvv(cvv);
        paymentBean.setCardHolder(cardHolder);
        return paymentBean;
    }

    @Override
    public void close() {
    }
}