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
    private static final Map<Integer, Field> fields = new HashMap<>();
    private static int idCounter = 1;
    private static final String DEMO_CITY = "Milano";

    static {
        // Initialize with some demo data
        initializeDemoFields();
    }

    private static void createAndAddField(String name, Sport sport, String city, int managerId) {
        int id = idCounter++;
        Field field = new Field(id, name, city, sport, managerId);
        fields.put(id, field);
    }

    private static void initializeDemoFields() {
        // Football 5 fields - Zona Centro
        createAndAddField("City Sports Center", Sport.FOOTBALL_5, DEMO_CITY, 3);
        createAndAddField("Arena Calcetto Centrale", Sport.FOOTBALL_5, DEMO_CITY, 3);
        createAndAddField("San Siro Five", Sport.FOOTBALL_5, DEMO_CITY, 3);

        // Football 8 fields
        createAndAddField("Campo 8 Navigli", Sport.FOOTBALL_8, DEMO_CITY, 3);

        // Football 11 fields
        createAndAddField("Green Stadium", Sport.FOOTBALL_11, DEMO_CITY, 3);
        createAndAddField("Stadio Bicocca", Sport.FOOTBALL_11, DEMO_CITY, 3);

        // Basketball fields
        createAndAddField("Indoor Basketball Arena", Sport.BASKETBALL, DEMO_CITY, 3);
        createAndAddField("Basket City Loreto", Sport.BASKETBALL, DEMO_CITY, 3);
        createAndAddField("PlayBasket Porta Romana", Sport.BASKETBALL, DEMO_CITY, 3);

        // Tennis fields
        createAndAddField("Tennis Club Milano", Sport.TENNIS_SINGLE, DEMO_CITY, 3);
        createAndAddField("Tennis Forlanini", Sport.TENNIS_SINGLE, DEMO_CITY, 3);
        createAndAddField("Tennis Double Garibaldi", Sport.TENNIS_DOUBLE, DEMO_CITY, 3);

        // Padel fields
        createAndAddField("Padel Center", Sport.PADEL_DOUBLE, DEMO_CITY, 3);
        createAndAddField("Padel Arena Citylife", Sport.PADEL_DOUBLE, DEMO_CITY, 3);
        createAndAddField("Padel Club Lambrate", Sport.PADEL_SINGLE, DEMO_CITY, 3);
    }

    @Override
    public List<Field> findAll() {
        return new ArrayList<>(fields.values());
    }

    @Override
    public Field findById(int id) {
        return fields.get(id);
    }

    @Override
    public List<Field> findByCity(String city) {
        return fields.values().stream()
                .filter(field -> field.getCity().equalsIgnoreCase(city))
                .collect(Collectors.toList());
    }

    @Override
    public List<Field> findAvailableFields(String city, Sport sport, LocalDate date, LocalTime time) {
        return fields.values().stream()
                .filter(field -> field.getCity().equalsIgnoreCase(city))
                .filter(field -> field.getSport() == sport)
                .collect(Collectors.toList());
    }

    @Override
    public List<Field> findByManagerId(int managerId) {
        return fields.values().stream()
                .filter(field -> field.getManagerId() == managerId)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Field field) {
        if (field.getId() == 0) {
            field.setId(idCounter++);
        }
        fields.put(field.getId(), field);
    }

    @Override
    public void delete(int id) {
        fields.remove(id);
    }

    public static void clearAll() {
        fields.clear();
        initializeDemoFields();
    }
}
