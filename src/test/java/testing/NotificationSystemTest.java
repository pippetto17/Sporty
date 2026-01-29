package testing;

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

    @BeforeEach
    void setUp() {
        notificationService = NotificationService.getInstance();
        notificationService.markAllAsRead("manager1");
        notificationService.markAllAsRead("manager2");
        notificationService.markAllAsRead("manager3");
        notificationService.markAllAsRead("manager4");
        notificationService.markAllAsRead("manager5");
    }

    @Test
    @DisplayName("Observer receives real-time notification when a match is created")
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
                "2026-01-25", "18:00", "FOOTBALL_5");

        boolean received = latch.await(1, TimeUnit.SECONDS);
        assertTrue(received);
        assertEquals(1, receivedEvents.size());
        assertEquals(NotificationEvent.Type.MATCH_CREATED, receivedEvents.get(0).type);
        assertEquals("manager1", receivedEvents.get(0).recipient);

        notificationService.unsubscribe(observer);
    }

    @Test
    @DisplayName("Observer receives notification when a field is booked")
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
                "2026-01-26", "20:00");

        boolean received = latch.await(1, TimeUnit.SECONDS);
        assertTrue(received);
        assertEquals(1, receivedEvents.size());
        assertEquals(NotificationEvent.Type.BOOKING_CREATED, receivedEvents.get(0).type);
        // assert message follows NotificationService formatting (english)
        assertTrue(receivedEvents.get(0).message.contains("has booked"));

        notificationService.unsubscribe(observer);
    }

    @Test
    @DisplayName("Notification is persisted (Service access)")
    void testNotificationPersistence() {
        notificationService.notifyBookingCreated(
                "manager2", "player2", "Campo Sud",
                "2026-01-27", "14:00");

        List<String> unreadNotifications = notificationService.getUnreadNotifications("manager2");

        assertFalse(unreadNotifications.isEmpty());
        // persist title is "New booking!" in NotificationService
        assertTrue(unreadNotifications.get(0).contains("New booking!"));
        assertTrue(unreadNotifications.get(0).contains("player2"));
    }

    @Test
    @DisplayName("Mark as read works correctly")
    void testMarkAsRead() {
        notificationService.notifyMatchCreated(
                "manager3", "organizer3", "Campo Est",
                "2026-01-28", "19:00", "BASKETBALL");

        List<String> unread = notificationService.getUnreadNotifications("manager3");
        assertFalse(unread.isEmpty());

        notificationService.markAllAsRead("manager3");

        List<String> afterRead = notificationService.getUnreadNotifications("manager3");
        assertTrue(afterRead.isEmpty());
    }

    @Test
    @DisplayName("Multiple observers receive the same notification")
    void testMultipleObservers() throws InterruptedException {
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        NotificationObserver observer1 = event -> latch1.countDown();
        NotificationObserver observer2 = event -> latch2.countDown();

        notificationService.subscribe(observer1);
        notificationService.subscribe(observer2);

        notificationService.notifyMatchCreated(
                "manager4", "organizer4", "Campo Ovest",
                "2026-01-29", "17:00", "FOOTBALL_11");

        boolean received1 = latch1.await(1, TimeUnit.SECONDS);
        boolean received2 = latch2.await(1, TimeUnit.SECONDS);

        assertTrue(received1);
        assertTrue(received2);

        notificationService.unsubscribe(observer1);
        notificationService.unsubscribe(observer2);
    }

    @Test
    @DisplayName("Unsubscribe works correctly")
    void testUnsubscribe() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        NotificationObserver observer = event -> latch.countDown();

        notificationService.subscribe(observer);
        notificationService.unsubscribe(observer);

        notificationService.notifyBookingCreated(
                "manager5", "player5", "Campo Test",
                "2026-01-30", "16:00");

        boolean received = latch.await(500, TimeUnit.MILLISECONDS);
        assertFalse(received);
    }
}
