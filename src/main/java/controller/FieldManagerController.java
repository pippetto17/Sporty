package controller;

import model.bean.FieldBean;
import model.bean.TimeSlotBean;
import model.bean.BookingBean;
import model.dao.DAOFactory;
import model.domain.User;
import model.service.AvailabilityService;
import model.service.BookingService;
import model.service.FieldManagementService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller for field manager operations.
 * Handles field management, availability, and booking approvals.
 */
public class FieldManagerController {
    private final User fieldManager;
    private final FieldManagementService fieldManagementService;
    private final AvailabilityService availabilityService;
    private final BookingService bookingService;

    public FieldManagerController(User fieldManager, DAOFactory.PersistenceType persistenceType)
            throws SQLException {
        if (fieldManager.getRole() != model.domain.Role.FIELD_MANAGER.getCode()) {
            throw new IllegalArgumentException("User must be a field manager");
        }

        this.fieldManager = fieldManager;
        this.fieldManagementService = new FieldManagementService(persistenceType);
        this.availabilityService = new AvailabilityService(persistenceType);
        this.bookingService = new BookingService(persistenceType);
    }

    // ==================== Field Management ====================

    /**
     * Add a new field to the manager's portfolio.
     */
    public void addNewField(FieldBean field) {
        fieldManagementService.addField(field, fieldManager.getUsername());
    }

    /**
     * Get all fields owned by this manager.
     */
    public List<FieldBean> getMyFields() {
        return fieldManagementService.getManagerFields(fieldManager.getUsername());
    }

    /**
     * Update field details.
     */
    public void updateField(FieldBean field) {
        fieldManagementService.updateField(field, fieldManager.getUsername());
    }

    /**
     * Delete a field.
     */
    public void deleteField(String fieldId) {
        fieldManagementService.deleteField(fieldId, fieldManager.getUsername());
    }

    // ==================== Availability Management ====================

    /**
     * Set weekly availability schedule for a field.
     */
    public void setFieldSchedule(String fieldId, List<TimeSlotBean> schedule) {
        // Verify manager owns this field
        List<FieldBean> myFields = getMyFields();
        boolean ownsField = myFields.stream()
                .anyMatch(f -> f.getFieldId().equals(fieldId));

        if (!ownsField) {
            throw new IllegalArgumentException("Manager does not own field: " + fieldId);
        }

        availabilityService.setWeeklySchedule(fieldId, schedule);
    }

    /**
     * Get field schedule.
     */
    public List<TimeSlotBean> getFieldSchedule(String fieldId) {
        return availabilityService.getFieldSchedule(fieldId);
    }

    /**
     * Get available slots for a specific date.
     */
    public List<TimeSlotBean> getAvailableSlots(String fieldId, LocalDate date) {
        return availabilityService.getAvailableSlots(fieldId, date);
    }

    /**
     * Block a time slot (e.g., for maintenance).
     */
    public void blockSlot(String fieldId, LocalDate date, LocalTime start, LocalTime end) {
        availabilityService.blockSlot(fieldId, date, start, end);
    }

    /**
     * Unblock a time slot.
     */
    public void unblockSlot(int slotId) {
        availabilityService.unblockSlot(slotId);
    }

    // ==================== Booking Management ====================

    /**
     * Get all pending booking requests for manager's fields.
     */
    public List<BookingBean> getPendingRequests() {
        return bookingService.getPendingBookingsForManager(fieldManager.getUsername());
    }

    /**
     * Approve a booking request.
     */
    public void approveBooking(int bookingId) {
        bookingService.approveBooking(bookingId, fieldManager.getUsername());
    }

    /**
     * Reject a booking request with reason.
     */
    public void rejectBooking(int bookingId, String reason) {
        bookingService.rejectBooking(bookingId, fieldManager.getUsername(), reason);
    }

    /**
     * Get all bookings for a specific field.
     */
    public List<BookingBean> getFieldBookings(String fieldId) {
        return bookingService.getFieldBookings(fieldId);
    }

    // ==================== Dashboard Data ====================

    /**
     * Get dashboard data for field manager.
     */
    public DashboardData getDashboardData() {
        List<FieldBean> fields = getMyFields();
        List<BookingBean> pendingRequests = getPendingRequests();

        // Calculate stats
        int totalFields = fields.size();
        int pendingCount = pendingRequests.size();

        // TODO: Calculate revenue, today's bookings, etc.

        return new DashboardData(totalFields, pendingCount, 0, 0.0);
    }

    public User getFieldManager() {
        return fieldManager;
    }

    /**
     * Simple dashboard data class.
     */
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
