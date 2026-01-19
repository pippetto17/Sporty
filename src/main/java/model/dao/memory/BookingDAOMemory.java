package model.dao.memory;

import model.dao.BookingDAO;
import model.dao.DAOFactory;
import model.domain.Booking;
import model.domain.BookingStatus;
import model.domain.SlotStatus;
import model.domain.TimeSlot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory implementation of BookingDAO for testing.
 */
public class BookingDAOMemory implements BookingDAO {
    private final Map<Integer, Booking> bookings = new HashMap<>();
    private int nextId = 1;

    @Override
    public void save(Booking booking) {
        if (booking.getBookingId() == null) {
            booking.setBookingId(nextId++);
        }
        bookings.put(booking.getBookingId(), booking);

        // Create time slot entry for confirmed bookings
        if (booking.getBookingId() != null && booking.getStatus() == BookingStatus.CONFIRMED) {
            createTimeSlotForBooking(booking);
        }
    }

    private void createTimeSlotForBooking(Booking booking) {
        TimeSlot slot = new TimeSlot();
        slot.setFieldId(booking.getFieldId());
        slot.setDayOfWeek(booking.getBookingDate().getDayOfWeek());
        slot.setBookingDate(booking.getBookingDate());
        slot.setStartTime(booking.getStartTime());
        slot.setEndTime(booking.getEndTime());
        slot.setStatus(SlotStatus.BOOKED);
        slot.setBookingId(booking.getBookingId());

        DAOFactory.getTimeSlotDAO(DAOFactory.PersistenceType.MEMORY).save(slot);
    }

    @Override
    public Booking findById(int bookingId) {
        return bookings.get(bookingId);
    }

    @Override
    public List<Booking> findByFieldId(String fieldId) {
        return bookings.values().stream()
                .filter(b -> b.getFieldId().equals(fieldId))
                .toList();
    }

    @Override
    public List<Booking> findByRequesterId(String username) {
        return bookings.values().stream()
                .filter(b -> b.getRequesterUsername().equals(username))
                .toList();
    }

    @Override
    public List<Booking> findPendingByManagerId(String managerId) {
        // In memory implementation: would need to join with fields to get managerId
        // Simplified: return all pending bookings
        return bookings.values().stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .toList();
    }

    @Override
    public List<Booking> findByStatus(BookingStatus status) {
        return bookings.values().stream()
                .filter(b -> b.getStatus() == status)
                .toList();
    }

    @Override
    public void updateStatus(int bookingId, BookingStatus newStatus) {
        Booking booking = bookings.get(bookingId);
        if (booking != null) {
            booking.setStatus(newStatus); // Uses state machine validation in domain
        }
    }

    @Override
    public void delete(int bookingId) {
        bookings.remove(bookingId);
    }

    // Testing utility
    public void clearAll() {
        bookings.clear();
        nextId = 1;
    }
}
