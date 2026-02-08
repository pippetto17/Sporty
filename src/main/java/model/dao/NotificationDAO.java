package model.dao;

import model.domain.Notification;

import java.util.List;

/**
 * DAO interface for Notification persistence.
 */
public interface NotificationDAO {
    void save(Notification notification);

    List<Notification> findUnreadByUsername(String username);

    void markAllAsRead(String username);
}