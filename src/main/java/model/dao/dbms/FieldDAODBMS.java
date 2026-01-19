package model.dao.dbms;

import exception.DataAccessException;
import model.dao.FieldDAO;
import model.domain.Field;
import model.domain.Sport;
import model.utils.Constants;

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
        String sql = "SELECT field_id, name, sport, address, city, price_per_hour, availability, indoor, manager_id, structure_name, auto_approve FROM fields ORDER BY name";
        List<Field> fields = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                fields.add(extractFieldFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_FINDING_ALL_FIELDS + e.getMessage(), e);
        }

        return fields;
    }

    @Override
    public Field findById(String fieldId) {
        String sql = "SELECT field_id, name, sport, address, city, price_per_hour, availability, indoor, manager_id, structure_name, auto_approve FROM fields WHERE field_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fieldId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractFieldFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_FINDING_FIELD + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Field> findByCity(String city) {
        String sql = "SELECT field_id, name, sport, address, city, price_per_hour, availability, indoor, manager_id, structure_name, auto_approve FROM fields WHERE city = ? ORDER BY name";
        List<Field> fields = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, city);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                fields.add(extractFieldFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_FINDING_FIELDS_BY_CITY + e.getMessage(), e);
        }

        return fields;
    }

    @Override
    public List<Field> findBySport(Sport sport) {
        String sql = "SELECT field_id, name, sport, address, city, price_per_hour, availability, indoor, manager_id, structure_name, auto_approve FROM fields WHERE sport = ? ORDER BY name";
        List<Field> fields = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sport.getCode());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                fields.add(extractFieldFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_FINDING_FIELDS_BY_SPORT + e.getMessage(), e);
        }

        return fields;
    }

    @Override
    public List<Field> findAvailableFields(Sport sport, String city, LocalDate date, LocalTime time) {
        String sql = """
                SELECT f.field_id, f.name, f.sport, f.address, f.city, f.price_per_hour,
                       f.availability, f.indoor, f.manager_id, f.structure_name, f.auto_approve
                FROM fields f
                WHERE f.sport = ? AND f.city = ?
                AND f.field_id NOT IN (
                    SELECT b.field_id
                    FROM bookings b
                    WHERE b.booking_date = ?
                    AND b.start_time < ?  -- Existing booking starts before requested end
                    AND b.end_time > ?    -- Existing booking ends after requested start
                    AND b.status NOT IN (2, 3) -- Exclude REJECTED (2) and CANCELLED (3)
                )
                ORDER BY f.price_per_hour
                """;
        List<Field> fields = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sport.getCode());
            stmt.setString(2, city);
            stmt.setDate(3, Date.valueOf(date));

            // Calculate end time based on sport duration
            LocalTime endTime = time.plusMinutes(sport.getDuration());

            stmt.setTime(4, Time.valueOf(endTime)); // Requested End Time
            stmt.setTime(5, Time.valueOf(time)); // Requested Start Time

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                fields.add(extractFieldFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_FINDING_AVAILABLE_FIELDS + e.getMessage(), e);
        }

        return fields;
    }

    @Override
    public List<Field> findByManagerId(String managerId) {
        String sql = """
                SELECT field_id, name, sport, address, city,
                       price_per_hour, availability, indoor, manager_id, structure_name, auto_approve
                FROM fields
                WHERE manager_id = ?
                ORDER BY name
                """;
        List<Field> fields = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, managerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                fields.add(extractFieldFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding fields by manager ID: " + e.getMessage(), e);
        }

        return fields;
    }

    @Override
    public void save(Field field) {
        String sql = """
                INSERT INTO fields (field_id, name, sport, address, city,
                                   price_per_hour, availability, indoor, manager_id, structure_name, auto_approve)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    sport = VALUES(sport),
                    address = VALUES(address),
                    city = VALUES(city),
                    price_per_hour = VALUES(price_per_hour),
                    availability = VALUES(availability),
                    indoor = VALUES(indoor),
                    manager_id = VALUES(manager_id),
                    structure_name = VALUES(structure_name),
                    auto_approve = VALUES(auto_approve)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, field.getFieldId());
            stmt.setString(2, field.getName());
            stmt.setInt(3, field.getSport().ordinal());
            stmt.setString(4, field.getAddress());
            stmt.setString(5, field.getCity());

            stmt.setDouble(6, field.getPricePerHour());
            stmt.setString(7, field.getAvailability());
            stmt.setBoolean(8, field.isIndoor());

            // Field Manager fields
            if (field.getManagerId() != null) {
                stmt.setString(9, field.getManagerId());
            } else {
                stmt.setNull(9, Types.VARCHAR);
            }

            if (field.getStructureName() != null) {
                stmt.setString(10, field.getStructureName());
            } else {
                stmt.setNull(10, Types.VARCHAR);
            }

            stmt.setBoolean(11, field.getAutoApprove() != null && field.getAutoApprove());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_SAVING_FIELD + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String fieldId) {
        String sql = "DELETE FROM fields WHERE field_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fieldId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_DELETING_FIELD + e.getMessage(), e);
        }
    }

    private Field extractFieldFromResultSet(ResultSet rs) throws SQLException {
        Field field = new Field();
        field.setFieldId(rs.getString("field_id"));
        field.setName(rs.getString("name"));
        field.setSport(Sport.fromCode(rs.getInt("sport")));
        field.setAddress(rs.getString("address"));
        field.setCity(rs.getString("city"));

        field.setPricePerHour(rs.getDouble("price_per_hour"));
        field.setAvailability(rs.getString("availability"));
        field.setIndoor(rs.getBoolean("indoor"));

        // Field Manager fields (may not be present in all queries)
        try {
            field.setManagerId(rs.getString("manager_id"));
            field.setStructureName(rs.getString("structure_name"));
            field.setAutoApprove(rs.getBoolean("auto_approve"));
        } catch (SQLException e) {
            // These columns might not be selected in all queries, ignore if missing
        }

        return field;
    }
}
