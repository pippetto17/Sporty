package model.dao;

import model.domain.Match;

import java.util.List;

public interface MatchDAO {
    /**
     * Save or update a match
     */
    void save(Match match);

    /**
     * Find match by ID
     * 
     * @return Match object if found, null otherwise
     */
    Match findById(int matchId);

    /**
     * Find all matches for a specific organizer
     */
    List<Match> findByOrganizer(String organizerUsername);

    /**
     * Find all available matches
     */
    List<Match> findAllAvailable();

    /**
     * Delete a match
     */
    void delete(int matchId);

    /**
     * Add a participant to a match
     * 
     * @return true if added successfully, false if match is full or participant
     *         already joined
     */
    boolean addParticipant(int matchId, String username);

    /**
     * Remove a participant from a match
     */
    boolean removeParticipant(int matchId, String username);

    /**
     * Get all participants for a match
     */
    List<String> getParticipants(int matchId);
}
