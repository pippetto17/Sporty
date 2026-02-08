package model.converter;

import model.bean.NotificationBean;
import model.domain.Notification;

public class NotificationConverter {

    private NotificationConverter() {
        // Private constructor to prevent instantiation
    }

    public static NotificationBean toBean(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationBean bean = new NotificationBean();
        bean.setRecipient(notification.getRecipientUsername());
        bean.setTitle(notification.getTitle());
        bean.setMessage(notification.getMessage());
        bean.setTimestamp(notification.getCreatedAt());
        bean.setRead(notification.isRead());
        return bean;
    }

    public static Notification toEntity(NotificationBean bean) {
        if (bean == null) {
            return null;
        }
        Notification notification = new Notification();
        notification.setRecipientUsername(bean.getRecipient());
        notification.setTitle(bean.getTitle());
        notification.setMessage(bean.getMessage());
        notification.setCreatedAt(bean.getTimestamp());
        notification.setRead(bean.isRead());
        return notification;
    }
}
