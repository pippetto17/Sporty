package model.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Domain class representing a field booking.
 * Supports both match bookings and private bookings.
 */
public class Booking {
    private Integer bookingId;
    private String fieldId;
    private String requesterUsername; // Organizer or Player who requested
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingType type;
    private BookingStatus status;
    private Double totalPrice;
    private LocalDateTime requestedAt;
    private LocalDateTime confirmedAt;

    public Booking() {
        this.status = BookingStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public BookingType getType() {
        return type;
    }

    public void setType(BookingType type) {
        this.type = type;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", this.status, newStatus));
        }
        this.status = newStatus;

        if (newStatus == BookingStatus.CONFIRMED) {
            this.confirmedAt = LocalDateTime.now();
        }
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }


    /**
     * Check if booking is active (confirmed and in future).
     */
    public boolean isActive() {
        return status == BookingStatus.CONFIRMED &&
                bookingDate != null &&
                !bookingDate.isBefore(LocalDate.now());
    }

    /**
     * Check if booking can be cancelled.
     */
    public boolean isCancellable() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    @Override
    public String toString() {
        return String.format("Booking[id=%d, field=%s, date=%s, %s-%s, status=%s]",
                bookingId, fieldId, bookingDate, startTime, endTime, status);
    }
}
