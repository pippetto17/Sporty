package model.dao;

import model.domain.SlotStatus;
import model.domain.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DAO interface for TimeSlot entity.
 * Handles both recurring weekly templates (bookingDate=null) and date-specific bookings.
 */
public interface TimeSlotDAO {

    /**
     * Save or update a time slot.
     */
    void save(TimeSlot slot);

    /**
     * Find all time slots for a specific field (both templates and bookings).
     */
    List<TimeSlot> findByFieldId(String fieldId);

    /**
     * Find recurring availability templates for a field on a given day.
     */
    List<TimeSlot> findAvailableSlots(String fieldId, DayOfWeek day);

    /**
     * Find available slots for a specific date (considering both templates and existing bookings).
     */
    List<TimeSlot> findAvailableSlotsForDate(String fieldId, LocalDate date);

    /**
     * Update slot status.
     */
    void updateStatus(int slotId, SlotStatus status);

    /**
     * Find slots that conflict with a time range on a specific date.
     */
    List<TimeSlot> findConflicting(String fieldId, DayOfWeek day, LocalTime start, LocalTime end);

    /**
     * Find slots that conflict with a time range on a specific date.
     */
    List<TimeSlot> findConflictingForDate(String fieldId, LocalDate date, LocalTime start, LocalTime end);

    /**
     * Delete a time slot.
     */
    void delete(int slotId);

    /**
     * Delete all time slots for a specific field.
     */
    void deleteByFieldId(String fieldId);
}
