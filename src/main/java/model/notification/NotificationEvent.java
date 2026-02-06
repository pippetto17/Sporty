package model.notification;

import java.time.LocalDateTime;

public class NotificationEvent {

    /* Event */

    public enum Type {
        BOOKING_CREATED, BOOKING_REQUEST, MATCH_CREATED
    }

    public final Type type;
    public final String recipient;
    public final String sender;
    public final String title;
    public final String message;
    public final LocalDateTime timestamp;

    public NotificationEvent(Type type, String recipient, String sender, String title, String message) {
        this.type = type;
        this.recipient = recipient;
        this.sender = sender;
        this.title = title;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}