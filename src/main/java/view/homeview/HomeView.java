package view.homeview;

import controller.ApplicationController;
import model.bean.MatchBean;
import view.View;
import java.util.List;

public interface HomeView extends View {
    void displayWelcome();

    void displayMatches(List<MatchBean> matches);

    void displayMenu();

    void setApplicationController(ApplicationController applicationController);

    void refreshMatches();

    void showMatchDetails(int matchId);

    void displayError(String message);

    void displaySuccess(String message);
}