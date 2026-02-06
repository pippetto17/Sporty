package model.notification;

public interface NotificationObserver {

    /* Observer */

    void onEvent(NotificationEvent event);
}