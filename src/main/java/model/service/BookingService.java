package model.service;

import model.bean.BookingBean;
import model.converter.BookingConverter;
import model.dao.BookingDAO;
import model.dao.DAOFactory;
import model.dao.FieldDAO;
import model.domain.Booking;
import model.domain.BookingStatus;
import model.domain.BookingType;
import model.domain.Field;
import model.observer.BookingObserver;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing field bookings.
 * Implements Observer pattern for notifications.
 */
public class BookingService {
    private final BookingDAO bookingDAO;
    private final FieldDAO fieldDAO;
    private final List<BookingObserver> observers;

    public BookingService(DAOFactory.PersistenceType persistenceType) throws SQLException {
        this.bookingDAO = DAOFactory.getBookingDAO(persistenceType);
        this.fieldDAO = DAOFactory.getFieldDAO(persistenceType);
        this.observers = new ArrayList<>();
    }

    /**
     * Add an observer for booking events.
     */
    public void addObserver(BookingObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Remove an observer.
     */
    public void removeObserver(BookingObserver observer) {
        observers.remove(observer);
    }

    /**
     * Create a new booking request.
     * 
     * @return BookingBean with generated ID
     */
    public BookingBean requestBooking(String fieldId, String requesterUsername,
            LocalDate date, LocalTime start, LocalTime end,
            BookingType type) {
        // Validate field exists
        Field field = fieldDAO.findById(fieldId);
        if (field == null) {
            throw new IllegalArgumentException("Field not found: " + fieldId);
        }

        // Create booking
        Booking booking = new Booking();
        booking.setFieldId(fieldId);
        booking.setRequesterUsername(requesterUsername);
        booking.setBookingDate(date);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setType(type);

        // Calculate price
        double hours = booking.getDurationHours();
        booking.setTotalPrice(field.getPricePerHour() * hours);

        // Save booking
        bookingDAO.save(booking);

        // Notify observers
        notifyObservers(booking, BookingEvent.REQUESTED);

        // Convert to bean and enrich
        BookingBean bean = BookingConverter.toBookingBean(booking);
        bean.setFieldName(field.getName());

        return bean;
    }

    /**
     * Approve a booking request (by field manager).
     */
    public void approveBooking(int bookingId, String managerId) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }

        // Verify manager owns this field
        Field field = fieldDAO.findById(booking.getFieldId());
        if (field == null || !managerId.equals(field.getManagerId())) {
            throw new IllegalArgumentException("Unauthorized: manager does not own this field");
        }

        // Verify booking is in PENDING state
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Can only approve pending bookings");
        }

        // Update status
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingDAO.save(booking);

        // Notify observers
        notifyObservers(booking, BookingEvent.APPROVED);
    }

    /**
     * Reject a booking request (by field manager).
     */
    public void rejectBooking(int bookingId, String managerId, String reason) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }

        // Verify manager owns this field
        Field field = fieldDAO.findById(booking.getFieldId());
        if (field == null || !managerId.equals(field.getManagerId())) {
            throw new IllegalArgumentException("Unauthorized: manager does not own this field");
        }

        // Verify booking is in PENDING state
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Can only reject pending bookings");
        }

        // Update status and reason
        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(reason);
        bookingDAO.save(booking);

        // Notify observers
        notifyObservers(booking, BookingEvent.REJECTED);
    }

    /**
     * Cancel a booking (by requester).
     */
    public void cancelBooking(int bookingId, String username) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }

        // Verify user is the requester
        if (!username.equals(booking.getRequesterUsername())) {
            throw new IllegalArgumentException("Unauthorized: only requester can cancel");
        }

        // Verify booking can be cancelled
        if (!booking.isCancellable()) {
            throw new IllegalStateException("Booking cannot be cancelled in current state");
        }

        // Update status
        booking.setStatus(BookingStatus.CANCELLED);
        bookingDAO.save(booking);

        // Notify observers
        notifyObservers(booking, BookingEvent.CANCELLED);
    }

    /**
     * Get all bookings for a specific user.
     */
    public List<BookingBean> getUserBookings(String username) {
        List<Booking> bookings = bookingDAO.findByRequesterId(username);
        return enrichBookings(bookings);
    }

    /**
     * Get pending bookings for a field manager.
     */
    public List<BookingBean> getPendingBookingsForManager(String managerId) {
        List<Booking> bookings = bookingDAO.findPendingByManagerId(managerId);
        return enrichBookings(bookings);
    }

    /**
     * Get all bookings for a specific field.
     */
    public List<BookingBean> getFieldBookings(String fieldId) {
        List<Booking> bookings = bookingDAO.findByFieldId(fieldId);
        return enrichBookings(bookings);
    }

    // Helper methods

    private List<BookingBean> enrichBookings(List<Booking> bookings) {
        List<BookingBean> beans = new ArrayList<>();
        for (Booking booking : bookings) {
            BookingBean bean = BookingConverter.toBookingBean(booking);

            // Enrich with field name
            Field field = fieldDAO.findById(booking.getFieldId());
            if (field != null) {
                bean.setFieldName(field.getName());
            }

            beans.add(bean);
        }
        return beans;
    }

    private void notifyObservers(Booking booking, BookingEvent event) {
        for (BookingObserver observer : observers) {
            try {
                switch (event) {
                    case REQUESTED -> observer.onBookingRequested(booking);
                    case APPROVED -> observer.onBookingApproved(booking);
                    case REJECTED -> observer.onBookingRejected(booking);
                    case CANCELLED -> observer.onBookingCancelled(booking);
                }
            } catch (Exception e) {
                // Log but don't fail if observer throws exception
                System.err.println("Observer error: " + e.getMessage());
            }
        }
    }

    private enum BookingEvent {
        REQUESTED, APPROVED, REJECTED, CANCELLED
    }
}
