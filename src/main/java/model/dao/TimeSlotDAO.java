package model.dao;

import model.domain.SlotStatus;
import model.domain.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * DAO interface for TimeSlot entity.
 * Handles persistence of weekly recurring availability slots for fields.
 */
public interface TimeSlotDAO {

    /**
     * Save or update a time slot.
     */
    void save(TimeSlot slot);

    /**
     * Find all time slots for a specific field.
     */
    List<TimeSlot> findByFieldId(String fieldId);

    /**
     * Find all available slots for a specific field on a given day.
     */
    List<TimeSlot> findAvailableSlots(String fieldId, DayOfWeek day);

    /**
     * Update slot status.
     */
    void updateStatus(int slotId, SlotStatus status);

    /**
     * Find slots that overlap with a given time range on a specific day.
     * Used for conflict detection.
     */
    List<TimeSlot> findConflicting(String fieldId, DayOfWeek day, LocalTime start, LocalTime end);

    /**
     * Delete a time slot.
     */
    void delete(int slotId);

    /**
     * Delete all time slots for a specific field.
     */
    void deleteByFieldId(String fieldId);
}
