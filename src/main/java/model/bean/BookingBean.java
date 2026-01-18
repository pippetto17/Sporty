package model.bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Bean class for transferring booking data between layers.
 */
public class BookingBean {
    private Integer bookingId;
    private String fieldId;
    private String fieldName; // Enriched data for display
    private String requesterUsername;
    private String requesterFullName; // Enriched data for display
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String type; // Display name of BookingType
    private String status; // Display name of BookingStatus
    private Double totalPrice;
    private LocalDateTime requestedAt;
    private LocalDateTime confirmedAt;

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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public String getRequesterFullName() {
        return requesterFullName;
    }

    public void setRequesterFullName(String requesterFullName) {
        this.requesterFullName = requesterFullName;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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


    @Override
    public String toString() {
        return String.format("BookingBean[id=%d, field=%s, date=%s, %s-%s, status=%s]",
                bookingId, fieldName, bookingDate, startTime, endTime, status);
    }
}
