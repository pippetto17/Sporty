package view.homeview;

import controller.ApplicationController;
import view.View;

public interface HomeView extends View {
    void displayWelcome();
    void displayMatches(String[] matches);
    void displayMenu();
    void setApplicationController(ApplicationController applicationController);
}

