package model.observer;

import model.dao.NotificationDAO;
import model.domain.Match;
import model.domain.Notification;

public class MatchNotificationObserver implements Observer {
    private final Match match;
    private final NotificationDAO notificationDAO;

    public MatchNotificationObserver(Match match, NotificationDAO notificationDAO) {
        this.match = match;
        this.notificationDAO = notificationDAO;
    }

    @Override
    public void update() {
        // PULL: Get data from the observed Match (Subject)
        if (match.getField() == null || match.getField().getManager() == null) {
            return;
        }

        String fieldManagerUsername = match.getField().getManager().getUsername();
        String title = "New Match Request";
        String message = String.format("%s has organized a %s match at '%s' for %s at %s",
                match.getOrganizer().getUsername(),
                match.getField().getSport().getDisplayName(),
                match.getField().getName(),
                match.getDate(),
                match.getTime());

        Notification notification = new Notification(fieldManagerUsername, title, message);
        notificationDAO.save(notification);
    }
}
