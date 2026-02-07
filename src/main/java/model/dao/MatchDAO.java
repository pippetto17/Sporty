package model.dao;

import model.domain.Match;

import java.util.List;

public interface MatchDAO {
    void save(Match match);

    Match findById(int id);

    List<Match> findByOrganizer(int organizerId);

    List<Match> findPendingForManager(int managerId);

    List<Match> findApprovedMatches();

    void updateStatus(int matchId, model.domain.MatchStatus status);

    void update(Match match);

    List<Match> findByJoinedPlayer(int userId);

    void delete(int id);

    int deleteExpiredMatches();
}