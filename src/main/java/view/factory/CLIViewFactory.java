package view.factory;

import controller.LoginController;
import controller.HomeController;
import controller.OrganizeMatchController;
import controller.BookFieldController;
import controller.FieldManagerController;
import view.loginview.LoginView;
import view.loginview.CLILoginView;
import view.homeview.HomeView;
import view.homeview.CLIHomeView;
import view.organizematchview.OrganizeMatchView;
import view.organizematchview.CLIOrganizeMatchView;
import view.bookfieldview.BookFieldView;
import view.bookfieldview.CLIBookFieldView;
import view.paymentview.CLIPaymentView;
import view.paymentview.PaymentView;
import view.recapview.CLIRecapView;
import view.recapview.RecapView;
import controller.PaymentController;
import view.fieldmanagerview.CLIFieldManagerView;
import view.fieldmanagerview.FieldManagerView;
import view.addfieldview.AddFieldView;
import view.addfieldview.CLIAddFieldView;
import view.myfieldsview.MyFieldsView;
import view.myfieldsview.CLIMyFieldsView;

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
        return new CLIPaymentView(paymentController);
    }

    @Override
    public RecapView createRecapView() {
        return new CLIRecapView();
    }

    @Override
    public FieldManagerView createFieldManagerView(FieldManagerController controller) {
        return new CLIFieldManagerView(controller, controller.getFieldManager());
    }

    @Override
    public AddFieldView createAddFieldView(FieldManagerController controller) {
        return new CLIAddFieldView(controller);
    }

    @Override
    public MyFieldsView createMyFieldsView(FieldManagerController controller) {
        return new CLIMyFieldsView(controller);
    }
}
