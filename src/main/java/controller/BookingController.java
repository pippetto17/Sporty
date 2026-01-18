package controller;

import exception.ValidationException;
import model.bean.BookingBean;
import model.dao.DAOFactory;
import model.domain.BookingType;
import model.domain.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller for booking operations.
 * Handles booking requests, approvals, and cancellations.
 */
@SuppressWarnings("unused")
public class BookingController {
    private final User currentUser;
    private final model.dao.BookingDAO bookingDAO;

    public BookingController(User currentUser, DAOFactory.PersistenceType persistenceType) {
        this.currentUser = currentUser;
        this.bookingDAO = DAOFactory.getBookingDAO(persistenceType);
    }

    public BookingBean requestMatchBooking(String fieldId, LocalDate date,
            LocalTime startTime, LocalTime endTime) {
        return createBooking(fieldId, date, startTime, endTime, BookingType.MATCH);
    }

    public BookingBean requestPrivateBooking(String fieldId, LocalDate date,
            LocalTime startTime, LocalTime endTime) {
        return createBooking(fieldId, date, startTime, endTime, BookingType.PRIVATE);
    }

    private BookingBean createBooking(String fieldId, LocalDate date, LocalTime startTime, LocalTime endTime,
            BookingType type) {
        model.domain.Booking booking = new model.domain.Booking();
        booking.setFieldId(fieldId);
        booking.setRequesterUsername(currentUser.getUsername());
        booking.setBookingDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setType(type);
        booking.setStatus(model.domain.BookingStatus.PENDING);

        bookingDAO.save(booking);
        return model.converter.BookingConverter.toBean(booking);
    }

    public List<BookingBean> getMyBookings() {
        return bookingDAO.findByRequesterId(currentUser.getUsername()).stream()
                .map(model.converter.BookingConverter::toBean)
                .toList();
    }

    public List<BookingBean> getPendingRequests() throws ValidationException {
        validateFieldManager();
        return bookingDAO.findPendingByManagerId(currentUser.getUsername()).stream()
                .map(model.converter.BookingConverter::toBean)
                .toList();
    }

    public void approveBooking(int bookingId) throws ValidationException {
        validateFieldManager();
        model.domain.Booking booking = bookingDAO.findById(bookingId);
        if (booking != null) {
            booking.setStatus(model.domain.BookingStatus.CONFIRMED);
            bookingDAO.save(booking);
        }
    }

    public void rejectBooking(int bookingId, String reason) throws ValidationException {
        validateFieldManager();
        model.domain.Booking booking = bookingDAO.findById(bookingId);
        if (booking != null) {
            booking.setStatus(model.domain.BookingStatus.REJECTED);
            // Save rejection reason if domain supports it
            if (reason != null && !reason.isBlank()) {
                booking.setRejectionReason(reason);
            }
            bookingDAO.save(booking);
        }
    }

    public void cancelMyBooking(int bookingId) {
        model.domain.Booking booking = bookingDAO.findById(bookingId);
        if (booking != null && booking.getRequesterUsername().equals(currentUser.getUsername())) {
            booking.setStatus(model.domain.BookingStatus.CANCELLED);
            bookingDAO.save(booking);
        }
    }

    public List<BookingBean> getFieldBookings(String fieldId) {
        return bookingDAO.findByFieldId(fieldId).stream()
                .map(model.converter.BookingConverter::toBean)
                .toList();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private void validateFieldManager() throws ValidationException {
        if (!currentUser.isFieldManager()) {
            throw new ValidationException("Only field managers can perform this operation");
        }
    }
}
