package controller;

import exception.AuthorizationException;
import exception.DataAccessException;
import exception.ValidationException;
import model.bean.MatchBean;
import model.domain.Sport;
import model.domain.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static model.utils.MapsAPI.ITALIAN_CITIES;

/**
 * Controller for organizing new matches.
 * Handles the workflow of creating a match, including field selection,
 * date/time scheduling, and payment processing.
 */
public class OrganizeMatchController {
    private final User organizer;
    private final ApplicationController applicationController;
    private final model.dao.MatchDAO matchDAO;
    private final model.dao.FieldDAO fieldDAO;
    private MatchBean currentMatchBean;
    private String preferredCity;

    /**
     * Constructs a new OrganizeMatchController for the given organizer.
     *
     * @param organizer             the user organizing the match
     * @param applicationController the main application controller
     * @throws AuthorizationException if the user is not an organizer
     */
    public OrganizeMatchController(User organizer, ApplicationController applicationController)
            throws AuthorizationException {
        if (!organizer.isOrganizer()) {
            throw new AuthorizationException("User must be an organizer");
        }
        this.organizer = organizer;
        this.applicationController = applicationController;
        this.matchDAO = applicationController.getDaoFactory().getMatchDAO();
        this.fieldDAO = applicationController.getDaoFactory().getFieldDAO();
        this.currentMatchBean = new MatchBean();
        this.currentMatchBean.setOrganizerId(organizer.getId());
        this.currentMatchBean.setOrganizerName(organizer.getName() + " " + organizer.getSurname());
    }

    /**
     * Initializes the match organization workflow.
     * Determines the organizer's preferred city based on previous matches.
     */
    public void initializeOrganizeMatch() {
        try {
            List<MatchBean> previousMatches = matchDAO.findByOrganizer(organizer.getId())
                    .stream()
                    .map(model.converter.MatchConverter::toBean)
                    .toList();
            for (MatchBean mb : previousMatches) {
                model.domain.Field f = fieldDAO.findById(mb.getFieldId());
                if (f != null) {
                    mb.setCity(f.getCity());
                }
            }
            this.preferredCity = previousMatches.stream()
                    .filter(m -> m.getCity() != null)
                    .collect(Collectors.groupingBy(MatchBean::getCity, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        } catch (DataAccessException e) {
            this.preferredCity = null;
            throw new DataAccessException("Failed to initialize organize match data: " + e.getMessage(), e);
        }
    }

    public void startNewMatch() {
        this.currentMatchBean = new MatchBean();
        this.currentMatchBean.setOrganizerId(organizer.getId());
        this.currentMatchBean.setOrganizerName(organizer.getName() + " " + organizer.getSurname());
        initializeOrganizeMatch();
    }

    public List<Sport> getAvailableSports() {
        return List.of(Sport.values());
    }

    public List<String> getAvailableCities() {
        return ITALIAN_CITIES;
    }

    public String getPreferredCity() {
        return preferredCity;
    }

    public MatchBean getCurrentMatchBean() {
        return currentMatchBean;
    }

    public void validateMatchDetails(Sport sport, LocalDate date, LocalTime time,
            String city, int additionalParticipants) throws ValidationException {
        if (sport == null)
            throw new ValidationException(model.utils.Constants.ERROR_SPORT_REQUIRED);
        if (date == null)
            throw new ValidationException(model.utils.Constants.ERROR_DATE_REQUIRED);
        if (time == null)
            throw new ValidationException(model.utils.Constants.ERROR_TIME_REQUIRED);
        if (city == null || city.trim().isEmpty())
            throw new ValidationException(model.utils.Constants.ERROR_CITY_REQUIRED);
        if (!isValidCity(city))
            throw new ValidationException(model.utils.Constants.ERROR_CITY_INVALID);
        if (date.isBefore(LocalDate.now()))
            throw new ValidationException(model.utils.Constants.ERROR_DATE_IN_PAST);
        if (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now()))
            throw new ValidationException(model.utils.Constants.ERROR_TIME_IN_PAST);
        if (!sport.isValidAdditionalParticipants(additionalParticipants)) {
            throw new ValidationException(model.utils.Constants.ERROR_INVALID_PARTICIPANTS + sport.getDisplayName());
        }
    }

    public void saveMatch() throws ValidationException {
        if (currentMatchBean == null)
            throw new ValidationException("MatchBean cannot be null");
        try {
            model.domain.Match match = model.converter.MatchConverter.toEntity(currentMatchBean);

            // Populate full Field and User entities from DAOs
            if (currentMatchBean.getFieldId() != 0) {
                model.domain.Field fullField = fieldDAO.findById(currentMatchBean.getFieldId());
                match.setField(fullField);
            }
            if (currentMatchBean.getOrganizerId() != 0) {
                match.setOrganizer(organizer);
            }

            matchDAO.save(match);
            if (match.getId() != 0) {
                currentMatchBean.setMatchId(match.getId());
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
        currentMatchBean.setMissingPlayers(additionalParticipants);
    }

    public void proceedToFieldSelection() {
        applicationController.navigateToBookField(currentMatchBean);
    }

    public boolean isValidCity(String city) {
        return model.utils.MapsAPI.isValidCity(city);
    }

    public User getOrganizer() {
        return organizer;
    }

    public void navigateBack() {
        applicationController.back();
    }

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
            return 1;
        }
        return sport.getAdditionalParticipantsNeeded();
    }

    public boolean isDateValid(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }
}