package model.notification;

import model.dao.NotificationDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NotificationService {

    /* Subject */

    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());
    private final List<NotificationObserver> observers = new ArrayList<>();
    private final Object mutex = new Object();
    private final NotificationDAO dao;

    public NotificationService(NotificationDAO dao) {
        this.dao = dao;
    }

    public void subscribe(NotificationObserver observer) {
        synchronized (mutex) {
            observers.add(observer);
        }
    }

    public void unsubscribe(NotificationObserver observer) {
        synchronized (mutex) {
            observers.remove(observer);
        }
    }

    public void notifyBookingCreated(String fieldManagerUsername, String organizerUsername,
            String fieldName, String date, String time) {
        String message = String.format("%s has booked the field '%s' for %s at %s",
                organizerUsername, fieldName, date, time);
        NotificationEvent event = new NotificationEvent(
                NotificationEvent.Type.BOOKING_CREATED,
                fieldManagerUsername,
                organizerUsername,
                "New booking!",
                message);
        notifyObservers(event);
    }

    public void notifyMatchCreated(String fieldManagerUsername, String organizerUsername,
            String fieldName, String date, String time, String sport) {
        String message = String.format("%s has organized a %s match at field '%s' for %s at %s",
                organizerUsername, sport, fieldName, date, time);
        NotificationEvent event = new NotificationEvent(
                NotificationEvent.Type.MATCH_CREATED,
                fieldManagerUsername,
                organizerUsername,
                "New match organized!",
                message);
        notifyObservers(event);
    }

    public void notifyBookingRequest(String fieldManagerUsername, String organizerUsername,
            String fieldName, String date, String time, String sport) {
        String message = String.format(
                "New booking request from %s for %s match at '%s' on %s at %s - Approval required!",
                organizerUsername, sport, fieldName, date, time);
        NotificationEvent event = new NotificationEvent(
                NotificationEvent.Type.BOOKING_REQUEST,
                fieldManagerUsername,
                organizerUsername,
                "⚠️ Booking Request - Action Required",
                message);
        notifyObservers(event);
    }

    private void notifyObservers(NotificationEvent event) {
        dao.save(event.recipient, event.sender, event.type.name(), event.title, event.message);

        List<NotificationObserver> observersCopy;
        synchronized (mutex) {
            observersCopy = new ArrayList<>(observers);
        }

        observersCopy.forEach(o -> {
            try {
                o.onEvent(event);
            } catch (Exception e) {
                logger.warning("Observer error: " + e.getMessage());
            }
        });
    }

    public List<String> getUnreadNotifications(String username) {
        return dao.getUnreadNotifications(username);
    }

    public void markAllAsRead(String username) {
        dao.markAllAsRead(username);
    }
}