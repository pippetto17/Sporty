package controller;

import model.bean.MatchBean;
import model.domain.Sport;
import model.domain.User;

import java.time.LocalDate;
import java.time.LocalTime;

public class OrganizeMatchController {
    private final User organizer;
    private final ApplicationController applicationController;
    private MatchBean currentMatchBean;

    public OrganizeMatchController(User organizer, ApplicationController applicationController) {
        this.organizer = organizer;
        this.applicationController = applicationController;
    }

    public void startNewMatch() {
        this.currentMatchBean = new MatchBean();
        this.currentMatchBean.setOrganizerUsername(organizer.getUsername());
    }

    public MatchBean getCurrentMatchBean() {
        return currentMatchBean;
    }

    public boolean validateMatchDetails(Sport sport, LocalDate date, LocalTime time, String city, int participants) {
        // Validazione base
        if (sport == null || date == null || time == null || city == null || city.trim().isEmpty()) {
            return false;
        }

        // Validazione data (non nel passato)
        if (date.isBefore(LocalDate.now())) {
            return false;
        }

        // Validazione partecipanti AGGIUNTIVI (l'organizer è già incluso come primo)
        // participants = numero di partecipanti aggiuntivi richiesti dall'organizer
        // Range valido: 1 <= participants <= (requiredPlayers - 1)
        if (!sport.isValidAdditionalParticipants(participants)) {
            return false;
        }

        return true;
    }

    public void setMatchDetails(Sport sport, LocalDate date, LocalTime time, String city, int participants) {
        currentMatchBean.setSport(sport);
        currentMatchBean.setMatchDate(date);
        currentMatchBean.setMatchTime(time);
        currentMatchBean.setCity(city);
        currentMatchBean.setRequiredParticipants(participants);
    }

    public void proceedToFieldSelection() {
        // Navigate to field selection view
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

