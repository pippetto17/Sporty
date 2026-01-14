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
import java.util.stream.Collectors;

public class FieldDAOMemory implements FieldDAO {
    private static final Map<String, Field> fields = new HashMap<>();

    static {
        // Initialize with some demo data
        initializeDemoFields();
    }

    private static void initializeDemoFields() {
        // Football 5 fields - Zona Centro
        Field field1 = new Field("F001", "City Sports Center", Sport.FOOTBALL_5, "Via Roma 123", "Milano");
        field1.setPricePerHour(50.0);
        field1.setIndoor(false);
        field1.setLatitude(45.4642); // Duomo area
        field1.setLongitude(9.1900);
        field1.setAvailability("available");
        fields.put(field1.getFieldId(), field1);

        Field field2 = new Field("F002", "Arena Calcetto Centrale", Sport.FOOTBALL_5, "Corso Buenos Aires 88", "Milano");
        // ...existing code...

        Field field3 = new Field("F003", "San Siro Five", Sport.FOOTBALL_5, "Via dei Missaglia 151", "Milano");
        // ...existing code...

        // Football 8 fields
        Field field4 = new Field("F004", "Campo 8 Navigli", Sport.FOOTBALL_8, "Via Gola 20", "Milano");
        // ...existing code...

        // Football 11 fields
        Field field5 = new Field("F005", "Green Stadium", Sport.FOOTBALL_11, "Via Dante 45", "Milano");
        // ...existing code...

        Field field6 = new Field("F006", "Stadio Bicocca", Sport.FOOTBALL_11, "Viale Sarca 202", "Milano");
        // ...existing code...

        // Basketball fields
        Field field7 = new Field("B001", "Indoor Basketball Arena", Sport.BASKETBALL, "Corso Italia 78", "Milano");
        // ...existing code...

        Field field8 = new Field("B002", "Basket City Loreto", Sport.BASKETBALL, "Piazzale Loreto 5", "Milano");
        // ...existing code...

        Field field9 = new Field("B003", "PlayBasket Porta Romana", Sport.BASKETBALL, "Via Orti 10", "Milano");
        // ...existing code...

        // Tennis fields
        Field field10 = new Field("T001", "Tennis Club Milano", Sport.TENNIS_SINGLE, "Via Sempione 200", "Milano");
        // ...existing code...

        Field field11 = new Field("T002", "Tennis Forlanini", Sport.TENNIS_SINGLE, "Via Corelli 136", "Milano");
        // ...existing code...

        Field field12 = new Field("T003", "Tennis Double Garibaldi", Sport.TENNIS_DOUBLE, "Via Farini 70", "Milano");
        // ...existing code...

        // Padel fields
        Field field13 = new Field("P001", "Padel Center", Sport.PADEL_DOUBLE, "Via Tortona 56", "Milano");
        // ...existing code...

        Field field14 = new Field("P002", "Padel Arena Citylife", Sport.PADEL_DOUBLE, "Via Stanislao Cannizzaro 2", "Milano");
        // ...existing code...

        Field field15 = new Field("P003", "Padel Club Lambrate", Sport.PADEL_SINGLE, "Via Conte Rosso 12", "Milano");
        // ...existing code...

        // Campi più lontani dal centro per testare distanze
        Field field16 = new Field("F007", "Campo Nord Milano", Sport.FOOTBALL_5, "Via Fulvio Testi 123", "Milano");
        // ...existing code...

        Field field17 = new Field("B004", "Basket Sud Milano", Sport.BASKETBALL, "Via Ripamonti 300", "Milano");
        // ...existing code...

        Field field18 = new Field("T004", "Tennis Ovest", Sport.TENNIS_SINGLE, "Via Novara 350", "Milano");
        // ...existing code...

        Field field19 = new Field("F008", "Campo Est Milano", Sport.FOOTBALL_8, "Via Mecenate 76", "Milano");
        // ...existing code...

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
                .collect(Collectors.toList());
    }

    @Override
    public List<Field> findBySport(Sport sport) {
        return fields.values().stream()
                .filter(field -> field.getSport() == sport)
                .collect(Collectors.toList());
    }

    @Override
    public List<Field> findAvailableFields(Sport sport, String city, LocalDate date, LocalTime time) {
        return fields.values().stream()
                .filter(field -> field.getSport() == sport)
                .filter(field -> field.isInCity(city))
                .filter(Field::isAvailable)
                .sorted((f1, f2) -> Double.compare(f1.getPricePerHour(), f2.getPricePerHour()))
                .collect(Collectors.toList());
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

