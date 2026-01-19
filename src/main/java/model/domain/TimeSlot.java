package model.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Domain class representing a time slot in a field's schedule.
 * - bookingDate == null: recurring weekly availability template
 * - bookingDate != null: specific booking for that date
 */
public class TimeSlot {
    private Integer slotId;
    private String fieldId;
    private DayOfWeek dayOfWeek;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private SlotStatus status;
    private Integer bookingId;

    public TimeSlot() {
        this.status = SlotStatus.AVAILABLE;
    }

    public TimeSlot(String fieldId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this();
        this.fieldId = fieldId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters

    public Integer getSlotId() {
        return slotId;
    }

    public void setSlotId(Integer slotId) {
        this.slotId = slotId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
        if (bookingId != null && bookingDate != null) {
            this.status = SlotStatus.BOOKED;
        } else if (bookingDate == null) {
            this.status = SlotStatus.AVAILABLE;
        }
    }

    /**
     * Check if this slot overlaps with another time range.
     */
    public boolean overlapsWith(LocalTime otherStart, LocalTime otherEnd) {
        return model.utils.Utils.timeRangesOverlap(startTime, endTime, otherStart, otherEnd);
    }

    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return String.format("TimeSlot[%s %s-%s, status=%s]",
                dayOfWeek, startTime, endTime, status);
    }
}
