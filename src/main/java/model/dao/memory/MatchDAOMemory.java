package model.dao.memory;

import model.dao.MatchDAO;
import model.domain.Match;
import model.domain.MatchStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchDAOMemory implements MatchDAO {
    private static final Map<Integer, Match> matches = new HashMap<>();
    private static int nextId = 1;

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

    @Override
    public boolean addParticipant(int matchId, String username) {
        Match match = findById(matchId);
        if (match == null) {
            return false;
        }
        return match.addParticipant(username);
    }

    @Override
    public boolean removeParticipant(int matchId, String username) {
        Match match = findById(matchId);
        if (match == null) {
            return false;
        }
        return match.removeParticipant(username);
    }

    @Override
    public List<String> getParticipants(int matchId) {
        Match match = findById(matchId);
        if (match == null) {
            return List.of();
        }
        return match.getParticipants();
    }

    // Metodo per testing - pulisce tutti i dati
    public static void clearAll() {
        matches.clear();
        nextId = 1;
    }
}
