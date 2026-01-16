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
}
