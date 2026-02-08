package view.loginview;

import model.bean.UserBean;
import view.View;

public interface LoginView extends View {
    void displayLoginSuccess(String username);

    void displayLoginError(String message);

    UserBean getUserCredentials();
}