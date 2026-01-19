package controller;

import exception.ValidationException;
import model.bean.BookingBean;
import model.bean.FieldBean;
import model.bean.TimeSlotBean;
import model.dao.DAOFactory;
import model.domain.User;

import java.util.List;

public class FieldManagerController {
    private static final String ERROR_NOT_OWNER = "Manager does not own field: ";
    private static final String ERROR_BOOKING_NOT_FOUND = "Booking not found with ID: ";

    private final User fieldManager;
    private final model.dao.FieldDAO fieldDAO;
    private final model.dao.BookingDAO bookingDAO;
    private final model.dao.TimeSlotDAO timeSlotDAO;

    public FieldManagerController(User fieldManager, DAOFactory.PersistenceType persistenceType)
            throws ValidationException {
        if (!fieldManager.isFieldManager()) {
            throw new ValidationException("User must be a field manager");
        }

        this.fieldManager = fieldManager;
        this.fieldDAO = DAOFactory.getFieldDAO(persistenceType);
        this.bookingDAO = DAOFactory.getBookingDAO(persistenceType);
        this.timeSlotDAO = DAOFactory.getTimeSlotDAO(persistenceType);
    }

    // ==================== Field Management ====================

    public void addNewField(FieldBean fieldBean) {
        var field = model.converter.FieldConverter.toEntity(fieldBean);
        field.setManagerId(fieldManager.getUsername());
        fieldDAO.save(field);
    }

    public List<FieldBean> getMyFields() {
        return fieldDAO.findByManagerId(fieldManager.getUsername()).stream()
                .map(model.converter.FieldConverter::toBean)
                .toList();
    }

    public void updateField(FieldBean fieldBean) {
        validateOwnership(fieldBean.getFieldId());

        var field = model.converter.FieldConverter.toEntity(fieldBean);
        field.setManagerId(fieldManager.getUsername());
        fieldDAO.save(field);
    }

    public void deleteField(String fieldId) {
        validateOwnership(fieldId);
        fieldDAO.delete(fieldId);
    }

    public void setFieldSchedule(String fieldId, List<TimeSlotBean> schedule) {
        validateOwnership(fieldId);

        timeSlotDAO.deleteByFieldId(fieldId);
        for (var slot : schedule) {
            var timeSlot = new model.domain.TimeSlot(
                    slot.getFieldId(),
                    slot.getDayOfWeek(),
                    slot.getStartTime(),
                    slot.getEndTime());
            timeSlotDAO.save(timeSlot);
        }
    }

    public List<TimeSlotBean> getFieldSchedule(String fieldId) {
        return timeSlotDAO.findByFieldId(fieldId).stream()
                .map(slot -> {
                    var bean = new TimeSlotBean();
                    bean.setSlotId(slot.getSlotId());
                    bean.setFieldId(slot.getFieldId());
                    bean.setDayOfWeek(slot.getDayOfWeek());
                    bean.setStartTime(slot.getStartTime());
                    bean.setEndTime(slot.getEndTime());
                    return bean;
                })
                .toList();
    }

    public List<TimeSlotBean> getAvailableSlots(String fieldId) {
        return getFieldSchedule(fieldId);
    }

    // ==================== Booking Management ====================

    public List<BookingBean> getPendingRequests() {
        return bookingDAO.findPendingByManagerId(fieldManager.getUsername()).stream()
                .map(model.converter.BookingConverter::toBean)
                .toList();
    }

    public void approveBooking(int bookingId) throws ValidationException {
        var booking = findBookingOrThrow(bookingId);
        booking.setStatus(model.domain.BookingStatus.CONFIRMED);
        bookingDAO.save(booking);
    }

    public void rejectBooking(int bookingId) throws ValidationException {
        var booking = findBookingOrThrow(bookingId);
        booking.setStatus(model.domain.BookingStatus.REJECTED);
        bookingDAO.save(booking);
    }

    public List<BookingBean> getFieldBookings(String fieldId) {
        return bookingDAO.findByFieldId(fieldId).stream()
                .map(model.converter.BookingConverter::toBean)
                .toList();
    }

    // ==================== Dashboard Data ====================

    public DashboardData getDashboardData() {
        var fields = getMyFields();
        var pendingRequests = getPendingRequests();

        int totalFields = fields.size();
        int pendingCount = pendingRequests.size();

        return new DashboardData(totalFields, pendingCount, 0, 0.0);
    }

    public User getFieldManager() {
        return fieldManager;
    }

    private void validateOwnership(String fieldId) {
        getMyFields().stream()
                .filter(f -> f.getFieldId().equals(fieldId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ERROR_NOT_OWNER + fieldId));
    }

    private model.domain.Booking findBookingOrThrow(int bookingId) throws ValidationException {
        var booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new ValidationException(ERROR_BOOKING_NOT_FOUND + bookingId);
        }
        return booking;
    }

    public record DashboardData(int totalFields, int pendingRequests, int todayBookings, double weekRevenue) {
    }
}
