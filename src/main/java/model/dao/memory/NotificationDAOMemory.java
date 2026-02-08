package model.dao.memory;

import model.dao.NotificationDAO;
import model.domain.Notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory implementation of NotificationDAO for testing/demo purposes.
 */
public class NotificationDAOMemory implements NotificationDAO {
    private static final Map<Integer, Notification> notifications = new HashMap<>();
    private static final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public void save(Notification notification) {
        notification.setId(idGenerator.getAndIncrement());
        notifications.put(notification.getId(), notification);
    }

    @Override
    public List<Notification> findUnreadByUsername(String username) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : notifications.values()) {
            if (n.getRecipientUsername().equals(username) && !n.isRead()) {
                result.add(n);
            }
        }
        return result;
    }

    @Override
    public void markAllAsRead(String username) {
        for (Notification n : notifications.values()) {
            if (n.getRecipientUsername().equals(username)) {
                n.setRead(true);
            }
        }
    }

    // For testing: clear all notifications
    public static void clear() {
        notifications.clear();
    }
}