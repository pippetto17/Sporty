package model.dao.memory;

import model.dao.FieldDAO;
import model.domain.Field;
import model.domain.Sport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldDAOMemory implements FieldDAO {
        private static final Map<Integer, Field> fields = new HashMap<>();
        private static int idCounter = 1;
        private static final String DEMO_CITY = "Milano";
        static {
                initializeDemoFields();
        }

        private static void createAndAddField(String name, Sport sport, String city, String address,
                        double pricePerHour,
                        int managerId) {
                int id = nextId();
                model.domain.User manager = new model.domain.User();
                manager.setId(managerId);
                Field field = new Field(id, name, city, address, pricePerHour, sport, manager);
                fields.put(id, field);
        }

        private static synchronized int nextId() {
                return idCounter++;
        }

        private static void initializeDemoFields() {
                createAndAddField("City Sports Center", Sport.FOOTBALL_5, DEMO_CITY,
                                "Via Centrale 15, Milano", 100.0, 3);
                createAndAddField("Arena Calcetto Centrale", Sport.FOOTBALL_5, DEMO_CITY,
                                "Via Arena 22, Milano", 90.0, 3);
                createAndAddField("San Siro Five", Sport.FOOTBALL_5, DEMO_CITY,
                                "Piazzale San Siro 1, Milano", 120.0, 3);
                createAndAddField("Campo 8 Navigli", Sport.FOOTBALL_8, DEMO_CITY,
                                "Via Naviglio Grande 45, Milano", 160.0, 3);
                createAndAddField("Green Stadium", Sport.FOOTBALL_11, DEMO_CITY,
                                "Via Stadio 100, Milano", 220.0, 3);
                createAndAddField("Stadio Bicocca", Sport.FOOTBALL_11, DEMO_CITY,
                                "Viale Bicocca 30, Milano", 200.0, 3);
                createAndAddField("Indoor Basketball Arena", Sport.BASKETBALL, DEMO_CITY,
                                "Via Basket 8, Milano", 100.0, 3);
                createAndAddField("Basket City Loreto", Sport.BASKETBALL, DEMO_CITY,
                                "Piazzale Loreto 5, Milano", 90.0, 3);
                createAndAddField("PlayBasket Porta Romana", Sport.BASKETBALL, DEMO_CITY,
                                "Corso Porta Romana 120, Milano", 80.0, 3);
                createAndAddField("Tennis Club Milano", Sport.TENNIS_SINGLE, DEMO_CITY,
                                "Via Tennis 10, Milano", 40.0, 3);
                createAndAddField("Tennis Forlanini", Sport.TENNIS_SINGLE, DEMO_CITY,
                                "Parco Forlanini 1, Milano", 35.0, 3);
                createAndAddField("Tennis Double Garibaldi", Sport.TENNIS_DOUBLE, DEMO_CITY,
                                "Via Garibaldi 55, Milano", 60.0, 3);
                createAndAddField("Padel Center", Sport.PADEL_DOUBLE, DEMO_CITY,
                                "Via Padel 20, Milano", 60.0, 3);
                createAndAddField("Padel Arena Citylife", Sport.PADEL_DOUBLE, DEMO_CITY,
                                "Citylife Shopping District, Milano", 70.0, 3);
                createAndAddField("Padel Club Lambrate", Sport.PADEL_SINGLE, DEMO_CITY,
                                "Via Lambrate 88, Milano", 40.0, 3);
        }

        @Override
        public List<Field> findAll() {
                return List.copyOf(fields.values());
        }

        @Override
        public Field findById(int id) {
                return fields.get(id);
        }

        @Override
        public List<Field> findByCity(String city) {
                return fields.values().stream()
                                .filter(field -> field.getCity().equalsIgnoreCase(city))
                                .filter(field -> field.getCity() != null)
                                .toList();
        }

        @Override
        public List<Field> findAvailableFields(String city, Sport sport, LocalDate date, LocalTime time) {
                return fields.values().stream()
                                .filter(field -> field.getCity().equalsIgnoreCase(city))
                                .filter(field -> field.getSport() == sport)
                                .toList();
        }

        @Override
        public List<Field> findByManagerId(int managerId) {
                return fields.values().stream()
                                .filter(field -> field.getManager() != null && field.getManager().getId() == managerId)
                                .toList();
        }

        @Override
        public void save(Field field) {
                if (field.getId() == 0) {
                        field.setId(nextId());
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