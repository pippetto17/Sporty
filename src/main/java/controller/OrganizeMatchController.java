package controller;
package controller;

import model.bean.MatchBean;
import model.domain.Sport;
import model.domain.User;
import model.utils.Constants;
import exception.DataAccessException;
import exception.ValidationException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Controller per l'organizzazione di un nuovo match.
 * Gestisce il flusso di creazione del match, con validazione e accesso ai dati.
 */
public class OrganizeMatchController {
    private final User organizer;
    private final ApplicationController applicationController;
    private final model.dao.MatchDAO matchDAO;
    private MatchBean currentMatchBean;

    public OrganizeMatchController(User organizer, ApplicationController applicationController) {
        this.organizer = organizer;
        this.applicationController = applicationController;
        try {
            this.matchDAO = model.dao.DAOFactory.getMatchDAO(applicationController.getPersistenceType());
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_DAO_INIT + e.getMessage(), e);
        }
    }

    public void startNewMatch() {
        this.currentMatchBean = new MatchBean();
        this.currentMatchBean.setOrganizerUsername(organizer.getUsername());
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

    public Sport[] getAvailableSports() {
        return Sport.values();
    }

    public java.util.List<String> getCities() {
        return model.utils.ItalianCities.CITIES;
    }

    public java.util.List<String> searchCitiesByPrefix(String prefix) {
        return model.utils.ItalianCities.searchByPrefix(prefix);
    }

    public boolean isValidCity(String city) {
        return model.utils.ItalianCities.isValidCity(city);
    }

    public User getOrganizer() {
        return organizer;
    }

    public void navigateBack() {
        applicationController.back();
    }
}
