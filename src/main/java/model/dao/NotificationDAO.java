package model.dao;

import java.util.List;

public interface NotificationDAO {
    void save(String recipient, String sender, String type, String title, String message);

    List<String> getUnreadNotifications(String username);

    void markAllAsRead(String username);
}