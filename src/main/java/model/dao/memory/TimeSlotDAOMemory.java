package model.dao.memory;

import model.dao.TimeSlotDAO;
import model.domain.SlotStatus;
import model.domain.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory implementation of TimeSlotDAO for testing.
 */
public class TimeSlotDAOMemory implements TimeSlotDAO {
    private final Map<Integer, TimeSlot> timeSlots = new HashMap<>();
    private int nextId = 1;

    @Override
    public void save(TimeSlot slot) {
        if (slot.getSlotId() == null) {
            slot.setSlotId(nextId++);
        }
        timeSlots.put(slot.getSlotId(), slot);
    }

    @Override
    public List<TimeSlot> findByFieldId(String fieldId) {
        return timeSlots.values().stream()
                .filter(slot -> slot.getFieldId().equals(fieldId))
                .toList();
    }

    @Override
    public List<TimeSlot> findAvailableSlots(String fieldId, DayOfWeek day) {
        return timeSlots.values().stream()
                .filter(slot -> slot.getFieldId().equals(fieldId))
                .filter(slot -> slot.getDayOfWeek() == day)
                .filter(TimeSlot::isAvailable)
                .toList();
    }

    @Override
    public void updateStatus(int slotId, SlotStatus status) {
        TimeSlot slot = timeSlots.get(slotId);
        if (slot != null) {
            slot.setStatus(status);
        }
    }

    @Override
    public List<TimeSlot> findConflicting(String fieldId, DayOfWeek day, LocalTime start, LocalTime end) {
        return timeSlots.values().stream()
                .filter(slot -> slot.getFieldId().equals(fieldId))
                .filter(slot -> slot.getDayOfWeek() == day)
                .filter(slot -> slot.overlapsWith(start, end))
                .toList();
    }

    @Override
    public void delete(int slotId) {
        timeSlots.remove(slotId);
    }

    @Override
    public void deleteByFieldId(String fieldId) {
        timeSlots.entrySet().removeIf(entry -> entry.getValue().getFieldId().equals(fieldId));
    }

    // Testing utility
    public void clearAll() {
        timeSlots.clear();
        nextId = 1;
    }
}
