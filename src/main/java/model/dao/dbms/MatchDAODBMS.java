package model.dao.dbms;

import exception.DataAccessException;
import model.dao.MatchDAO;
import model.domain.Match;
import model.domain.MatchStatus;
import model.domain.Sport;
import model.utils.Constants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchDAODBMS implements MatchDAO {
    private final Connection connection;

    public MatchDAODBMS(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Match match) {
        String sql = """
                INSERT INTO matches (organizer_username, sport, match_date, match_time, city,
                                    required_participants, field_id, status, participants, booking_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    sport = VALUES(sport),
                    match_date = VALUES(match_date),
                    match_time = VALUES(match_time),
                    city = VALUES(city),
                    required_participants = VALUES(required_participants),
                    field_id = VALUES(field_id),
                    status = VALUES(status),
                    participants = VALUES(participants),
                    booking_id = VALUES(booking_id)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, match.getOrganizerUsername());
            stmt.setInt(2, match.getSport().ordinal());
            stmt.setDate(3, Date.valueOf(match.getMatchDate()));
            stmt.setTime(4, Time.valueOf(match.getMatchTime()));
            stmt.setString(5, match.getCity());
            stmt.setInt(6, match.getRequiredParticipants());

            if (match.getFieldId() != null && !match.getFieldId().isEmpty()) {
                stmt.setString(7, match.getFieldId());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }

            stmt.setInt(8, match.getStatus().getCode());

            String participantsJson = serializeParticipantsToJson(match.getParticipants());
            stmt.setString(9, participantsJson);

            if (match.getBookingId() != null) {
                stmt.setInt(10, match.getBookingId());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }

            stmt.executeUpdate();

            if (match.getMatchId() == null) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        match.setMatchId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_SAVING_MATCH + e.getMessage(), e);
        }
    }

    @Override
    public Match findById(int matchId) {
        String sql = """
                SELECT match_id, organizer_username, sport, match_date, match_time,
                       city, required_participants, field_id, status, participants, booking_id
                FROM matches
                WHERE match_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractMatchFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_FINDING_MATCH_BY_ID + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Match> findByOrganizer(String organizerUsername) {
        String sql = "SELECT match_id, organizer_username, sport, match_date, match_time, " +
                "city, required_participants, field_id, status, participants, booking_id FROM matches " +
                "WHERE organizer_username = ? ORDER BY match_date DESC, match_time DESC";
        List<Match> matches = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, organizerUsername);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                matches.add(extractMatchFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_FINDING_MATCHES_BY_ORGANIZER + e.getMessage(), e);
        }
        return matches;
    }

    @Override
    public List<Match> findAllAvailable() {
        String sql = "SELECT match_id, organizer_username, sport, match_date, match_time, " +
                "city, required_participants, field_id, status, participants, booking_id FROM matches " +
                "WHERE status = ? AND match_date >= CURDATE() ORDER BY match_date, match_time";
        List<Match> matches = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, MatchStatus.CONFIRMED.getCode());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                matches.add(extractMatchFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_FINDING_AVAILABLE_MATCHES + e.getMessage(), e);
        }
        return matches;
    }

    @Override
    public void delete(int matchId) {
        String sql = "DELETE FROM matches WHERE match_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_DELETING_MATCH + e.getMessage(), e);
        }
    }

    private Match extractMatchFromResultSet(ResultSet rs) throws SQLException {
        Match match = new Match();
        match.setMatchId(rs.getInt("match_id"));
        match.setOrganizerUsername(rs.getString("organizer_username"));
        match.setSport(Sport.values()[rs.getInt("sport")]);

        Date sqlDate = rs.getDate("match_date");
        match.setMatchDate(sqlDate != null ? sqlDate.toLocalDate() : null);

        Time sqlTime = rs.getTime("match_time");
        match.setMatchTime(sqlTime != null ? sqlTime.toLocalTime() : null);

        match.setCity(rs.getString("city"));
        match.setRequiredParticipants(rs.getInt("required_participants"));

        String fieldId = rs.getString("field_id");
        match.setFieldId(fieldId);

        match.setStatus(MatchStatus.fromCode(rs.getInt("status")));

        String participantsJson = rs.getString("participants");
        List<String> participants = deserializeParticipantsFromJson(participantsJson);
        match.setParticipants(participants);

        int bookingId = rs.getInt("booking_id");
        if (!rs.wasNull()) {
            match.setBookingId(bookingId);
        }

        return match;
    }

    /**
     * Serialize a list of participants to JSON array format.
     * Example: ["user1", "user2", "user3"]
     */
    private String serializeParticipantsToJson(List<String> participants) {
        if (participants == null || participants.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < participants.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\"").append(escapeJson(participants.get(i))).append("\"");
        }
        json.append("]");
        return json.toString();
    }

    /**
     * Deserialize JSON array to list of participants.
     * Example: ["user1", "user2"] -> List.of("user1", "user2")
     */
    private List<String> deserializeParticipantsFromJson(String json) {
        if (json == null || json.trim().isEmpty() || "[]".equals(json.trim())) {
            return new ArrayList<>();
        }

        List<String> participants = new ArrayList<>();
        // Remove brackets and split by comma
        String content = json.trim().substring(1, json.trim().length() - 1);
        if (content.isEmpty()) {
            return participants;
        }

        String[] parts = content.split(",");
        for (String part : parts) {
            // Remove quotes and whitespace
            String username = part.trim().replaceAll("(^\")|(\"$)", "");
            if (!username.isEmpty()) {
                participants.add(username);
            }
        }
        return participants;
    }

    /**
     * Escape special JSON characters in a string.
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
