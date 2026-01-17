package model.service;

import model.bean.TimeSlotBean;
import model.converter.TimeSlotConverter;
import model.dao.DAOFactory;
import model.dao.FieldDAO;
import model.dao.TimeSlotDAO;
import model.domain.Field;
import model.domain.SlotStatus;
import model.domain.TimeSlot;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing field availability and time slots.
 * Handles conflict detection and slot scheduling.
 */
public class AvailabilityService {
    private final TimeSlotDAO timeSlotDAO;
    private final FieldDAO fieldDAO;

    public AvailabilityService(DAOFactory.PersistenceType persistenceType) throws SQLException {
        this.timeSlotDAO = DAOFactory.getTimeSlotDAO(persistenceType);
        this.fieldDAO = DAOFactory.getFieldDAO(persistenceType);
    }

    /**
     * Set weekly schedule for a field.
     * Replaces existing schedule.
     */
    public void setWeeklySchedule(String fieldId, List<TimeSlotBean> schedule) {
        // Verify field exists
        Field field = fieldDAO.findById(fieldId);
        if (field == null) {
            throw new IllegalArgumentException("Field not found: " + fieldId);
        }

        // Validate no conflicts in the schedule
        for (int i = 0; i < schedule.size(); i++) {
            for (int j = i + 1; j < schedule.size(); j++) {
                TimeSlotBean slot1 = schedule.get(i);
                TimeSlotBean slot2 = schedule.get(j);

                // Check if same day and overlapping
                if (slot1.getDayOfWeek() == slot2.getDayOfWeek()) {
                    TimeSlot ts1 = TimeSlotConverter.toTimeSlot(slot1);
                    if (ts1.overlapsWith(slot2.getStartTime(), slot2.getEndTime())) {
                        throw new IllegalArgumentException(
                                String.format("Schedule conflict: slots on %s overlap", slot1.getDayOfWeek()));
                    }
                }
            }
        }

        // Delete existing schedule
        timeSlotDAO.deleteByFieldId(fieldId);

        // Save new schedule
        for (TimeSlotBean bean : schedule) {
            TimeSlot slot = TimeSlotConverter.toTimeSlot(bean);
            slot.setFieldId(fieldId);
            slot.setStatus(SlotStatus.AVAILABLE);
            timeSlotDAO.save(slot);
        }
    }

    /**
     * Get available slots for a field on a specific date.
     */
    public List<TimeSlotBean> getAvailableSlots(String fieldId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<TimeSlot> slots = timeSlotDAO.findAvailableSlots(fieldId, dayOfWeek);

        return slots.stream()
                .map(TimeSlotConverter::toTimeSlotBean)
                .toList();
    }

    /**
     * Check if a time range has conflicts with existing slots.
     * 
     * @return true if there are conflicts, false otherwise
     */
    public boolean hasConflict(String fieldId, LocalDate date, LocalTime start, LocalTime end) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<TimeSlot> conflicting = timeSlotDAO.findConflicting(fieldId, dayOfWeek, start, end);
        return !conflicting.isEmpty();
    }

    /**
     * Find alternative available slots if preferred time is not available.
     * Returns next 5 available slots on the same day or following days.
     */
    public List<TimeSlotBean> suggestAlternatives(String fieldId, LocalDate preferredDate) {
        List<TimeSlotBean> suggestions = new ArrayList<>();
        LocalDate searchDate = preferredDate;
        int daysChecked = 0;
        final int maxDays = 7; // Look ahead 7 days

        while (suggestions.size() < 5 && daysChecked < maxDays) {
            List<TimeSlotBean> daySlots = getAvailableSlots(fieldId, searchDate);
            suggestions.addAll(daySlots);

            searchDate = searchDate.plusDays(1);
            daysChecked++;
        }

        // Return max 5 suggestions
        return suggestions.size() > 5 ? suggestions.subList(0, 5) : suggestions;
    }

    /**
     * Block a slot (e.g., for maintenance).
     */
    public void blockSlot(String fieldId, LocalDate date, LocalTime start, LocalTime end) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Find slots in this time range
        List<TimeSlot> affectedSlots = timeSlotDAO.findConflicting(fieldId, dayOfWeek, start, end);

        for (TimeSlot slot : affectedSlots) {
            if (slot.getStatus() == SlotStatus.BOOKED) {
                throw new IllegalStateException(
                        "Cannot block slot that is already booked: " + slot);
            }
            slot.setStatus(SlotStatus.BLOCKED);
            timeSlotDAO.save(slot);
        }
    }

    /**
     * Unblock a slot.
     */
    public void unblockSlot(int slotId) {
        // Note: In a full implementation, we'd load the slot first
        timeSlotDAO.updateStatus(slotId, SlotStatus.AVAILABLE);
    }

    /**
     * Get all slots for a field (entire weekly schedule).
     */
    public List<TimeSlotBean> getFieldSchedule(String fieldId) {
        List<TimeSlot> slots = timeSlotDAO.findByFieldId(fieldId);

        List<TimeSlotBean> beans = new ArrayList<>();
        for (TimeSlot slot : slots) {
            TimeSlotBean bean = TimeSlotConverter.toTimeSlotBean(slot);

            // Enrich with field name
            Field field = fieldDAO.findById(fieldId);
            if (field != null) {
                bean.setFieldName(field.getName());
            }

            beans.add(bean);
        }

        return beans;
    }
}
