package view.factory;

import controller.LoginController;
import controller.HomeController;
import controller.OrganizeMatchController;
import controller.BookFieldController;
import view.loginview.LoginView;
import view.homeview.HomeView;
import view.organizematchview.OrganizeMatchView;
import view.bookfieldview.BookFieldView;
import view.paymentview.PaymentView;
import view.recapview.RecapView;
import view.fieldmanagerview.FieldManagerView;
import controller.PaymentController;
import controller.FieldManagerController;
import view.addfieldview.AddFieldView;
import view.myfieldsview.MyFieldsView;

public interface ViewFactory {
    LoginView createLoginView(LoginController loginController);

    HomeView createHomeView(HomeController homeController);

    OrganizeMatchView createOrganizeMatchView(OrganizeMatchController organizeMatchController);

    BookFieldView createBookFieldView(BookFieldController bookFieldController);

    PaymentView createPaymentView(PaymentController paymentController);

    RecapView createRecapView();

    FieldManagerView createFieldManagerView(FieldManagerController controller);

    // Field Manager additional views
    AddFieldView createAddFieldView(FieldManagerController controller);

    MyFieldsView createMyFieldsView(FieldManagerController controller);
}
