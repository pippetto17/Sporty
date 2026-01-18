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

    List<MatchBean> getMatches();

    default MatchBean findMatchById(int matchId) {
        return getMatches().stream()
                .filter(m -> m.getMatchId() == matchId)
                .findFirst()
                .orElse(null);
    }

    void showMatchDetails(int matchId);
}
