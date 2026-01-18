package model.dao.dbms;

import exception.DataAccessException;
import model.dao.TimeSlotDAO;
import model.domain.SlotStatus;
import model.domain.TimeSlot;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DBMS implementation of TimeSlotDAO using MySQL.
 */
public class TimeSlotDAODBMS implements TimeSlotDAO {

    private static final String TIME_SLOTS_COLUMNS = "slot_id, field_id, day_of_week, start_time, end_time, status, booking_id";

    @Override
    public void save(TimeSlot slot) {
        String sql = """
                INSERT INTO time_slots (field_id, day_of_week, start_time, end_time, status, booking_id)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    status = VALUES(status),
                    booking_id = VALUES(booking_id)
                """;

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, slot.getFieldId());
            stmt.setInt(2, slot.getDayOfWeek().getValue());
            stmt.setTime(3, Time.valueOf(slot.getStartTime()));
            stmt.setTime(4, Time.valueOf(slot.getEndTime()));
            stmt.setString(5, slot.getStatus().name());

            if (slot.getBookingId() != null) {
                stmt.setInt(6, slot.getBookingId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.executeUpdate();

            if (slot.getSlotId() == null) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        slot.setSlotId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving time slot: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TimeSlot> findByFieldId(String fieldId) {
        String sql = "SELECT " + TIME_SLOTS_COLUMNS + " FROM time_slots WHERE field_id = ? ORDER BY day_of_week, start_time";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fieldId);
            return executeQuery(stmt);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding time slots by field ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TimeSlot> findAvailableSlots(String fieldId, DayOfWeek day) {
        String sql = """
                SELECT %s FROM time_slots
                WHERE field_id = ? AND day_of_week = ? AND status = 'AVAILABLE'
                ORDER BY start_time
                """.formatted(TIME_SLOTS_COLUMNS);

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fieldId);
            stmt.setInt(2, day.getValue());
            return executeQuery(stmt);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding available slots: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStatus(int slotId, SlotStatus status) {
        String sql = "UPDATE time_slots SET status = ? WHERE slot_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, slotId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating slot status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TimeSlot> findConflicting(String fieldId, DayOfWeek day, LocalTime start, LocalTime end) {
        String sql = """
                SELECT %s FROM time_slots
                WHERE field_id = ? AND day_of_week = ?
                AND start_time < ? AND end_time > ?
                """.formatted(TIME_SLOTS_COLUMNS);

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fieldId);
            stmt.setInt(2, day.getValue());
            stmt.setTime(3, Time.valueOf(end));
            stmt.setTime(4, Time.valueOf(start));
            return executeQuery(stmt);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding conflicting slots: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int slotId) {
        String sql = "DELETE FROM time_slots WHERE slot_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, slotId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting time slot: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteByFieldId(String fieldId) {
        String sql = "DELETE FROM time_slots WHERE field_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fieldId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting time slots for field: " + e.getMessage(), e);
        }
    }

    // Helper methods

    private List<TimeSlot> executeQuery(PreparedStatement stmt) throws SQLException {
        List<TimeSlot> slots = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                slots.add(mapResultSetToTimeSlot(rs));
            }
        }
        return slots;
    }

    private TimeSlot mapResultSetToTimeSlot(ResultSet rs) throws SQLException {
        TimeSlot slot = new TimeSlot();
        slot.setSlotId(rs.getInt("slot_id"));
        slot.setFieldId(rs.getString("field_id"));
        slot.setDayOfWeek(DayOfWeek.of(rs.getInt("day_of_week")));
        slot.setStartTime(rs.getTime("start_time").toLocalTime());
        slot.setEndTime(rs.getTime("end_time").toLocalTime());
        slot.setStatus(SlotStatus.valueOf(rs.getString("status")));

        int bookingId = rs.getInt("booking_id");
        if (!rs.wasNull()) {
            slot.setBookingId(bookingId);
        }

        return slot;
    }
}
