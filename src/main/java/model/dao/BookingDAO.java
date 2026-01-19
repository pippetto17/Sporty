package model.dao;

import model.domain.Booking;
import model.domain.BookingStatus;

import java.util.List;

public interface BookingDAO {

    void save(Booking booking);

    Booking findById(int bookingId);

    List<Booking> findByFieldId(String fieldId);

    List<Booking> findByRequesterId(String username);

    List<Booking> findPendingByManagerId(String managerId);

    List<Booking> findByStatus(BookingStatus status);

    void updateStatus(int bookingId, BookingStatus newStatus);

    void delete(int bookingId);
}
