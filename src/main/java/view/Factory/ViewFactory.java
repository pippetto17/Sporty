package view.Factory;

import controller.LoginController;
import controller.HomeController;
import controller.OrganizeMatchController;
import controller.BookFieldController;
import view.LoginView.LoginView;
import view.HomeView.HomeView;
import view.OrganizeMatchView.OrganizeMatchView;
import view.BookFieldView.BookFieldView;

public interface ViewFactory {
    LoginView createLoginView(LoginController loginController);
    HomeView createHomeView(HomeController homeController);
    OrganizeMatchView createOrganizeMatchView(OrganizeMatchController organizeMatchController);
    BookFieldView createBookFieldView(BookFieldController bookFieldController);
}
