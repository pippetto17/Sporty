package view.factory;

import controller.*;
import view.addfieldview.AddFieldView;
import view.addfieldview.CLIAddFieldView;
import view.bookfieldview.BookFieldView;
import view.bookfieldview.CLIBookFieldView;
import view.fieldmanagerview.CLIFieldManagerView;
import view.fieldmanagerview.FieldManagerView;
import view.homeview.CLIHomeView;
import view.homeview.HomeView;
import view.joinmatchview.CLIJoinMatchView;
import view.joinmatchview.JoinMatchView;
import view.loginview.CLILoginView;
import view.loginview.LoginView;
import view.myfieldsview.CLIMyFieldsView;
import view.myfieldsview.MyFieldsView;
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

    @Override
    public AddFieldView createAddFieldView(FieldManagerController controller) {
        return new CLIAddFieldView(controller);
    }

    @Override
    public MyFieldsView createMyFieldsView(FieldManagerController controller) {
        return new CLIMyFieldsView(controller);
    }

    @Override
    public JoinMatchView createJoinMatchView(JoinMatchController controller) {
        return new CLIJoinMatchView();
    }
}
