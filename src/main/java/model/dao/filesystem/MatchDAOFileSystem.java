package model.dao.filesystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import exception.DataAccessException;
import model.dao.MatchDAO;
import model.domain.Match;
import model.domain.MatchStatus;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MatchDAOFileSystem implements MatchDAO {
    private static final String DATA_FILE = "data/matches.json";
    private final Gson gson;
    private final Type matchListType;
    private final AtomicInteger idGenerator;

    public MatchDAOFileSystem() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .setPrettyPrinting()
                .create();
        this.matchListType = new TypeToken<List<Match>>() {
        }.getType();
        ensureDataFileExists();
        this.idGenerator = new AtomicInteger(getMaxId() + 1);
    }

    private void ensureDataFileExists() {
        try {
            java.nio.file.Path path = Paths.get(DATA_FILE);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
                // Write empty JSON array
                try (FileWriter writer = new FileWriter(DATA_FILE)) {
                    writer.write("[]");
                }
            }
        } catch (IOException e) {
            throw new DataAccessException("Error creating data file: " + DATA_FILE, e);
        }
    }

    private synchronized List<Match> loadMatches() {
        try (Reader reader = new FileReader(DATA_FILE)) {
            List<Match> matches = gson.fromJson(reader, matchListType);
            return matches != null ? matches : new ArrayList<>();
        } catch (IOException e) {
            throw new DataAccessException("Error loading matches from file", e);
        }
    }

    private synchronized void saveMatches(List<Match> matches) {
        try (Writer writer = new FileWriter(DATA_FILE)) {
            gson.toJson(matches, writer);
        } catch (IOException e) {
            throw new DataAccessException("Error saving matches to file", e);
        }
    }

    private int getMaxId() {
        List<Match> matches = loadMatches();
        return matches.stream()
                .mapToInt(Match::getId)
                .max()
                .orElse(0);
    }

    @Override
    public void save(Match match) {
        List<Match> matches = loadMatches();
        if (match.getId() == 0) {
            match.setId(idGenerator.getAndIncrement());
        }
        matches.add(match);
        saveMatches(matches);
    }

    @Override
    public Match findById(int id) {
        return loadMatches().stream()
                .filter(m -> m.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Match> findByOrganizer(int organizerId) {
        return loadMatches().stream()
                .filter(m -> m.getOrganizer() != null && m.getOrganizer().getId() == organizerId)
                .toList();
    }

    @Override
    public List<Match> findPendingForManager(int managerId) {
        return loadMatches().stream()
                .filter(m -> m.getStatus() == MatchStatus.PENDING)
                .filter(m -> m.getField() != null && m.getField().getManager() != null &&
                        m.getField().getManager().getId() == managerId)
                .toList();
    }

    @Override
    public List<Match> findApprovedMatches() {
        return loadMatches().stream()
                .filter(m -> m.getStatus() == MatchStatus.APPROVED)
                .toList();
    }

    @Override
    public void updateStatus(int matchId, MatchStatus status) {
        List<Match> matches = loadMatches();
        matches.stream()
                .filter(m -> m.getId() == matchId)
                .findFirst()
                .ifPresent(m -> m.setStatus(status));
        saveMatches(matches);
    }

    @Override
    public void delete(int id) {
        List<Match> matches = loadMatches();
        matches.removeIf(m -> m.getId() == id);
        saveMatches(matches);
    }

    @Override
    public int deleteExpiredMatches() {
        List<Match> matches = loadMatches();
        LocalDate today = LocalDate.now();
        int initialSize = matches.size();
        matches.removeIf(m -> m.getDate() != null && m.getDate().isBefore(today));
        saveMatches(matches);
        return initialSize - matches.size();
    }

    @Override
    public void update(Match match) {
        List<Match> matches = loadMatches();
        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i).getId() == match.getId()) {
                matches.set(i, match);
                saveMatches(matches);
                return;
            }
        }
    }

    @Override
    public List<Match> findByJoinedPlayer(int userId) {
        return loadMatches().stream()
                .filter(m -> m.getJoinedPlayers().contains(userId))
                .toList();
    }
}