package model.dao.dbms;

import model.dao.FieldDAO;
import model.domain.Field;
import model.domain.Sport;
import exception.DataAccessException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FieldDAODBMS implements FieldDAO {
    private final Connection connection;

    public FieldDAODBMS(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Field> findAll() {
        String sql = "SELECT * FROM fields ORDER BY name";
        List<Field> fields = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                fields.add(extractFieldFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all fields: " + e.getMessage(), e);
        }

        return fields;
    }

    @Override
    public Field findById(String fieldId) {
        String sql = "SELECT * FROM fields WHERE field_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fieldId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractFieldFromResultSet(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding field: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Field> findByCity(String city) {
        String sql = "SELECT * FROM fields WHERE city = ? ORDER BY name";
        List<Field> fields = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, city);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                fields.add(extractFieldFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding fields by city: " + e.getMessage(), e);
        }

        return fields;
    }

    @Override
    public List<Field> findBySport(Sport sport) {
        String sql = "SELECT * FROM fields WHERE sport = ? ORDER BY name";
        List<Field> fields = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sport.getCode());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                fields.add(extractFieldFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding fields by sport: " + e.getMessage(), e);
        }

        return fields;
    }

    @Override
    public List<Field> findAvailableFields(Sport sport, String city, LocalDate date, LocalTime time) {
        // For now, we return all fields matching sport and city
        // TODO: implement actual availability check based on bookings
        String sql = "SELECT * FROM fields WHERE sport = ? AND city = ? ORDER BY price_per_hour";
        List<Field> fields = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sport.getCode());
            stmt.setString(2, city);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                fields.add(extractFieldFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding available fields: " + e.getMessage(), e);
        }

        return fields;
    }

    @Override
    public void save(Field field) {
        String sql = """
                INSERT INTO fields (field_id, name, sport, address, city, latitude, longitude,
                                   price_per_hour, availability, indoor)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    sport = VALUES(sport),
                    address = VALUES(address),
                    city = VALUES(city),
                    latitude = VALUES(latitude),
                    longitude = VALUES(longitude),
                    price_per_hour = VALUES(price_per_hour),
                    availability = VALUES(availability),
                    indoor = VALUES(indoor)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, field.getFieldId());
            stmt.setString(2, field.getName());
            stmt.setInt(3, field.getSport().ordinal());
            stmt.setString(4, field.getAddress());
            stmt.setString(5, field.getCity());

            if (field.getLatitude() != null) {
                stmt.setDouble(6, field.getLatitude());
            } else {
                stmt.setNull(6, Types.DOUBLE);
            }

            if (field.getLongitude() != null) {
                stmt.setDouble(7, field.getLongitude());
            } else {
                stmt.setNull(7, Types.DOUBLE);
            }

            stmt.setDouble(8, field.getPricePerHour());
            stmt.setString(9, field.getAvailability());
            stmt.setBoolean(10, field.isIndoor());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error saving field: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String fieldId) {
        String sql = "DELETE FROM fields WHERE field_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fieldId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting field: " + e.getMessage(), e);
        }
    }

    private Field extractFieldFromResultSet(ResultSet rs) throws SQLException {
        Field field = new Field();
        field.setFieldId(rs.getString("field_id"));
        field.setName(rs.getString("name"));
        field.setSport(Sport.fromCode(rs.getInt("sport")));
        field.setAddress(rs.getString("address"));
        field.setCity(rs.getString("city"));

        double lat = rs.getDouble("latitude");
        field.setLatitude(rs.wasNull() ? null : lat);

        double lon = rs.getDouble("longitude");
        field.setLongitude(rs.wasNull() ? null : lon);

        field.setPricePerHour(rs.getDouble("price_per_hour"));
        field.setAvailability(rs.getString("availability"));
        field.setIndoor(rs.getBoolean("indoor"));

        return field;
    }
}
