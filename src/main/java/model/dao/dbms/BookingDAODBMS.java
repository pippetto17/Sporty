package model.dao.dbms;

import exception.DataAccessException;
import model.dao.BookingDAO;
import model.domain.Booking;
import model.domain.BookingStatus;
import model.domain.BookingType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DBMS implementation of BookingDAO using MySQL.
 */
public class BookingDAODBMS implements BookingDAO {

    private static final String BOOKINGS_COLUMNS = "booking_id, field_id, requester_username, booking_date, start_time, end_time, type, status, total_price, requested_at, confirmed_at";
    // Common prefix for selecting all booking columns from the bookings table
    private static final String SELECT_BOOKINGS = "SELECT " + BOOKINGS_COLUMNS + " FROM bookings";

    @Override
    public void save(Booking booking) {
        String sql = """
                INSERT INTO bookings (field_id, requester_username, booking_date, start_time, end_time,
                                    type, status, total_price, requested_at, confirmed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    status = VALUES(status),
                    confirmed_at = VALUES(confirmed_at)
                """;

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, booking.getFieldId());
            stmt.setString(2, booking.getRequesterUsername());
            stmt.setDate(3, Date.valueOf(booking.getBookingDate()));
            stmt.setTime(4, Time.valueOf(booking.getStartTime()));
            stmt.setTime(5, Time.valueOf(booking.getEndTime()));
            stmt.setString(6, booking.getType().name());
            stmt.setInt(7, booking.getStatus().getCode());
            stmt.setDouble(8, booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0);
            stmt.setTimestamp(9, Timestamp.valueOf(booking.getRequestedAt()));
            stmt.setTimestamp(10,
                    booking.getConfirmedAt() != null ? Timestamp.valueOf(booking.getConfirmedAt()) : null);

            stmt.executeUpdate();

            if (booking.getBookingId() == null) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        booking.setBookingId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving booking: " + e.getMessage(), e);
        }
    }

    @Override
    public Booking findById(int bookingId) {
        String sql = SELECT_BOOKINGS + " WHERE booking_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBooking(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding booking by ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Booking> findByFieldId(String fieldId) {
        String sql = SELECT_BOOKINGS + " WHERE field_id = ? ORDER BY booking_date, start_time";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fieldId);
            return executeQuery(stmt);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding bookings by field ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Booking> findByRequesterId(String username) {
        String sql = SELECT_BOOKINGS + " WHERE requester_username = ? ORDER BY booking_date DESC, start_time";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            return executeQuery(stmt);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding bookings by requester: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Booking> findPendingByManagerId(String managerId) {
        String sql = """
                SELECT b.booking_id, b.field_id, b.requester_username, b.booking_date, b.start_time, b.end_time,
                       b.type, b.status, b.total_price, b.requested_at, b.confirmed_at
                FROM bookings b
                JOIN fields f ON b.field_id = f.field_id
                WHERE f.manager_id = ? AND b.status = 0
                ORDER BY b.requested_at ASC
                """.trim();

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, managerId);
            return executeQuery(stmt);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding pending bookings for manager: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Booking> findByStatus(BookingStatus status) {
        String sql = SELECT_BOOKINGS + " WHERE status = ? ORDER BY booking_date, start_time";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, status.getCode());
            return executeQuery(stmt);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding bookings by status: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStatus(int bookingId, BookingStatus newStatus) {
        String sql = "UPDATE bookings SET status = ?, confirmed_at = ? WHERE booking_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newStatus.getCode());
            stmt.setTimestamp(2, newStatus == BookingStatus.CONFIRMED ? Timestamp.valueOf(LocalDateTime.now()) : null);
            stmt.setInt(3, bookingId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating booking status: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int bookingId) {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting booking: " + e.getMessage(), e);
        }
    }

    // Helper methods

    private List<Booking> executeQuery(PreparedStatement stmt) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        }
        return bookings;
    }

    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setFieldId(rs.getString("field_id"));
        booking.setRequesterUsername(rs.getString("requester_username"));
        booking.setBookingDate(rs.getDate("booking_date").toLocalDate());
        booking.setStartTime(rs.getTime("start_time").toLocalTime());
        booking.setEndTime(rs.getTime("end_time").toLocalTime());
        booking.setType(BookingType.valueOf(rs.getString("type")));

        // Use domain method to set status (bypasses validation for loading from DB)
        BookingStatus status = BookingStatus.fromCode(rs.getInt("status"));
        try {
            booking.setStatus(status);
        } catch (IllegalStateException ignored) {
            // Ignore state transition errors when hydrating from persistence
        }

        double totalPrice = rs.getDouble("total_price");
        booking.setTotalPrice(rs.wasNull() ? null : totalPrice);

        Timestamp requestedAt = rs.getTimestamp("requested_at");
        if (requestedAt != null) {
            booking.setRequestedAt(requestedAt.toLocalDateTime());
        }

        Timestamp confirmedAt = rs.getTimestamp("confirmed_at");
        if (confirmedAt != null) {
            booking.setConfirmedAt(confirmedAt.toLocalDateTime());
        }


        return booking;
    }
}
