package model.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class Match {
    private int id;
    private User organizer;
    private Field field;
    private LocalDate date;
    private LocalTime time;
    private int missingPlayers;
    private MatchStatus status;

    public Match() {
        this.status = MatchStatus.PENDING;
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

    public void setMissingPlayers(int missingPlayers) {
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
}