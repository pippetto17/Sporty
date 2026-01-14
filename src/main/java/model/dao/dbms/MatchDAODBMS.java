package model.dao.dbms;

import model.dao.MatchDAO;
import model.domain.Match;
import model.domain.MatchStatus;
import model.domain.Sport;

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
                                required_participants, field_id, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                sport = VALUES(sport),
                match_date = VALUES(match_date),
                match_time = VALUES(match_time),
                city = VALUES(city),
                required_participants = VALUES(required_participants),
                field_id = VALUES(field_id),
                status = VALUES(status)
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
            stmt.executeUpdate();

            // Se è un nuovo match, recupera l'ID generato
            if (match.getMatchId() == null) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        match.setMatchId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving match: " + e.getMessage(), e);
        }
    }

    @Override
    public Match findById(int matchId) {
        String sql = "SELECT * FROM matches WHERE match_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractMatchFromResultSet(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding match: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Match> findByOrganizer(String organizerUsername) {
        String sql = "SELECT * FROM matches WHERE organizer_username = ? ORDER BY match_date DESC, match_time DESC";
        List<Match> matches = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, organizerUsername);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                matches.add(extractMatchFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding matches by organizer: " + e.getMessage(), e);
        }

        return matches;
    }

    @Override
    public List<Match> findAllAvailable() {
        String sql = "SELECT * FROM matches WHERE status = ? AND match_date >= CURDATE() ORDER BY match_date, match_time";
        List<Match> matches = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, MatchStatus.CONFIRMED.getCode());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                matches.add(extractMatchFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding available matches: " + e.getMessage(), e);
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
            throw new RuntimeException("Error deleting match: " + e.getMessage(), e);
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

        return match;
    }
}

