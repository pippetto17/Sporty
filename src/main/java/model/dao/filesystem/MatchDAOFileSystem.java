package model.dao.filesystem;

import model.dao.MatchDAO;
import model.domain.Match;
import model.domain.MatchStatus;
import java.util.List;

public class MatchDAOFileSystem implements MatchDAO {
    public MatchDAOFileSystem() {
    }

    @Override
    public void save(Match match) {
        throw new UnsupportedOperationException("Not implemented yet - GSON Hybrid Strategy Candidate");
    }

    @Override
    public Match findById(int id) {
        throw new UnsupportedOperationException("Not implemented yet - GSON Hybrid Strategy Candidate");
    }

    @Override
    public List<Match> findByOrganizer(int organizerId) {
        throw new UnsupportedOperationException("Not implemented yet - GSON Hybrid Strategy Candidate");
    }

    @Override
    public List<Match> findPendingForManager(int managerId) {
        throw new UnsupportedOperationException("Not implemented yet - GSON Hybrid Strategy Candidate");
    }

    @Override
    public List<Match> findApprovedMatches() {
        throw new UnsupportedOperationException("Not implemented yet - GSON Hybrid Strategy Candidate");
    }

    @Override
    public void updateStatus(int matchId, MatchStatus status) {
        throw new UnsupportedOperationException("Not implemented yet - GSON Hybrid Strategy Candidate");
    }

    @Override
    public void delete(int id) {
        throw new UnsupportedOperationException("Not implemented yet - GSON Hybrid Strategy Candidate");
    }

    @Override
    public int deleteExpiredMatches() {
        throw new UnsupportedOperationException("Not implemented yet - GSON Hybrid Strategy Candidate");
    }
}