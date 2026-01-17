package model.utils;

/**
 * Utility class for price calculation operations.
 * Centralizes pricing logic to avoid code duplication.
 */
public final class PriceCalculationUtils {

    private PriceCalculationUtils() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Calculate price per person based on hourly rate, duration, and number of
     * participants.
     * 
     * @param pricePerHour         Hourly rate for the field
     * @param hoursBooked          Number of hours booked
     * @param numberOfParticipants Number of participants sharing the cost
     * @return Price per person
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static double calculatePricePerPerson(double pricePerHour, double hoursBooked,
            int numberOfParticipants) {
        if (pricePerHour < 0) {
            throw new IllegalArgumentException("Price per hour cannot be negative");
        }
        if (hoursBooked <= 0) {
            throw new IllegalArgumentException("Hours booked must be positive");
        }
        if (numberOfParticipants <= 0) {
            throw new IllegalArgumentException("Number of participants must be positive");
        }

        return (pricePerHour * hoursBooked) / numberOfParticipants;
    }

    /**
     * Calculate total price based on hourly rate and duration.
     * 
     * @param pricePerHour Hourly rate
     * @param hoursBooked  Number of hours booked
     * @return Total price
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static double calculateTotalPrice(double pricePerHour, double hoursBooked) {
        if (pricePerHour < 0) {
            throw new IllegalArgumentException("Price per hour cannot be negative");
        }
        if (hoursBooked <= 0) {
            throw new IllegalArgumentException("Hours booked must be positive");
        }

        return pricePerHour * hoursBooked;
    }

    /**
     * Format a price value as a currency string.
     * 
     * @param price Price value
     * @return Formatted price string (e.g., "€12.50")
     */
    public static String formatPrice(double price) {
        return String.format("€%.2f", price);
    }
}
