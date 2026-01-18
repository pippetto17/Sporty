package view.joinmatchview;
import controller.ApplicationController;
import view.View;
public interface JoinMatchView extends View {
    void setApplicationController(ApplicationController applicationController);
    void displayMatchDetails();
    void displayError(String message);
    void displaySuccess(String message);
}
