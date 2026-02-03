package view.organizematchview;

import controller.ApplicationController;
import model.bean.MatchBean;
import view.View;

public interface OrganizeMatchView extends View {
    void setApplicationController(ApplicationController applicationController);

    void displayMatchList();

    void displayNewMatchForm();

    void displayError(String message);

    void displaySuccess(String message);

    void displayRecap(MatchBean matchBean);
}