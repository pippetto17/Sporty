package model.dao.dbms;

import exception.DataAccessException;
import model.dao.UserDAO;
import model.domain.User;
import model.utils.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserDAODBMS implements UserDAO {
    private final Connection connection;
    // Cache to avoid repeated queries for the same user
    private final Map<String, User> userCache = new HashMap<>();

    public UserDAODBMS(Connection connection) {
        this.connection = connection;
    }

    @Override
    public User authenticate(String username, String password) {
        User user = findByUsername(username);
        if (user != null && user.matchesPassword(password)) {
            return user;
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        // Check cache first
        if (userCache.containsKey(username)) {
            return userCache.get(username);
        }

        String query = "SELECT username, password, name, surname, role FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));
                user.setSurname(rs.getString("surname"));
                user.setRole(rs.getInt("role"));

                // Store in cache
                userCache.put(username, user);
                return user;
            }
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_FINDING_USER, e);
        }
        return null;
    }

    @Override
    public void save(User user) {
        String query = "INSERT INTO users (username, password, name, surname, role) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE password = ?, name = ?, surname = ?, role = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getSurname());
            stmt.setInt(5, user.getRole());
            stmt.setString(6, user.getPassword());
            stmt.setString(7, user.getName());
            stmt.setString(8, user.getSurname());
            stmt.setInt(9, user.getRole());
            stmt.executeUpdate();

            // Invalidate cache for this user since data has changed
            invalidateCache(user.getUsername());
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_SAVING_USER, e);
        }
    }

    // Method to invalidate cache when user data changes
    public void invalidateCache(String username) {
        userCache.remove(username);
    }

    // Method to clear entire cache
    public void clearCache() {
        userCache.clear();
    }
}
