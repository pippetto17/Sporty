package model.dao.dbms;

import model.dao.FieldDAO;
import model.domain.Field;
import model.domain.Sport;

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
        List<Field> fields = new ArrayList<>();
        String query = "SELECT id, name, city, address, price_per_hour, sport, manager_id FROM field";
        try (PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                fields.add(mapRowToField(rs));
            }
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error finding all fields", e);
        }
        return fields;
    }

    @Override
    public Field findById(int id) {
        String query = "SELECT id, name, city, address, price_per_hour, sport, manager_id FROM field WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToField(rs);
                }
            }
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error finding field by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Field> findByCity(String city) {
        List<Field> fields = new ArrayList<>();
        String query = "SELECT id, name, city, address, price_per_hour, sport, manager_id FROM field WHERE city = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, city);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fields.add(mapRowToField(rs));
                }
            }
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error finding fields by city: " + city, e);
        }
        return fields;
    }

    @Override
    public List<Field> findAvailableFields(String city, Sport sport, LocalDate date, LocalTime time) {
        List<Field> fields = new ArrayList<>();
        String query = "SELECT f.id, f.name, f.city, f.address, f.price_per_hour, f.sport, f.manager_id FROM field f WHERE f.city = ? AND f.sport = ? "
                +
                "AND NOT EXISTS (SELECT 1 FROM matches m WHERE m.field_id = f.id " +
                "AND m.date = ? AND m.time = ? AND m.status = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, city);
            stmt.setInt(2, sport.getCode());
            stmt.setDate(3, java.sql.Date.valueOf(date));
            stmt.setTime(4, java.sql.Time.valueOf(time));
            stmt.setInt(5, model.domain.MatchStatus.APPROVED.getCode());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fields.add(mapRowToField(rs));
                }
            }
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error finding available fields in " + city, e);
        }
        return fields;
    }

    @Override
    public List<Field> findByManagerId(int managerId) {
        List<Field> fields = new ArrayList<>();
        String query = "SELECT id, name, city, address, price_per_hour, sport, manager_id FROM field WHERE manager_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, managerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fields.add(mapRowToField(rs));
                }
            }
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error finding fields by manager id: " + managerId, e);
        }
        return fields;
    }

    @Override
    public void save(Field field) {
        String query = "INSERT INTO field (name, city, address, price_per_hour, sport, manager_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, field.getName());
            stmt.setString(2, field.getCity());
            stmt.setString(3, field.getAddress());
            stmt.setDouble(4, field.getPricePerHour());
            stmt.setInt(5, field.getSport().getCode());
            // Extract ID from manager entity
            stmt.setInt(6, field.getManager() != null ? field.getManager().getId() : 0);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        field.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error saving field", e);
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM field WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error deleting field with id: " + id, e);
        }
    }

    private Field mapRowToField(ResultSet rs) throws SQLException {
        int managerId = rs.getInt("manager_id");

        UserDAODBMS userDAO = new UserDAODBMS(connection);
        model.domain.User manager = userDAO.findById(managerId);

        return new Field(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("city"),
                rs.getString("address"),
                rs.getDouble("price_per_hour"),
                model.domain.Sport.fromCode(rs.getInt("sport")),
                manager); // Full entity instead of ID
    }
}