package model.domain;

import exception.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Match {
    private int id;
    private User organizer;
    private Field field;
    private LocalDate date;
    private LocalTime time;
    private int missingPlayers;
    private MatchStatus status;
    private List<Integer> joinedPlayers;

    public Match() {
        this.status = MatchStatus.PENDING;
        this.joinedPlayers = new ArrayList<>();
    }

    public Match(int id, User organizer, Field field, LocalDate date, LocalTime time, int missingPlayers,
            MatchStatus status) {
        this.id = id;
        this.organizer = organizer;
        this.field = field;
        this.date = date;
        this.time = time;
        this.missingPlayers = missingPlayers;
        this.status = status;
        this.joinedPlayers = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public int getMissingPlayers() {
        return missingPlayers;
    }

    public void setMissingPlayers(int missingPlayers) throws exception.ValidationException {
        if (missingPlayers < 0) {
            throw new ValidationException("Missing players cannot be negative");
        }
        if (this.field != null && this.field.getSport() != null) {
            int requiredPlayers = this.field.getSport().getRequiredPlayers();
            if (missingPlayers >= requiredPlayers) {
                throw new ValidationException(
                        String.format("Missing players (%d) must be less than required players (%d) for %s",
                                missingPlayers, requiredPlayers, this.field.getSport().getDisplayName()));
            }
        }

        this.missingPlayers = missingPlayers;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public boolean isApproved() {
        return status == MatchStatus.APPROVED;
    }

    public List<Integer> getJoinedPlayers() {
        return joinedPlayers;
    }

    public void setJoinedPlayers(List<Integer> joinedPlayers) {
        this.joinedPlayers = joinedPlayers != null ? joinedPlayers : new ArrayList<>();
    }

    public void addJoinedPlayer(int userId) throws ValidationException {
        if (joinedPlayers.contains(userId)) {
            throw new ValidationException("User has already joined this match");
        }
        if (missingPlayers <= 0) {
            throw new ValidationException("Match is full, cannot join");
        }
        joinedPlayers.add(userId);
        setMissingPlayers(missingPlayers - 1);
    }

    public boolean isUserJoined(int userId) {
        return joinedPlayers.contains(userId);
    }

    public int getJoinedPlayersCount() {
        return joinedPlayers.size();
    }
}