package model.dao.filesystem;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.bean.NotificationBean;
import model.dao.NotificationDAO;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NotificationDAOFileSystem implements NotificationDAO {
    private static final Logger logger = Logger.getLogger(NotificationDAOFileSystem.class.getName());
    private static final String JSON_FILE = "data/notifications.json";
    private final Gson gson;

    public NotificationDAOFileSystem() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                        LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .create();
        ensureFileExists();
    }

    @Override
    public void save(String recipient, String sender, String type, String title, String message) {
        List<NotificationBean> notifications = loadNotifications();

        NotificationBean bean = new NotificationBean();
        bean.setRecipient(recipient);
        bean.setSender(sender);
        bean.setType(type);
        bean.setTitle(title);
        bean.setMessage(message);
        bean.setTimestamp(LocalDateTime.now());
        bean.setRead(false);

        notifications.add(bean);
        saveNotifications(notifications);
    }

    @Override
    public List<String> getUnreadNotifications(String username) {
        List<String> result = new ArrayList<>();
        List<NotificationBean> notifications = loadNotifications();

        for (NotificationBean n : notifications) {
            if (n.getRecipient().equals(username) && !n.isRead()) {
                result.add(n.getTitle() + ": " + n.getMessage());
            }
        }
        return result;
    }

    @Override
    public void markAllAsRead(String username) {
        List<NotificationBean> notifications = loadNotifications();

        for (NotificationBean n : notifications) {
            if (n.getRecipient().equals(username)) {
                n.setRead(true);
            }
        }

        saveNotifications(notifications);
    }

    private List<NotificationBean> loadNotifications() {
        try {
            File file = new File(JSON_FILE);
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }

            String json = Files.readString(Paths.get(JSON_FILE));
            List<NotificationBean> list = gson.fromJson(json, new TypeToken<List<NotificationBean>>(){}.getType());
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            logger.warning("Error loading notifications: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveNotifications(List<NotificationBean> notifications) {
        try {
            String json = gson.toJson(notifications);
            Files.writeString(Paths.get(JSON_FILE), json);
        } catch (IOException e) {
            logger.warning("Error saving notifications: " + e.getMessage());
        }
    }

    private void ensureFileExists() {
        try {
            File file = new File(JSON_FILE);
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                Files.writeString(Paths.get(JSON_FILE), "[]");
            }
        } catch (IOException e) {
            logger.warning("Could not create notifications file: " + e.getMessage());
        }
    }
}

