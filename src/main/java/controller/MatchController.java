package controller;

import model.bean.MatchBean;
import model.domain.User;
import model.service.MatchService;
import model.utils.Constants;
import view.matchdetailview.MatchDetailView;

import java.util.List;

/**
 * Controller for match detail operations.
 * Handles match viewing, joining, and cancellation.
 */
public class MatchController {
    private final MatchService matchService;
    private MatchDetailView view;
    private User currentUser;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    public void setView(MatchDetailView view) {
        this.view = view;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Load and display match details.
     */
    public void showMatchDetail(int matchId) {
        MatchBean match = matchService.getMatchById(matchId);
        if (match == null) {
            view.showError(Constants.ERROR_MATCH_NOT_FOUND);
            return;
        }

        view.displayMatchDetails(match);

        // Show appropriate actions based on user role
        if (isUserOrganizer(match)) {
            view.showOrganizerActions();
        } else {
            view.showJoinButton(!hasUserJoined(match));
        }
    }

    /**
     * Handle player joining a match.
     */
    public void joinMatch(int matchId) {
        if (currentUser == null) {
            view.showError(Constants.ERROR_USER_NOT_LOGGED_IN);
            return;
        }

        boolean success = matchService.joinMatch(matchId, currentUser.getUsername());
        if (success) {
            view.displaySuccess(Constants.SUCCESS_JOIN_MATCH);
            // Refresh match details
            showMatchDetail(matchId);
        } else {
            view.showError(Constants.MSG_COULD_NOT_JOIN);
        }
    }

    /**
     * Handle match cancellation by organizer.
     */
    public void cancelMatch(int matchId) {
        List<String> participants = matchService.cancelMatch(matchId);
        view.displaySuccess(Constants.SUCCESS_MATCH_CANCELLED);

        // Future: send notifications to participants
        if (!participants.isEmpty()) {
            System.out.println(Constants.INFO_PARTICIPANTS_TO_NOTIFY + participants);
        }
    }

    /**
     * Navigate to invite players view (future implementation).
     */
    public void invitePlayers() {
        // Placeholder for future implementation
        view.showInfo(Constants.INFO_INVITE_COMING_SOON);
    }

    private boolean isUserOrganizer(MatchBean match) {
        return currentUser != null &&
                currentUser.getUsername().equals(match.getOrganizerUsername());
    }

    private boolean hasUserJoined(MatchBean match) {
        if (currentUser == null) {
            return false;
        }
        return match.getParticipants() != null &&
                match.getParticipants().contains(currentUser.getUsername());
    }
}
