package model.bean;

import model.domain.MatchStatus;
import model.domain.Sport;

import java.time.LocalDate;
import java.time.LocalTime;

public class MatchBean {
    private int matchId;
    private Sport sport;
    private LocalDate matchDate;
    private LocalTime matchTime;
    private String city;
    private int missingPlayers;
    private int organizerId;
    private String organizerName;
    private int fieldId;
    private MatchStatus status;

    public MatchBean() {
        /* Intentionally empty */
    }

    // Getters and Setters

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDate matchDate) {
        this.matchDate = matchDate;
    }

    public LocalTime getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(LocalTime matchTime) {
        this.matchTime = matchTime;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getMissingPlayers() {
        return missingPlayers;
    }

    public void setMissingPlayers(int missingPlayers) {
        this.missingPlayers = missingPlayers;
    }

    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    private String fieldName;

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
