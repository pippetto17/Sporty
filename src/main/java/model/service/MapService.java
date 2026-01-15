package model.service;

import model.domain.Field;

import java.util.List;

/**
 * Service per la gestione delle mappe e calcolo distanze
 */
public class MapService {

    // Coordinate di default (Milano centro)
    private static final double DEFAULT_LAT = 45.4642;
    private static final double DEFAULT_LON = 9.1900;

    private MapService() {
        // Private constructor to prevent instantiation
    }

    /**
     * Calcola la distanza tra due punti geografici usando la formula di Haversine
     * @return distanza in kilometri
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Raggio della Terra in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * Calcola la distanza di un campo da una posizione
     */
    public static double calculateDistanceFromField(Field field, double userLat, double userLon) {
        if (!field.hasCoordinates()) {
            return Double.MAX_VALUE; // Se il campo non ha coordinate, mettilo alla fine
        }
        return calculateDistance(userLat, userLon, field.getLatitude(), field.getLongitude());
    }

    /**
     * Ordina i campi per distanza da una posizione
     */
    public static List<Field> sortByDistance(List<Field> fields, double userLat, double userLon) {
        return fields.stream()
                .filter(Field::hasCoordinates)
                .sorted((f1, f2) -> {
                    double dist1 = calculateDistanceFromField(f1, userLat, userLon);
                    double dist2 = calculateDistanceFromField(f2, userLat, userLon);
                    return Double.compare(dist1, dist2);
                })
                .toList();
    }

    /**
     * Genera URL per OpenStreetMap (alternativa gratuita a Google Maps)
     */
    public static String generateMapUrl(double lat, double lon, int zoom) {
        return String.format("https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f#map=%d/%.6f/%.6f",
                lat, lon, zoom, lat, lon);
    }

    /**
     * Genera HTML semplice con mappa statica (MOLTO PIÙ AFFIDABILE)
     * Usa OpenStreetMap Static Map API - nessun JavaScript, nessun rendering bug
     */
    public static String generateMapHtml(List<Field> fields) {
        // Calcola centro e bounds
        double avgLat = fields.stream()
                .filter(Field::hasCoordinates)
                .mapToDouble(Field::getLatitude)
                .average()
                .orElse(DEFAULT_LAT);
        double avgLon = fields.stream()
                .filter(Field::hasCoordinates)
                .mapToDouble(Field::getLongitude)
                .average()
                .orElse(DEFAULT_LON);

        // Crea HTML semplice con lista campi e link a OpenStreetMap
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<meta charset='utf-8'/>");
        html.append("<style>");
        html.append("body{font-family:Arial,sans-serif;margin:20px;background:#f5f5f5;}");
        html.append("h2{color:#28a745;margin-bottom:20px;}");
        html.append(".map-link{display:block;background:#28a745;color:white;padding:15px;");
        html.append("text-align:center;text-decoration:none;border-radius:8px;margin-bottom:20px;");
        html.append("font-size:16px;font-weight:bold;}");
        html.append(".map-link:hover{background:#218838;}");
        html.append(".field-list{background:white;border-radius:8px;padding:15px;box-shadow:0 2px 4px rgba(0,0,0,0.1);}");
        html.append(".field-item{padding:12px;border-bottom:1px solid #eee;display:flex;justify-content:space-between;align-items:center;}");
        html.append(".field-item:last-child{border-bottom:none;}");
        html.append(".field-name{font-weight:bold;color:#333;}");
        html.append(".field-info{color:#666;font-size:14px;}");
        html.append(".field-price{color:#28a745;font-weight:bold;}");
        html.append(".view-btn{background:#007bff;color:white;padding:8px 15px;border:none;");
        html.append("border-radius:4px;cursor:pointer;text-decoration:none;font-size:14px;}");
        html.append(".view-btn:hover{background:#0056b3;}");
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h2>📍 Campi Disponibili - Milano</h2>");

        // Link per aprire mappa completa in OpenStreetMap
        String osmUrl = String.format(java.util.Locale.US,
                "https://www.openstreetmap.org/#map=13/%.6f/%.6f", avgLat, avgLon);
        html.append(String.format("<a href='%s' class='map-link' target='_blank'>", escapeHtml(osmUrl)));
        html.append("🗺️ APRI MAPPA COMPLETA IN OPENSTREETMAP</a>");

        html.append("<div class='field-list'>");
        html.append(String.format("<p style='color:#666;margin-top:0;'>Trovati %d campi</p>", fields.size()));

        // Lista campi
        for (Field field : fields) {
            if (field.hasCoordinates()) {
                html.append("<div class='field-item'>");
                html.append("<div>");
                html.append(String.format("<div class='field-name'>%s</div>", escapeHtml(field.getName())));
                html.append(String.format("<div class='field-info'>%s - %s</div>",
                        escapeHtml(field.getSport().getDisplayName()),
                        escapeHtml(field.getAddress())));
                html.append("</div>");
                html.append("<div style='text-align:right;'>");
                html.append(String.format("<div class='field-price'>€%.2f/h</div>",
                        field.getPricePerHour()));

                // Link diretto al campo su OpenStreetMap
                String fieldOsmUrl = String.format(java.util.Locale.US,
                        "https://www.openstreetmap.org/#map=17/%.6f/%.6f",
                        field.getLatitude(), field.getLongitude());
                html.append(String.format("<a href='%s' class='view-btn' target='_blank'>Visualizza</a>",
                        escapeHtml(fieldOsmUrl)));
                html.append("</div>");
                html.append("</div>");
            }
        }

        html.append("</div>");
        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Genera HTML semplice per singolo campo
     */
    public static String generateSingleFieldMapHtml(Field field) {
        if (!field.hasCoordinates()) {
            return "<html><body style='font-family:Arial;padding:20px;'>" +
                   "<h3>⚠️ Coordinate non disponibili</h3></body></html>";
        }

        String osmUrl = String.format(java.util.Locale.US,
                "https://www.openstreetmap.org/#map=17/%.6f/%.6f",
                field.getLatitude(), field.getLongitude());

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<meta charset='utf-8'/>");
        html.append("<style>");
        html.append("body{font-family:Arial,sans-serif;margin:0;padding:20px;background:#f5f5f5;}");
        html.append(".container{background:white;border-radius:8px;padding:20px;max-width:600px;margin:0 auto;box-shadow:0 2px 8px rgba(0,0,0,0.1);}");
        html.append("h2{color:#28a745;margin-top:0;}");
        html.append(".info-row{margin:15px 0;padding:10px;background:#f8f9fa;border-radius:4px;}");
        html.append(".label{font-weight:bold;color:#666;font-size:14px;}");
        html.append(".value{color:#333;font-size:16px;margin-top:5px;}");
        html.append(".map-btn{display:block;background:#28a745;color:white;padding:15px;");
        html.append("text-align:center;text-decoration:none;border-radius:8px;margin-top:20px;");
        html.append("font-size:16px;font-weight:bold;}");
        html.append(".map-btn:hover{background:#218838;}");
        html.append("</style>");
        html.append("</head><body>");
        html.append("<div class='container'>");

        html.append(String.format("<h2>📍 %s</h2>", escapeHtml(field.getName())));

        html.append("<div class='info-row'>");
        html.append("<div class='label'>Sport</div>");
        html.append(String.format("<div class='value'>%s</div>", escapeHtml(field.getSport().getDisplayName())));
        html.append("</div>");

        html.append("<div class='info-row'>");
        html.append("<div class='label'>Indirizzo</div>");
        html.append(String.format("<div class='value'>%s, %s</div>",
                escapeHtml(field.getAddress()), escapeHtml(field.getCity())));
        html.append("</div>");

        html.append("<div class='info-row'>");
        html.append("<div class='label'>Prezzo</div>");
        html.append(String.format("<div class='value' style='color:#28a745;font-size:20px;'>€%.2f/ora</div>",
                field.getPricePerHour()));
        html.append("</div>");

        html.append("<div class='info-row'>");
        html.append("<div class='label'>Coordinate GPS</div>");
        html.append(String.format(java.util.Locale.US, "<div class='value'>%.6f, %.6f</div>",
                field.getLatitude(), field.getLongitude()));
        html.append("</div>");

        html.append(String.format("<a href='%s' class='map-btn' target='_blank'>", escapeHtml(osmUrl)));
        html.append("🗺️ APRI SU OPENSTREETMAP</a>");

        html.append("</div>");
        html.append("</body></html>");
        return html.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    public static double getDefaultLat() {
        return DEFAULT_LAT;
    }

    public static double getDefaultLon() {
        return DEFAULT_LON;
    }
}

