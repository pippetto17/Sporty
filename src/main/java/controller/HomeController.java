package controller;

import model.bean.MatchBean;
import model.domain.User;
import model.domain.Role;
import model.domain.Sport;
import model.service.MatchService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class HomeController {
    private final User currentUser;
    private final ApplicationController applicationController;
    private final MatchService matchService;
    private boolean viewAsPlayer; // Toggle between organizer and player view

    public HomeController(User user, ApplicationController applicationController, MatchService matchService) {
        this.currentUser = user;
        this.applicationController = applicationController;
        this.matchService = matchService;
        // Default view based on user role
        this.viewAsPlayer = !user.isOrganizer();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Role getUserRole() {
        return Role.fromCode(currentUser.getRole());
    }

    public boolean isOrganizer() {
        return currentUser.isOrganizer();
    }

    public boolean isPlayer() {
        return currentUser.isPlayer();
    }

    /**
     * Check if currently viewing as player (used for role switch).
     */
    public boolean isViewingAsPlayer() {
        return viewAsPlayer;
    }

    /**
     * Toggle between organizer and player view.
     * Only available for organizers.
     */
    public void switchRole() {
        if (currentUser.isOrganizer()) {
            viewAsPlayer = !viewAsPlayer;
        }
    }

    /**
     * Get matches based on current view mode.
     */
    public List<MatchBean> getMatches() {
        if (viewAsPlayer) {
            // Player view: show all available matches
            return matchService.getAllAvailableMatches();
        } else {
            // Organizer view: show only own matches
            return matchService.getOrganizerMatches(currentUser.getUsername());
        }
    }

    /**
     * Filter matches by sport, city, and date.
     */
    public List<MatchBean> filterMatches(Sport sport, String city, LocalDate date) {
        List<MatchBean> matches = getMatches();

        return matches.stream()
                .filter(match -> sport == null || match.getSport() == sport)
                .filter(match -> city == null || city.trim().isEmpty() ||
                        match.getCity().equalsIgnoreCase(city.trim()))
                .filter(match -> date == null || match.getMatchDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Navigate to match detail view.
     */
    public void viewMatchDetail(int matchId) {
        applicationController.navigateToMatchDetail(matchId, currentUser);
    }

    /**
     * Navigate to organize match.
     */
    public void organizeMatch() {
        applicationController.navigateToOrganizeMatch(currentUser);
    }
}
