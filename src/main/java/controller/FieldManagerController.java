package controller;

import exception.ValidationException;
import model.bean.FieldBean;
import model.bean.TimeSlotBean;
import model.bean.BookingBean;
import model.dao.DAOFactory;
import model.domain.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller for field manager operations.
 * Handles field management, availability, and booking approvals.
 */
public class FieldManagerController {
    private final User fieldManager;
    private final model.dao.FieldDAO fieldDAO;
    private final model.dao.BookingDAO bookingDAO;
    private final model.dao.TimeSlotDAO timeSlotDAO;

    public FieldManagerController(User fieldManager, DAOFactory.PersistenceType persistenceType)
            throws SQLException, ValidationException {
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
        model.domain.Field field = model.converter.FieldConverter.toEntity(fieldBean);
        field.setManagerId(fieldManager.getUsername());
        fieldDAO.save(field);
    }

    public List<FieldBean> getMyFields() {
        return fieldDAO.findByManagerId(fieldManager.getUsername()).stream()
                .map(model.converter.FieldConverter::toBean)
                .toList();
    }

    private static final String ERROR_NOT_OWNER = "Manager does not own field: ";

    // ...

    public void updateField(FieldBean fieldBean) {
        // Validate ownership
        getMyFields().stream()
                .filter(f -> f.getFieldId().equals(fieldBean.getFieldId()))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(ERROR_NOT_OWNER + fieldBean.getFieldId()));

        model.domain.Field field = model.converter.FieldConverter.toEntity(fieldBean);
        field.setManagerId(fieldManager.getUsername());
        fieldDAO.save(field);
    }

    public void deleteField(String fieldId) {
        // Validate ownership
        getMyFields().stream()
                .filter(f -> f.getFieldId().equals(fieldId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ERROR_NOT_OWNER + fieldId));

        fieldDAO.delete(fieldId);
    }

    // ...

    public void setFieldSchedule(String fieldId, List<TimeSlotBean> schedule) {
        // Validate ownership
        getMyFields().stream()
                .filter(f -> f.getFieldId().equals(fieldId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ERROR_NOT_OWNER + fieldId));

        // Delete existing and save new
        timeSlotDAO.deleteByFieldId(fieldId);
        for (TimeSlotBean slot : schedule) {
            model.domain.TimeSlot timeSlot = new model.domain.TimeSlot(
                    slot.getFieldId(),
                    slot.getDayOfWeek(),
                    slot.getStartTime(),
                    slot.getEndTime());
            // Basic save, real implementation might need checks
            timeSlotDAO.save(timeSlot);
        }
    }

    public List<TimeSlotBean> getFieldSchedule(String fieldId) {
        return timeSlotDAO.findByFieldId(fieldId).stream()
                .map(slot -> {
                    TimeSlotBean bean = new TimeSlotBean();
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
        // Simplified: Return all slots, real implementation would filter out booked
        // ones.
        return getFieldSchedule(fieldId);
    }

    // ==================== Booking Management ====================

    public List<BookingBean> getPendingRequests() {
        return bookingDAO.findPendingByManagerId(fieldManager.getUsername()).stream()
                .map(model.converter.BookingConverter::toBean)
                .toList();
    }

    public void approveBooking(int bookingId) {
        model.domain.Booking booking = bookingDAO.findById(bookingId);
        if (booking != null) {
            booking.setStatus(model.domain.BookingStatus.CONFIRMED);
            bookingDAO.save(booking);
        }
    }

    public void rejectBooking(int bookingId) {
        model.domain.Booking booking = bookingDAO.findById(bookingId);
        if (booking != null) {
            booking.setStatus(model.domain.BookingStatus.REJECTED);
            bookingDAO.save(booking);
        }
    }

    public void rejectBooking(int bookingId, String reason) {
        // Overload to support reason, even if we don't store it in this simple version
        // yet
        // or if we add reason support to domain later.
        // For now, delegate to simple reject or just save status.
        model.domain.Booking booking = bookingDAO.findById(bookingId);
        if (booking != null) {
            booking.setStatus(model.domain.BookingStatus.REJECTED);
            // Optionally store reason if Booking entity supports it
            booking.setRejectionReason(reason);
            bookingDAO.save(booking);
        }
    }

    public List<BookingBean> getFieldBookings(String fieldId) {
        return bookingDAO.findByFieldId(fieldId).stream()
                .map(model.converter.BookingConverter::toBean)
                .toList();
    }

    // ==================== Dashboard Data ====================

    public DashboardData getDashboardData() {
        List<FieldBean> fields = getMyFields();
        List<BookingBean> pendingRequests = getPendingRequests();

        int totalFields = fields.size();
        int pendingCount = pendingRequests.size();

        return new DashboardData(totalFields, pendingCount, 0, 0.0);
    }

    public User getFieldManager() {
        return fieldManager;
    }

    public static class DashboardData {
        private final int totalFields;
        private final int pendingRequests;
        private final int todayBookings;
        private final double weekRevenue;

        public DashboardData(int totalFields, int pendingRequests,
                int todayBookings, double weekRevenue) {
            this.totalFields = totalFields;
            this.pendingRequests = pendingRequests;
            this.todayBookings = todayBookings;
            this.weekRevenue = weekRevenue;
        }

        public int getTotalFields() {
            return totalFields;
        }

        public int getPendingRequests() {
            return pendingRequests;
        }

        public int getTodayBookings() {
            return todayBookings;
        }

        public double getWeekRevenue() {
            return weekRevenue;
        }
    }
}
