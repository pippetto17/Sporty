package model.dao;

import model.domain.Match;

import java.util.List;

public interface MatchDAO {
    void save(Match match);

    Match findById(int matchId);

    List<Match> findByOrganizer(String organizerUsername);

    List<Match> findAllAvailable();

    void delete(int matchId);
}
