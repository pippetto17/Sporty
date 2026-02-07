package model.notification;

import model.dao.NotificationDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NotificationService implements Subject {

    /* Concrete Subject */

    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());
    private final List<Observer> observers = new ArrayList<>();
    private final Object mutex = new Object();
    private final NotificationDAO dao;

    public NotificationService(NotificationDAO dao) {
        this.dao = dao;
    }

    @Override
    public void attach(Observer observer) {
        synchronized (mutex) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(Observer observer) {
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
        notifyObserversWithEvent(event);
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
        notifyObserversWithEvent(event);
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
        notifyObserversWithEvent(event);
    }

    @Override
    public void notifyObservers() {
        // This is a simplified version without event data
        // In practice, the specific notify methods handle the full notification logic
        throw new UnsupportedOperationException("Use specific notify methods instead");
    }

    private void notifyObserversWithEvent(NotificationEvent event) {
        dao.save(event.recipient, event.sender, event.type.name(), event.title, event.message);

        List<Observer> observersCopy;
        synchronized (mutex) {
            observersCopy = new ArrayList<>(observers);
        }

        observersCopy.forEach(o -> {
            try {
                o.update(event);
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