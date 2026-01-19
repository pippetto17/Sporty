package controller;

import exception.DataAccessException;
import exception.ValidationException;
import model.bean.MatchBean;
import model.domain.Sport;
import model.domain.User;

import model.utils.Utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static model.utils.Utils.ITALIAN_CITIES;

/**
 * Controller per l'organizzazione di un nuovo match.
 * Gestisce il flusso di creazione del match, con validazione e accesso ai dati.
 */
public class OrganizeMatchController {
    private final User organizer;
    private final ApplicationController applicationController;
    private final model.dao.MatchDAO matchDAO;
    private MatchBean currentMatchBean;

    // BCE Criterion 3: Preloaded data (stored after initialization)
    private List<Sport> availableSports;
    private List<String> availableCities;
    private String preferredCity;

    public OrganizeMatchController(User organizer, ApplicationController applicationController) {
        this.organizer = organizer;
        this.applicationController = applicationController;
        this.matchDAO = model.dao.DAOFactory.getMatchDAO(applicationController.getPersistenceType());
    }

    public void startNewMatch() {
        this.currentMatchBean = new MatchBean();
        this.currentMatchBean.setOrganizerUsername(organizer.getUsername());
    }

    /**
     * BCE Criterion 3: First interaction loads all required entities into memory.
     * This method MUST be called at the start of the use case.
     */
    public void initializeOrganizeMatch() {
        // 1. Load available sports
        this.availableSports = Arrays.asList(Sport.values());

        // 2. Load available cities
        this.availableCities = ITALIAN_CITIES;

        // 3. Load organizer's previous matches (for suggestions)
        List<MatchBean> previousMatches = matchDAO.findByOrganizer(organizer.getUsername())
                .stream()
                .map(model.converter.MatchConverter::toBean)
                .toList();

        // 4. Derive preferred city from history (most frequently used)
        this.preferredCity = previousMatches.stream()
                .collect(Collectors.groupingBy(MatchBean::getCity, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // 5. Start new match (as part of initialization)
        startNewMatch();
    }

    public List<Sport> getAvailableSports() {
        return availableSports != null ? availableSports : Arrays.asList(Sport.values());
    }

    public List<String> getAvailableCities() {
        return availableCities != null ? availableCities : ITALIAN_CITIES;
    }

    public String getPreferredCity() {
        return preferredCity;
    }

    public MatchBean getCurrentMatchBean() {
        return currentMatchBean;
    }

    public boolean validateMatchDetails(Sport sport, LocalDate date, LocalTime time,
            String city, int additionalParticipants) {
        if (sport == null || date == null || time == null)
            return false;
        if (city == null || city.trim().isEmpty())
            return false;
        if (date.isBefore(LocalDate.now()))
            return false;

        return sport.isValidAdditionalParticipants(additionalParticipants);
    }

    /**
     * Save match logic moved here.
     */
    public void saveMatch() throws ValidationException {
        if (currentMatchBean == null)
            throw new ValidationException("MatchBean cannot be null");
        try {
            model.domain.Match match = model.converter.MatchConverter.toEntity(currentMatchBean);
            matchDAO.save(match);
            if (match.getMatchId() != null) {
                currentMatchBean.setMatchId(match.getMatchId());
            }
        } catch (exception.DataAccessException e) {
            throw new DataAccessException("Error saving match: " + e.getMessage(), e);
        }
    }

    public void setMatchDetails(Sport sport, LocalDate date, LocalTime time,
            String city, int additionalParticipants) {
        currentMatchBean.setSport(sport);
        currentMatchBean.setMatchDate(date);
        currentMatchBean.setMatchTime(time);
        currentMatchBean.setCity(city);
        currentMatchBean.setRequiredParticipants(additionalParticipants);
    }

    public void proceedToFieldSelection() {
        // Potentially save draft state here if needed, or just navigate
        applicationController.navigateToBookField(currentMatchBean);
    }

    public Sport[] getAvailableSportsArray() {
        return Sport.values();
    }

    public java.util.List<String> getCities() {
        return ITALIAN_CITIES;
    }

    public java.util.List<String> searchCitiesByPrefix(String prefix) {
        return Utils.searchCitiesByPrefix(prefix);
    }

    public boolean isValidCity(String city) {
        return model.utils.Utils.isValidCity(city);
    }

    public User getOrganizer() {
        return organizer;
    }

    public void navigateBack() {
        applicationController.back();
    }

    // --- Parsing Methods ---

    public LocalDate parseDate(String dateStr) {
        if (dateStr == null)
            return null;
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateStr.trim(), formatter);
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    public LocalTime parseTime(String timeStr) {
        if (timeStr == null)
            return null;
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            return LocalTime.parse(timeStr.trim(), formatter);
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    // --- UI Helper Methods ---

    public String getParticipantsInfoText(Sport sport) {
        if (sport == null) {
            return model.utils.Constants.ERROR_SELECT_SPORT_FIRST;
        }
        int totalPlayers = sport.getRequiredPlayers();
        int maxAdditional = sport.getAdditionalParticipantsNeeded();
        return String.format("Need %d more players (Total: %d for %s)",
                maxAdditional, totalPlayers, sport.getDisplayName());
    }

    public int getMaxAdditionalParticipants(Sport sport) {
        if (sport == null) {
            return 1; // Default fallback
        }
        return sport.getAdditionalParticipantsNeeded();
    }
}
