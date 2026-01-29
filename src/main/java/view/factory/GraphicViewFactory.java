package view.factory;

import controller.*;
import view.bookfieldview.BookFieldView;
import view.bookfieldview.GraphicBookFieldView;
import view.homeview.GraphicHomeView;
import view.homeview.HomeView;
import view.loginview.GraphicLoginView;
import view.loginview.LoginView;
import view.organizematchview.GraphicOrganizeMatchView;
import view.organizematchview.OrganizeMatchView;
import view.paymentview.GraphicPaymentView;
import view.paymentview.PaymentView;

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
        return new GraphicPaymentView();
    }

    @Override
    public view.fieldmanagerview.FieldManagerView createFieldManagerView(controller.FieldManagerController controller) {
        return new view.fieldmanagerview.GraphicFieldManagerView(controller, controller.getFieldManager());
    }

}
