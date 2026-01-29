package view.factory;

import controller.*;
import view.bookfieldview.BookFieldView;
import view.bookfieldview.CLIBookFieldView;
import view.fieldmanagerview.CLIFieldManagerView;
import view.fieldmanagerview.FieldManagerView;
import view.homeview.CLIHomeView;
import view.homeview.HomeView;
import view.loginview.CLILoginView;
import view.loginview.LoginView;
import view.organizematchview.CLIOrganizeMatchView;
import view.organizematchview.OrganizeMatchView;
import view.paymentview.CLIPaymentView;
import view.paymentview.PaymentView;

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

    @Override
    public PaymentView createPaymentView(PaymentController paymentController) {
        return new CLIPaymentView();
    }

    @Override
    public FieldManagerView createFieldManagerView(FieldManagerController controller) {
        return new CLIFieldManagerView(controller, controller.getFieldManager());
    }

}
