package testing;

import model.dao.NotificationDAO;
import model.dao.filesystem.NotificationDAOFileSystem;
import model.notification.NotificationEvent;
import model.notification.NotificationObserver;
import model.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Notification System Test Suite")
class NotificationSystemTest {

    private NotificationService notificationService;
    private NotificationDAO notificationDAO;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
        notificationDAO = new NotificationDAOFileSystem();
    }

    @Test
    @DisplayName("Observer riceve notifica in real-time quando viene creato un match")
    void testObserverReceivesMatchCreationNotification() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<NotificationEvent> receivedEvents = new ArrayList<>();

        NotificationObserver observer = event -> {
            receivedEvents.add(event);
            latch.countDown();
        };

        notificationService.subscribe(observer);
        notificationService.notifyMatchCreated(
            "manager1", "organizer1", "Campo Nord",
            "2026-01-25", "18:00", "FOOTBALL_5"
        );

        boolean received = latch.await(1, TimeUnit.SECONDS);
        assertTrue(received);
        assertEquals(1, receivedEvents.size());
        assertEquals(NotificationEvent.Type.MATCH_CREATED, receivedEvents.get(0).type);
        assertEquals("manager1", receivedEvents.get(0).recipient);
    }

    @Test
    @DisplayName("Observer riceve notifica quando viene prenotato un campo")
    void testObserverReceivesBookingNotification() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<NotificationEvent> receivedEvents = new ArrayList<>();

        NotificationObserver observer = event -> {
            receivedEvents.add(event);
            latch.countDown();
        };

        notificationService.subscribe(observer);
        notificationService.notifyBookingCreated(
            "manager1", "player1", "Campo Centrale",
            "2026-01-26", "20:00"
        );

        boolean received = latch.await(1, TimeUnit.SECONDS);
        assertTrue(received);
        assertEquals(1, receivedEvents.size());
        assertEquals(NotificationEvent.Type.BOOKING_CREATED, receivedEvents.get(0).type);
        assertTrue(receivedEvents.get(0).message.contains("ha prenotato"));
    }

    @Test
    @DisplayName("Notifica viene persistita su filesystem (JSON)")
    void testNotificationPersistence() {
        notificationService.notifyBookingCreated(
            "manager2", "player2", "Campo Sud",
            "2026-01-27", "14:00"
        );

        List<String> unreadNotifications = notificationDAO.getUnreadNotifications("manager2");

        assertFalse(unreadNotifications.isEmpty());
        assertTrue(unreadNotifications.get(0).contains("Nuova prenotazione!"));
        assertTrue(unreadNotifications.get(0).contains("player2"));
    }

    @Test
    @DisplayName("Mark as read funziona correttamente")
    void testMarkAsRead() {
        notificationService.notifyMatchCreated(
            "manager3", "organizer3", "Campo Est",
            "2026-01-28", "19:00", "BASKETBALL"
        );

        List<String> unread = notificationDAO.getUnreadNotifications("manager3");
        assertFalse(unread.isEmpty());

        notificationDAO.markAllAsRead("manager3");

        List<String> afterRead = notificationDAO.getUnreadNotifications("manager3");
        assertTrue(afterRead.isEmpty());
    }

    @Test
    @DisplayName("Multiple observers ricevono la stessa notifica")
    void testMultipleObservers() throws InterruptedException {
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        NotificationObserver observer1 = event -> latch1.countDown();
        NotificationObserver observer2 = event -> latch2.countDown();

        notificationService.subscribe(observer1);
        notificationService.subscribe(observer2);

        notificationService.notifyMatchCreated(
            "manager4", "organizer4", "Campo Ovest",
            "2026-01-29", "17:00", "FOOTBALL_11"
        );

        boolean received1 = latch1.await(1, TimeUnit.SECONDS);
        boolean received2 = latch2.await(1, TimeUnit.SECONDS);

        assertTrue(received1);
        assertTrue(received2);
    }

    @Test
    @DisplayName("Unsubscribe funziona correttamente")
    void testUnsubscribe() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        NotificationObserver observer = event -> latch.countDown();

        notificationService.subscribe(observer);
        notificationService.unsubscribe(observer);

        notificationService.notifyBookingCreated(
            "manager5", "player5", "Campo Test",
            "2026-01-30", "16:00"
        );

        boolean received = latch.await(500, TimeUnit.MILLISECONDS);
        assertFalse(received);
    }
}

