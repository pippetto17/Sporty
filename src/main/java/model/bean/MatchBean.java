package model.bean;

import model.domain.Sport;
import model.domain.MatchStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MatchBean {
    private Integer matchId;
    private Sport sport;
    private LocalDate matchDate;
    private LocalTime matchTime;
    private String city;
    private int requiredParticipants;
    private String organizerUsername;
    private String fieldId;
    private Double pricePerPerson;
    private MatchStatus status;
    private List<String> participants;

    public MatchBean() {
        this.participants = new ArrayList<>();
    }

    public MatchBean(Sport sport, LocalDate matchDate, LocalTime matchTime, String city, int requiredParticipants) {
        this.sport = sport;
        this.matchDate = matchDate;
        this.matchTime = matchTime;
        this.city = city;
        this.requiredParticipants = requiredParticipants;
        this.participants = new ArrayList<>();
    }

    // Getters and Setters
    public Integer getMatchId() {
        return matchId;
    }

    public void setMatchId(Integer matchId) {
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

    public int getRequiredParticipants() {
        return requiredParticipants;
    }

    public void setRequiredParticipants(int requiredParticipants) {
        this.requiredParticipants = requiredParticipants;
    }

    public String getOrganizerUsername() {
        return organizerUsername;
    }

    public void setOrganizerUsername(String organizerUsername) {
        this.organizerUsername = organizerUsername;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public Double getPricePerPerson() {
        return pricePerPerson;
    }

    public void setPricePerPerson(Double pricePerPerson) {
        this.pricePerPerson = pricePerPerson;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }
}
