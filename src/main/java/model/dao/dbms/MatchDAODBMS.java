package model.dao.dbms;

import exception.DataAccessException;
import model.dao.MatchDAO;
import model.domain.Field;
import model.domain.Match;
import model.domain.MatchStatus;
import model.domain.User;
import model.utils.JsonUtils;

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
        String query = "INSERT INTO matches (organizer_id, field_id, date, time, missing_players, status, joined_players) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, match.getOrganizer() != null ? match.getOrganizer().getId() : 0);
            stmt.setInt(2, match.getField() != null ? match.getField().getId() : 0);
            stmt.setDate(3, java.sql.Date.valueOf(match.getDate()));
            stmt.setTime(4, java.sql.Time.valueOf(match.getTime()));
            stmt.setInt(5, match.getMissingPlayers());
            stmt.setInt(6, match.getStatus().getCode());
            stmt.setString(7, JsonUtils.listToJson(match.getJoinedPlayers()));
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        match.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving match", e);
        }
    }

    @Override
    public Match findById(int id) {
        String query = "SELECT id, organizer_id, field_id, date, time, missing_players, status, joined_players FROM matches WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMatch(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding match by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Match> findByOrganizer(int organizerId) {
        List<Match> matches = new ArrayList<>();
        String query = "SELECT id, organizer_id, field_id, date, time, missing_players, status, joined_players FROM matches WHERE organizer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, organizerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapRowToMatch(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding matches by organizer: " + organizerId, e);
        }
        return matches;
    }

    @Override
    public List<Match> findPendingForManager(int managerId) {
        List<Match> matches = new ArrayList<>();
        String query = "SELECT m.id, m.organizer_id, m.field_id, m.date, m.time, m.missing_players, m.status, m.joined_players FROM matches m "
                +
                "JOIN field f ON m.field_id = f.id " +
                "WHERE f.manager_id = ? AND m.status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, managerId);
            stmt.setInt(2, MatchStatus.PENDING.getCode());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapRowToMatch(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding pending matches for manager: " + managerId, e);
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
            throw new DataAccessException("Error updating match status for match id: " + matchId, e);
        }
    }

    @Override
    public void update(Match match) {
        String query = "UPDATE matches SET missing_players = ?, status = ?, joined_players = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, match.getMissingPlayers());
            stmt.setInt(2, match.getStatus().getCode());
            stmt.setString(3, JsonUtils.listToJson(match.getJoinedPlayers()));
            stmt.setInt(4, match.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating match with id: " + match.getId(), e);
        }
    }

    @Override
    public List<Match> findByJoinedPlayer(int userId) {
        List<Match> matches = new ArrayList<>();
        // Use LIKE for broader MySQL compatibility (works even without JSON functions)
        String query = "SELECT id, organizer_id, field_id, date, time, missing_players, status, joined_players " +
                "FROM matches WHERE joined_players LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            // Search for the user ID as a number in the JSON array
            String searchPattern = "%" + userId + "%";
            stmt.setString(1, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Match match = mapRowToMatch(rs);
                    // Double-check that the user is actually in the list (not a substring match)
                    if (match.getJoinedPlayers().contains(userId)) {
                        matches.add(match);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding matches by joined player: " + userId, e);
        }
        return matches;
    }

    @Override
    public List<Match> findApprovedMatches() {
        List<Match> matches = new ArrayList<>();
        String query = "SELECT id, organizer_id, field_id, date, time, missing_players, status, joined_players FROM matches WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, MatchStatus.APPROVED.getCode());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapRowToMatch(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding approved matches", e);
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
            throw new DataAccessException("Error deleting match with id: " + id, e);
        }
    }

    @Override
    public void deleteExpiredMatches() {
        String query = "DELETE FROM matches WHERE date < CURDATE()";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting expired matches", e);
        }
    }

    private Match mapRowToMatch(ResultSet rs) throws SQLException {

        int organizerId = rs.getInt("organizer_id");
        int fieldId = rs.getInt("field_id");

        UserDAODBMS userDAO = new UserDAODBMS(connection);
        FieldDAODBMS fieldDAO = new FieldDAODBMS(connection);

        User organizer = userDAO.findById(organizerId);
        Field field = fieldDAO.findById(fieldId);

        Match match = new Match(
                rs.getInt("id"),
                organizer,
                field,
                rs.getDate("date").toLocalDate(),
                rs.getTime("time").toLocalTime(),
                rs.getInt("missing_players"),
                MatchStatus.fromCode(rs.getInt("status")));

        // Parse joined_players from JSON
        String joinedPlayersJson = rs.getString("joined_players");
        if (joinedPlayersJson != null) {
            match.setJoinedPlayers(JsonUtils.jsonToList(joinedPlayersJson));
        }

        return match;
    }
}