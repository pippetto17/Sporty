package view.Factory;

import controller.LoginController;
import controller.HomeController;
import controller.OrganizeMatchController;
import controller.BookFieldController;
import view.LoginView.LoginView;
import view.LoginView.GraphicLoginView;
import view.HomeView.HomeView;
import view.HomeView.GraphicHomeView;
import view.OrganizeMatchView.OrganizeMatchView;
import view.OrganizeMatchView.GraphicOrganizeMatchView;
import view.BookFieldView.BookFieldView;
import view.BookFieldView.GraphicBookFieldView;

public class GraphicViewFactory implements ViewFactory {

    @Override
    public LoginView createLoginView(LoginController loginController) {
        GraphicLoginView graphicView = new GraphicLoginView();
        graphicView.setLoginController(loginController);
        return graphicView;
    }

    @Override
    public HomeView createHomeView(HomeController homeController) {
        return new GraphicHomeView(homeController);
    }

    @Override
    public OrganizeMatchView createOrganizeMatchView(OrganizeMatchController organizeMatchController) {
        return new GraphicOrganizeMatchView(organizeMatchController);
    }

    @Override
    public BookFieldView createBookFieldView(BookFieldController bookFieldController) {
        return new GraphicBookFieldView(bookFieldController);
    }
}
