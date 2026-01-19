package view.joinmatchview;

import controller.ApplicationController;
import view.View;

public interface JoinMatchView extends View {
    void setApplicationController(ApplicationController applicationController);

    void displayMatchInfo(String sport, String date, String time, String city,
                         String organizer, int[] participants, double cost);

    void displayCannotJoin(String reason);

    String getUserChoice();

    void displayError(String message);

    void displaySuccess(String message);
}
