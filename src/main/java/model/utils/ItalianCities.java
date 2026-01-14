package model.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class containing lists of Italian cities
 */
public class ItalianCities {

    private ItalianCities() {
        // Private constructor to prevent instantiation
    }

    /**
     * Lista completa delle città italiane principali
     * Ordinata alfabeticamente per facilitare l'autocomplete
     */
    public static final List<String> CITIES = Arrays.asList(
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
        "Venezia", "Verbano-Cusio-Ossola", "Vercelli", "Verona", "Vibo Valentia", "Vicenza", "Viterbo"
    );

    /**
     * Cerca città che iniziano con il prefisso dato
     */
    public static List<String> searchByPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return CITIES;
        }

        String lowerPrefix = prefix.toLowerCase();
        return CITIES.stream()
                .filter(city -> city.toLowerCase().startsWith(lowerPrefix))
                .toList();
    }

    /**
     * Verifica se una città è valida
     */
    public static boolean isValidCity(String city) {
        return city != null && CITIES.contains(city);
    }
}

