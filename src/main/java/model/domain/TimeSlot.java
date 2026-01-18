package model.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Domain class representing a recurring time slot in a field's weekly schedule.
 * Used by Field Managers to define when their fields are available.
 */
public class TimeSlot {
    private Integer slotId;
    private String fieldId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private SlotStatus status;
    private Integer bookingId; // null if available

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
        this.status = (bookingId != null) ? SlotStatus.BOOKED : SlotStatus.AVAILABLE;
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
