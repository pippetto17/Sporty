package model.domain;

import java.time.LocalDateTime;

/**
 * Domain entity for notifications.
 * Stores notification data for Field Managers.
 */
public class Notification {
    private int id;
    private String recipientUsername;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public Notification() {
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(String recipientUsername, String title, String message) {
        this();
        this.recipientUsername = recipientUsername;
        this.title = title;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
