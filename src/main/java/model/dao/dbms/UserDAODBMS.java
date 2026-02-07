package model.dao.dbms;

import exception.DataAccessException;
import model.dao.UserDAO;
import model.domain.Role;
import model.domain.User;

import java.sql.*;

public class UserDAODBMS implements UserDAO {
    private final Connection connection;

    public UserDAODBMS(Connection connection) {
        this.connection = connection;
    }

    @Override
    public User authenticate(String username, String password) {
        String query = "SELECT id, username, password, name, surname, role FROM user WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error authenticating user", e);
        }
        return null;
    }

    @Override
    public User findById(int id) {
        String query = "SELECT id, username, password, name, surname, role FROM user WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by id: " + id, e);
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        String query = "SELECT id, username, password, name, surname, role FROM user WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by username: " + username, e);
        }
        return null;
    }

    @Override
    public void save(User user) {
        String query = "INSERT INTO user (username, password, name, surname, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getSurname());
            stmt.setInt(5, user.getRole().getCode());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving user", e);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("name"),
                rs.getString("surname"),
                Role.fromCode(rs.getInt("role")));
    }
}