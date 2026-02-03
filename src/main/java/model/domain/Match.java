package model.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class Match {
    private int id;
    private int organizerId;
    private int fieldId;
    private LocalDate date;
    private LocalTime time;
    private int missingPlayers;
    private MatchStatus status;

    public Match() {
        this.status = MatchStatus.PENDING;
    }

    public Match(int id, int organizerId, int fieldId, LocalDate date, LocalTime time, int missingPlayers,
            MatchStatus status) {
        this.id = id;
        this.organizerId = organizerId;
        this.fieldId = fieldId;
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

    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
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