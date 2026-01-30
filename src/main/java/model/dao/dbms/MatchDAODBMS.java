package model.dao.dbms;

import model.dao.MatchDAO;
import model.domain.Match;
import model.domain.MatchStatus;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import java.sql.Connection;
import java.util.List;

public class MatchDAODBMS implements MatchDAO {
    private final Connection connection;

    public MatchDAODBMS(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Match match) {
        String query = "INSERT INTO matches (organizer_id, field_id, date, time, missing_players, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, match.getOrganizerId());
            stmt.setInt(2, match.getFieldId());
            stmt.setDate(3, java.sql.Date.valueOf(match.getDate()));
            stmt.setTime(4, java.sql.Time.valueOf(match.getTime()));
            stmt.setInt(5, match.getMissingPlayers());
            stmt.setInt(6, match.getStatus().getCode());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        match.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error saving match", e);
        }
    }

    @Override
    public Match findById(int id) {
        String query = "SELECT id, organizer_id, field_id, date, time, missing_players, status FROM matches WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMatch(rs);
                }
            }
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error finding match by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Match> findByOrganizer(int organizerId) {
        List<Match> matches = new ArrayList<>();
        String query = "SELECT id, organizer_id, field_id, date, time, missing_players, status FROM matches WHERE organizer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, organizerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapRowToMatch(rs));
                }
            }
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error finding matches by organizer: " + organizerId, e);
        }
        return matches;
    }

    @Override
    public List<Match> findPendingForManager(int managerId) {
        List<Match> matches = new ArrayList<>();
        String query = "SELECT m.id, m.organizer_id, m.field_id, m.date, m.time, m.missing_players, m.status FROM matches m " +
                "JOIN field f ON m.field_id = f.id " +
                "WHERE f.manager_id = ? AND m.status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, managerId);
            stmt.setInt(2, model.domain.MatchStatus.PENDING.getCode());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapRowToMatch(rs));
                }
            }
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error finding pending matches for manager: " + managerId, e);
        }
        return matches;
    }

    @Override
    public void updateStatus(int matchId, MatchStatus status) {
        String query = "UPDATE matches SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, status.getCode());
            stmt.setInt(2, matchId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error updating match status for match id: " + matchId, e);
        }
    }

    @Override
    public List<Match> findApprovedMatches() {
        List<Match> matches = new ArrayList<>();
        String query = "SELECT id, organizer_id, field_id, date, time, missing_players, status FROM matches WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, model.domain.MatchStatus.APPROVED.getCode());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapRowToMatch(rs));
                }
            }
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error finding approved matches", e);
        }
        return matches;
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM matches WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new exception.DataAccessException("Error deleting match with id: " + id, e);
        }
    }

    private Match mapRowToMatch(ResultSet rs) throws SQLException {
        return new Match(
                rs.getInt("id"),
                rs.getInt("organizer_id"),
                rs.getInt("field_id"),
                rs.getDate("date").toLocalDate(),
                rs.getTime("time").toLocalTime(),
                rs.getInt("missing_players"),
                model.domain.MatchStatus.fromCode(rs.getInt("status")));
    }
}
