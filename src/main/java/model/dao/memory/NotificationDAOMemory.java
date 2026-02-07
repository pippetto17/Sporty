package model.dao.memory;

import model.dao.NotificationDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class NotificationDAOMemory implements NotificationDAO {
    private static final Map<String, List<String>> inbox = new HashMap<>();
    @Override
    public void save(String recipient, String sender, String type, String title, String message) {
        inbox.computeIfAbsent(recipient, k -> new ArrayList<>())
                .add(String.format("[%s] %s: %s", type, title, message));
    }
    @Override
    public List<String> getUnreadNotifications(String username) {
        return List.copyOf(inbox.getOrDefault(username, List.of()));
    }
    @Override
    public void markAllAsRead(String username) {
        inbox.remove(username);
    }
}