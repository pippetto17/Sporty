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
}
