package model.dao;

import model.domain.SlotStatus;
import model.domain.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TimeSlotDAO {

    void save(TimeSlot slot);

    List<TimeSlot> findByFieldId(String fieldId);

    List<TimeSlot> findAvailableSlots(String fieldId, DayOfWeek day);

    List<TimeSlot> findAvailableSlotsForDate(String fieldId, LocalDate date);

    void updateStatus(int slotId, SlotStatus status);

    List<TimeSlot> findConflicting(String fieldId, DayOfWeek day, LocalTime start, LocalTime end);

    List<TimeSlot> findConflictingForDate(String fieldId, LocalDate date, LocalTime start, LocalTime end);

    void delete(int slotId);

    void deleteByFieldId(String fieldId);
}
