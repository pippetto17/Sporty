package model.service;

public class PricingService {
    private PricingService() {
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

    public static String formatPrice(double amount) {
        return String.format("€%.2f", amount);
    }
}