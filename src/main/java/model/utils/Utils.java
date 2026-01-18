package model.utils;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public final class Utils {

    private Utils() {}

    // Time utilities
    public static boolean timeRangesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) return false;
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    // Price calculation
    public static double calculatePricePerPerson(double pricePerHour, double hoursBooked, int participants) {
        if (pricePerHour < 0 || hoursBooked <= 0 || participants <= 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        return (pricePerHour * hoursBooked) / participants;
    }

    // Italian cities
    public static final List<String> ITALIAN_CITIES = java.util.Collections.unmodifiableList(Arrays.asList(
            "Agrigento", "Alessandria", "Ancona", "Aosta", "Arezzo", "Ascoli Piceno", "Asti",
            "Avellino", "Bari", "Barletta-Andria-Trani", "Belluno", "Benevento", "Bergamo",
            "Biella", "Bologna", "Bolzano", "Brescia", "Brindisi", "Cagliari", "Caltanissetta",
            "Campobasso", "Caserta", "Catania", "Catanzaro", "Chieti", "Como", "Cosenza",
            "Cremona", "Crotone", "Cuneo", "Enna", "Fermo", "Ferrara", "Firenze", "Foggia",
            "Forlì-Cesena", "Frosinone", "Genova", "Gorizia", "Grosseto", "Imperia", "Isernia",
            "L'Aquila", "La Spezia", "Latina", "Lecce", "Lecco", "Livorno", "Lodi", "Lucca",
            "Macerata", "Mantova", "Massa-Carrara", "Matera", "Messina", "Milano", "Modena",
            "Monza e Brianza", "Napoli", "Novara", "Nuoro", "Oristano", "Padova", "Palermo",
            "Parma", "Pavia", "Perugia", "Pesaro e Urbino", "Pescara", "Piacenza", "Pisa",
            "Pistoia", "Pordenone", "Potenza", "Prato", "Ragusa", "Ravenna", "Reggio Calabria",
            "Reggio Emilia", "Rieti", "Rimini", "Roma", "Rovigo", "Salerno", "Sassari",
            "Savona", "Siena", "Siracusa", "Sondrio", "Sud Sardegna", "Taranto", "Teramo",
            "Terni", "Torino", "Trapani", "Trento", "Treviso", "Trieste", "Udine", "Varese",
            "Venezia", "Verbano-Cusio-Ossola", "Vercelli", "Verona", "Vibo Valentia", "Vicenza", "Viterbo"));

    public static List<String> searchCitiesByPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) return ITALIAN_CITIES;
        String lower = prefix.toLowerCase();
        return ITALIAN_CITIES.stream().filter(c -> c.toLowerCase().startsWith(lower)).toList();
    }

    public static boolean isValidCity(String city) {
        if (city == null) return false;
        return ITALIAN_CITIES.stream().anyMatch(c -> c.equalsIgnoreCase(city.trim()));
    }
}

