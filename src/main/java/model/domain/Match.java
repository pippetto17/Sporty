package model.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Match {
    private Integer matchId;
    private Sport sport;
    private LocalDate matchDate;
    private LocalTime matchTime;
    private String city;
    private int requiredParticipants;
    private String organizerUsername;
    private String fieldId; // Will be set after field selection
    private Integer bookingId; // Link to Booking entity if field was booked
    private Double pricePerPerson; // Calculated based on selected field
    private MatchStatus status; // DRAFT, CONFIRMED, CANCELLED
    private List<String> participants; // Usernames of players who joined

    public Match() {
        this.status = MatchStatus.DRAFT;
        this.participants = new ArrayList<>();
    }

    public Match(Sport sport, LocalDate matchDate, LocalTime matchTime, String city,
            int requiredParticipants, String organizerUsername) {
        this.sport = sport;
        this.matchDate = matchDate;
        this.matchTime = matchTime;
        this.city = city;
        this.requiredParticipants = requiredParticipants;
        this.organizerUsername = organizerUsername;
        this.status = MatchStatus.DRAFT;
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

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public Double getPricePerPerson() {
        return pricePerPerson;
    }

    public void setPricePerPerson(Double pricePerPerson) {
        this.pricePerPerson = pricePerPerson;
    }

    public List<String> getParticipants() {
        return new ArrayList<>(participants); // Return copy for encapsulation
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants != null ? new ArrayList<>(participants) : new ArrayList<>();
    }

    // Participant management methods
    public boolean addParticipant(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (participants.contains(username)) {
            return false; // Already joined
        }
        if (isFull()) {
            return false; // Match is full
        }
        return participants.add(username);
    }

    public boolean removeParticipant(String username) {
        return participants.remove(username);
    }

    public int getParticipantCount() {
        return participants.size();
    }

    public boolean isFull() {
        return participants.size() >= requiredParticipants;
    }

    public boolean hasParticipant(String username) {
        return participants.contains(username);
    }

    // Behavioral methods - operations instead of just getters
    public boolean isDraft() {
        return this.status == MatchStatus.DRAFT;
    }

    public boolean isConfirmed() {
        return this.status == MatchStatus.CONFIRMED;
    }

    public boolean isCancelled() {
        return this.status == MatchStatus.CANCELLED;
    }

    public boolean canBeModified() {
        return this.status == MatchStatus.DRAFT;
    }

    public boolean hasField() {
        return this.fieldId != null && !this.fieldId.isEmpty();
    }

    public void calculatePricePerPerson(double fieldPricePerHour, double hoursBooked) {
        if (requiredParticipants <= 0) {
            throw new IllegalStateException("Cannot calculate price: invalid participants count");
        }
        this.pricePerPerson = (fieldPricePerHour * hoursBooked) / requiredParticipants;
    }

    public void confirm() {
        if (!hasField()) {
            throw new IllegalStateException("Cannot confirm match without a field");
        }
        this.status = MatchStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == MatchStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel a confirmed match directly");
        }
        this.status = MatchStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return String.format("Match %d - %s - %s at %s - %s (%d players) - Status: %s",
                matchId != null ? matchId : 0,
                sport.getDisplayName(),
                matchDate,
                matchTime,
                city,
                requiredParticipants,
                status);
    }
}
