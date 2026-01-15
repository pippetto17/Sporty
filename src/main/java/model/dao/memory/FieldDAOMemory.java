package model.dao.memory;

import model.dao.FieldDAO;
import model.domain.Field;
import model.domain.Sport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldDAOMemory implements FieldDAO {
    private static final Map<String, Field> fields = new HashMap<>();

    static {
        // Initialize with some demo data
        initializeDemoFields();
    }

    private static void createAndAddField(String id, String name, Sport sport, String address, String city,
            double price, boolean indoor) {
        Field field = new Field(id, name, sport, address, city);
        field.setPricePerHour(price);
        field.setIndoor(indoor);
        field.setLatitude(45.4642); // Default mock coordinates
        field.setLongitude(9.1900);
        field.setAvailability("available");
        fields.put(field.getFieldId(), field);
    }

    private static void initializeDemoFields() {
        // Football 5 fields - Zona Centro
        createAndAddField("F001", "City Sports Center", Sport.FOOTBALL_5, "Via Roma 123", "Milano", 50.0, false);
        createAndAddField("F002", "Arena Calcetto Centrale", Sport.FOOTBALL_5, "Corso Buenos Aires 88", "Milano", 55.0,
                true);
        createAndAddField("F003", "San Siro Five", Sport.FOOTBALL_5, "Via dei Missaglia 151", "Milano", 45.0, false);

        // Football 8 fields
        createAndAddField("F004", "Campo 8 Navigli", Sport.FOOTBALL_8, "Via Gola 20", "Milano", 70.0, false);

        // Football 11 fields
        createAndAddField("F005", "Green Stadium", Sport.FOOTBALL_11, "Via Dante 45", "Milano", 120.0, false);
        createAndAddField("F006", "Stadio Bicocca", Sport.FOOTBALL_11, "Viale Sarca 202", "Milano", 110.0, false);

        // Basketball fields
        createAndAddField("B001", "Indoor Basketball Arena", Sport.BASKETBALL, "Corso Italia 78", "Milano", 40.0, true);
        createAndAddField("B002", "Basket City Loreto", Sport.BASKETBALL, "Piazzale Loreto 5", "Milano", 35.0, true);
        createAndAddField("B003", "PlayBasket Porta Romana", Sport.BASKETBALL, "Via Orti 10", "Milano", 38.0, false);

        // Tennis fields
        createAndAddField("T001", "Tennis Club Milano", Sport.TENNIS_SINGLE, "Via Sempione 200", "Milano", 25.0, false);
        createAndAddField("T002", "Tennis Forlanini", Sport.TENNIS_SINGLE, "Via Corelli 136", "Milano", 22.0, false);
        createAndAddField("T003", "Tennis Double Garibaldi", Sport.TENNIS_DOUBLE, "Via Farini 70", "Milano", 30.0,
                true);

        // Padel fields
        createAndAddField("P001", "Padel Center", Sport.PADEL_DOUBLE, "Via Tortona 56", "Milano", 40.0, true);
        createAndAddField("P002", "Padel Arena Citylife", Sport.PADEL_DOUBLE, "Via Stanislao Cannizzaro 2", "Milano",
                45.0, true);
        createAndAddField("P003", "Padel Club Lambrate", Sport.PADEL_SINGLE, "Via Conte Rosso 12", "Milano", 28.0,
                true);

        // Campi più lontani dal centro per testare distanze
        createAndAddField("F007", "Campo Nord Milano", Sport.FOOTBALL_5, "Via Fulvio Testi 123", "Milano", 45.0, false);
        createAndAddField("B004", "Basket Sud Milano", Sport.BASKETBALL, "Via Ripamonti 300", "Milano", 35.0, false);
        createAndAddField("T004", "Tennis Ovest", Sport.TENNIS_SINGLE, "Via Novara 350", "Milano", 20.0, false);
        createAndAddField("F008", "Campo Est Milano", Sport.FOOTBALL_8, "Via Mecenate 76", "Milano", 65.0, false);

        // Campo economico lontano
        Field field20 = new Field("F009", "Budget Field Barona", Sport.FOOTBALL_5, "Via Lorenteggio 255", "Milano");
        field20.setPricePerHour(30.0);
        field20.setIndoor(false);
        field20.setLatitude(45.4408);
        field20.setLongitude(9.1402);
        field20.setAvailability("available");
        fields.put(field20.getFieldId(), field20);
    }

    @Override
    public List<Field> findAll() {
        return new ArrayList<>(fields.values());
    }

    @Override
    public Field findById(String fieldId) {
        return fields.get(fieldId);
    }

    @Override
    public List<Field> findByCity(String city) {
        return fields.values().stream()
                .filter(field -> field.isInCity(city))
                .toList();
    }

    @Override
    public List<Field> findBySport(Sport sport) {
        return fields.values().stream()
                .filter(field -> field.getSport() == sport)
                .toList();
    }

    @Override
    public List<Field> findAvailableFields(Sport sport, String city, LocalDate date, LocalTime time) {
        return fields.values().stream()
                .filter(field -> field.getSport() == sport)
                .filter(field -> field.isInCity(city))
                .filter(Field::isAvailable)
                .sorted((f1, f2) -> Double.compare(f1.getPricePerHour(), f2.getPricePerHour()))
                .toList();
    }

    @Override
    public void save(Field field) {
        fields.put(field.getFieldId(), field);
    }

    @Override
    public void delete(String fieldId) {
        fields.remove(fieldId);
    }

    public static void clearAll() {
        fields.clear();
        initializeDemoFields();
    }
}
