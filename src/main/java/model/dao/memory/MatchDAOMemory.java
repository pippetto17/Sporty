package model.dao.memory;

import model.dao.MatchDAO;
import model.domain.Match;
import model.domain.MatchStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchDAOMemory implements MatchDAO {
    private final Map<Integer, Match> matches = new HashMap<>();
    private int nextId = 1;

    @Override
    public void save(Match match) {
        if (match.getMatchId() == null) {
            match.setMatchId(nextId++);
        }
        matches.put(match.getMatchId(), match);
    }

    @Override
    public Match findById(int matchId) {
        return matches.get(matchId);
    }

    @Override
    public List<Match> findByOrganizer(String organizerUsername) {
        return matches.values().stream()
                .filter(match -> match.getOrganizerUsername().equals(organizerUsername))
                .toList();
    }

    @Override
    public List<Match> findAllAvailable() {
        return matches.values().stream()
                .filter(match -> match.getStatus() == MatchStatus.CONFIRMED)
                .toList();
    }

    @Override
    public void delete(int matchId) {
        matches.remove(matchId);
    }

    // Metodo per testing - pulisce tutti i dati
    public void clearAll() {
        matches.clear();
        nextId = 1;
    }
}
