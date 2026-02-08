package model.dao.dbms;

import exception.DataAccessException;
import model.dao.NotificationDAO;
import model.domain.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DBMS implementation of NotificationDAO.
 */
public class NotificationDAODBMS implements NotificationDAO {
    private final Connection connection;

    public NotificationDAODBMS(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Notification notification) {
        String query = "INSERT INTO notifications (recipient_username, title, message, is_read, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, notification.getRecipientUsername());
            stmt.setString(2, notification.getTitle());
            stmt.setString(3, notification.getMessage());
            stmt.setBoolean(4, notification.isRead());
            stmt.setTimestamp(5, Timestamp.valueOf(notification.getCreatedAt()));
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        notification.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving notification", e);
        }
    }

    @Override
    public List<Notification> findUnreadByUsername(String username) {
        String query = "SELECT id, recipient_username, title, message, is_read, created_at FROM notifications WHERE recipient_username = ? AND is_read = false ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRowToNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding unread notifications for: " + username, e);
        }
        return notifications;
    }

    @Override
    public void markAllAsRead(String username) {
        String query = "UPDATE notifications SET is_read = true WHERE recipient_username = ? AND is_read = false";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error marking notifications as read for: " + username, e);
        }
    }

    private Notification mapRowToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setRecipientUsername(rs.getString("recipient_username"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return notification;
    }
}
