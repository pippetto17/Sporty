package controller;

import model.bean.BookingBean;
import model.dao.DAOFactory;
import model.domain.BookingType;
import model.domain.User;
import model.service.BookingService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller for booking operations.
 * Handles booking requests, approvals, and cancellations.
 */
public class BookingController {
    private final User currentUser;
    private final BookingService bookingService;

    public BookingController(User currentUser, DAOFactory.PersistenceType persistenceType) throws SQLException {
        this.currentUser = currentUser;
        this.bookingService = new BookingService(persistenceType);
    }

    /**
     * Request a field booking for a match.
     * Used by organizers when booking a field for their match.
     */
    public BookingBean requestMatchBooking(String fieldId, LocalDate date,
            LocalTime startTime, LocalTime endTime) {
        return bookingService.requestBooking(
                fieldId,
                currentUser.getUsername(),
                date,
                startTime,
                endTime,
                BookingType.MATCH);
    }

    /**
     * Request a private field booking.
     * Used by players for private bookings.
     */
    public BookingBean requestPrivateBooking(String fieldId, LocalDate date,
            LocalTime startTime, LocalTime endTime) {
        return bookingService.requestBooking(
                fieldId,
                currentUser.getUsername(),
                date,
                startTime,
                endTime,
                BookingType.PRIVATE);
    }

    /**
     * Get all bookings for current user.
     */
    public List<BookingBean> getMyBookings() {
        return bookingService.getUserBookings(currentUser.getUsername());
    }

    /**
     * Get pending booking requests (for field managers only).
     */
    public List<BookingBean> getPendingRequests() {
        // Only field managers can see pending requests
        if (currentUser.getRole() != model.domain.Role.FIELD_MANAGER.getCode()) {
            throw new IllegalStateException("Only field managers can view pending requests");
        }
        return bookingService.getPendingBookingsForManager(currentUser.getUsername());
    }

    /**
     * Approve a booking request (field managers only).
     */
    public void approveBooking(int bookingId) {
        if (currentUser.getRole() != model.domain.Role.FIELD_MANAGER.getCode()) {
            throw new IllegalStateException("Only field managers can approve bookings");
        }
        bookingService.approveBooking(bookingId, currentUser.getUsername());
    }

    /**
     * Reject a booking request (field managers only).
     */
    public void rejectBooking(int bookingId, String reason) {
        if (currentUser.getRole() != model.domain.Role.FIELD_MANAGER.getCode()) {
            throw new IllegalStateException("Only field managers can reject bookings");
        }
        bookingService.rejectBooking(bookingId, currentUser.getUsername(), reason);
    }

    /**
     * Cancel own booking.
     */
    public void cancelMyBooking(int bookingId) {
        bookingService.cancelBooking(bookingId, currentUser.getUsername());
    }

    /**
     * Get bookings for a specific field (managers can check their fields).
     */
    public List<BookingBean> getFieldBookings(String fieldId) {
        return bookingService.getFieldBookings(fieldId);
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
