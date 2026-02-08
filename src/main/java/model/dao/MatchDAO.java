package model.dao;

import model.domain.Match;
import model.domain.MatchStatus;

import java.util.List;

/**
 * Data Access Object interface for Match entities.
 * Supports match persistence, querying, and status management.
 */
public interface MatchDAO {
    /**
     * Saves a match entity (creates new or updates existing).
     *
     * @param match the match to save
     */
    void save(Match match);

    /**
     * Finds a match by its unique ID.
     *
     * @param id the match ID
     * @return the Match if found, null otherwise
     */
    Match findById(int id);

    /**
     * Finds all matches organized by a specific organizer.
     *
     * @param organizerId the organizer's user ID
     * @return list of matches organized by this user
     */
    List<Match> findByOrganizer(int organizerId);

    /**
     * Finds all pending matches for a field manager.
     *
     * @param managerId the field manager's user ID
     * @return list of pending matches for fields managed by this user
     */
    List<Match> findPendingForManager(int managerId);

    /**
     * Finds all approved matches.
     *
     * @return list of approved matches
     */
    List<Match> findApprovedMatches();

    /**
     * Updates the status of a match.
     *
     * @param matchId the match ID
     * @param status  the new status
     */
    void updateStatus(int matchId, MatchStatus status);

    /**
     * Updates an existing match entity.
     *
     * @param match the match with updated information
     */
    void update(Match match);

    /**
     * Finds all matches that a player has joined.
     *
     * @param userId the player's user ID
     * @return list of matches the player has joined
     */
    List<Match> findByJoinedPlayer(int userId);

    /**
     * Deletes a match by its ID.
     *
     * @param id the match ID
     */
    void delete(int id);

    /**
     * Deletes all expired matches from the database.
     * A match is considered expired if its date/time has passed.
     */
    void deleteExpiredMatches();
}