package view.factory;

import controller.LoginController;
import controller.HomeController;
import controller.OrganizeMatchController;
import controller.BookFieldController;
import controller.PaymentController;
import view.loginview.LoginView;
import view.loginview.GraphicLoginView;
import view.homeview.HomeView;
import view.homeview.GraphicHomeView;
import view.organizematchview.OrganizeMatchView;
import view.organizematchview.GraphicOrganizeMatchView;
import view.bookfieldview.BookFieldView;
import view.bookfieldview.GraphicBookFieldView;
import view.paymentview.GraphicPaymentView;
import view.paymentview.PaymentView;
import view.recapview.GraphicRecapView;
import view.recapview.RecapView;

public class GraphicViewFactory implements ViewFactory {

    @Override
    public LoginView createLoginView(LoginController loginController) {
        GraphicLoginView graphicView = new GraphicLoginView(loginController);
        GraphicLoginView.setStaticLoginController(loginController);
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

    @Override
    public PaymentView createPaymentView(PaymentController paymentController) {
        return new GraphicPaymentView(paymentController);
    }

    @Override
    public RecapView createRecapView() {
        return new GraphicRecapView();
    }
}
