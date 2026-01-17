package model.dao;

import model.domain.Booking;
import model.domain.BookingStatus;

import java.util.List;

/**
 * DAO interface for Booking entity.
 * Handles persistence of field booking requests and confirmations.
 */
public interface BookingDAO {

    /**
     * Save or update a booking.
     */
    void save(Booking booking);

    /**
     * Find booking by ID.
     * 
     * @return Booking if found, null otherwise
     */
    Booking findById(int bookingId);

    /**
     * Find all bookings for a specific field.
     */
    List<Booking> findByFieldId(String fieldId);

    /**
     * Find all bookings made by a specific requester (organizer or player).
     */
    List<Booking> findByRequesterId(String username);

    /**
     * Find all pending bookings for fields owned by a specific manager.
     */
    List<Booking> findPendingByManagerId(String managerId);

    /**
     * Find all bookings with a specific status.
     */
    List<Booking> findByStatus(BookingStatus status);

    /**
     * Update booking status.
     * Note: Use Booking.setStatus() for state machine validation before calling
     * this.
     */
    void updateStatus(int bookingId, BookingStatus newStatus);

    /**
     * Delete a booking.
     */
    void delete(int bookingId);
}
