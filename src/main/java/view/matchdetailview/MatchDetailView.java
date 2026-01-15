package view.matchdetailview;

import model.bean.MatchBean;
import view.View;

public interface MatchDetailView extends View {
    /**
     * Display detailed match information.
     */
    void displayMatchDetails(MatchBean match);

    /**
     * Show the join button for players.
     */
    void showJoinButton(boolean show);

    /**
     * Show organizer actions (invite, cancel).
     */
    void showOrganizerActions();

    /**
     * Display error message.
     */
    void showError(String message);

    /**
     * Display success message.
     */
    void displaySuccess(String message);

    /**
     * Display info message.
     */
    void showInfo(String message);

    /**
     * Close the detail view.
     */
    void close();
}
