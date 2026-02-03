package model.dao.memory;
import model.dao.MatchDAO;
import model.domain.Match;
import model.domain.MatchStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class MatchDAOMemory implements MatchDAO {
    private static final Map<Integer, Match> matches = new HashMap<>();
    private static int nextId = 1;
    private static synchronized int nextId() {
        return nextId++;
    }
    @Override
    public void save(Match match) {
        if (match.getId() == 0) {
            match.setId(nextId());
        }
        matches.put(match.getId(), match);
    }
    @Override
    public Match findById(int id) {
        return matches.get(id);
    }
    @Override
    public List<Match> findByOrganizer(int organizerId) {
        return matches.values().stream()
                .filter(match -> match.getOrganizerId() == organizerId)
                .toList();
    }
    @Override
    public List<Match> findPendingForManager(int managerId) {
        List<Match> pendingMatches = new ArrayList<>();
        model.dao.FieldDAO fieldDAO = new FieldDAOMemory();
        for (Match match : matches.values()) {
            if (match.getStatus() == model.domain.MatchStatus.PENDING) {
                model.domain.Field field = fieldDAO.findById(match.getFieldId());
                if (field != null && field.getManagerId() == managerId) {
                    pendingMatches.add(match);
                }
            }
        }
        return List.copyOf(pendingMatches);
    }
    @Override
    public List<Match> findApprovedMatches() {
        List<Match> approvedMatches = new ArrayList<>();
        for (Match match : matches.values()) {
            if (match.getStatus() == model.domain.MatchStatus.APPROVED) {
                approvedMatches.add(match);
            }
        }
        return List.copyOf(approvedMatches);
    }
    @Override
    public void updateStatus(int matchId, MatchStatus status) {
        Match match = matches.get(matchId);
        if (match != null) {
            match.setStatus(status);
        }
    }
    @Override
    public void delete(int id) {
        matches.remove(id);
    }
    @Override
    public int deleteExpiredMatches() {
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Integer> toDelete = matches.values().stream()
                .filter(match -> match.getDate().isBefore(today))
                .map(Match::getId)
                .toList();
        toDelete.forEach(matches::remove);
        return toDelete.size();
    }
    public static void clearAll() {
        matches.clear();
        nextId = 1;
    }
}