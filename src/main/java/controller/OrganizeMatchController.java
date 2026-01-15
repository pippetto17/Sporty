package controller;

import model.bean.MatchBean;
import model.domain.Sport;
import model.domain.User;
import model.service.MatchService;
import exception.ServiceInitializationException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Controller per l'organizzazione di un nuovo match.
 * Gestisce il flusso di creazione del match, delegando
 * la validazione e la logica di business a MatchService.
 */
public class OrganizeMatchController {
    private final User organizer;
    private final ApplicationController applicationController;
    private final MatchService matchService;
    private MatchBean currentMatchBean;

    public OrganizeMatchController(User organizer, ApplicationController applicationController) {
        this.organizer = organizer;
        this.applicationController = applicationController;
        try {
            this.matchService = new MatchService(applicationController.getPersistenceType());
        } catch (SQLException e) {
            throw new ServiceInitializationException("Errore nell'inizializzazione di MatchService: " + e.getMessage(),
                    e);
        }
    }

    /**
     * Inizializza un nuovo match associandolo all'organizzatore.
     */
    public void startNewMatch() {
        this.currentMatchBean = new MatchBean();
        this.currentMatchBean.setOrganizerUsername(organizer.getUsername());
    }

    public MatchBean getCurrentMatchBean() {
        return currentMatchBean;
    }

    /**
     * Valida i dettagli del match.
     * Delega la validazione a MatchService per mantenere la logica centralizzata.
     */
    public boolean validateMatchDetails(Sport sport, LocalDate date, LocalTime time,
            String city, int additionalParticipants) {
        return matchService.validateMatchDetails(sport, date, time, city, additionalParticipants);
    }

    /**
     * Imposta i dettagli del match dopo la validazione.
     */
    public void setMatchDetails(Sport sport, LocalDate date, LocalTime time,
            String city, int additionalParticipants) {
        currentMatchBean.setSport(sport);
        currentMatchBean.setMatchDate(date);
        currentMatchBean.setMatchTime(time);
        currentMatchBean.setCity(city);
        currentMatchBean.setRequiredParticipants(additionalParticipants);
    }

    /**
     * Procede alla selezione del campo sportivo.
     */
    public void proceedToFieldSelection() {
        applicationController.navigateToBookField(currentMatchBean);
    }

    public Sport[] getAvailableSports() {
        return Sport.values();
    }

    public User getOrganizer() {
        return organizer;
    }

    public void navigateBack() {
        applicationController.back();
    }
}
