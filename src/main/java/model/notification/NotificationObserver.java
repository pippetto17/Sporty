package model.notification;

public interface NotificationObserver {
    void onEvent(NotificationEvent event);
}
