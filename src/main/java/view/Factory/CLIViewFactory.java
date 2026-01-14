package view.Factory;

import controller.LoginController;
import controller.HomeController;
import controller.OrganizeMatchController;
import controller.BookFieldController;
import view.LoginView.LoginView;
import view.LoginView.CLILoginView;
import view.HomeView.HomeView;
import view.HomeView.CLIHomeView;
import view.OrganizeMatchView.OrganizeMatchView;
import view.OrganizeMatchView.CLIOrganizeMatchView;
import view.BookFieldView.BookFieldView;
import view.BookFieldView.CLIBookFieldView;

public class CLIViewFactory implements ViewFactory {

    @Override
    public LoginView createLoginView(LoginController loginController) {
        return new CLILoginView(loginController);
    }

    @Override
    public HomeView createHomeView(HomeController homeController) {
        return new CLIHomeView(homeController);
    }

    @Override
    public OrganizeMatchView createOrganizeMatchView(OrganizeMatchController organizeMatchController) {
        return new CLIOrganizeMatchView(organizeMatchController);
    }

    @Override
    public BookFieldView createBookFieldView(BookFieldController bookFieldController) {
        return new CLIBookFieldView(bookFieldController);
    }
}
