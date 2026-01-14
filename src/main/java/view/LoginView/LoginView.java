package view.LoginView;

import controller.ApplicationController;
import model.bean.UserBean;
import view.View;

public interface LoginView extends View {
    void displayLoginSuccess(String username);
    void displayLoginError(String message);
    UserBean getUserCredentials();
    void setApplicationController(ApplicationController applicationController);
}
