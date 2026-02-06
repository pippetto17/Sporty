package model.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MapsAPI {

    private MapsAPI() {
        // Utility class
    }

    public static final List<String> ITALIAN_CITIES = Collections.unmodifiableList(Arrays.asList(
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
        if (prefix == null || prefix.trim().isEmpty()) {
            return ITALIAN_CITIES;
        }
        String lower = prefix.toLowerCase();
        return ITALIAN_CITIES.stream()
                .filter(city -> city.toLowerCase().startsWith(lower))
                .toList();
    }

    public static boolean isValidCity(String city) {
        if (city == null) {
            return false;
        }
        return ITALIAN_CITIES.stream().anyMatch(c -> c.equalsIgnoreCase(city.trim()));
    }
}
