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

    /**
     * Show match details in a dialog/popup.
     * 
     * @param matchId ID of the match to show details for
     */
    void showMatchDetails(int matchId);
}
