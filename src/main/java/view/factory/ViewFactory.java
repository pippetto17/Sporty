package view.factory;

import controller.*;
import view.bookfieldview.BookFieldView;
import view.fieldmanagerview.FieldManagerView;
import view.homeview.HomeView;
import view.loginview.LoginView;
import view.organizematchview.OrganizeMatchView;
import view.paymentview.PaymentView;

public interface ViewFactory {
    LoginView createLoginView(LoginController loginController);

    HomeView createHomeView(HomeController homeController);

    OrganizeMatchView createOrganizeMatchView(OrganizeMatchController organizeMatchController);

    BookFieldView createBookFieldView(BookFieldController bookFieldController);

    PaymentView createPaymentView(PaymentController paymentController);

    FieldManagerView createFieldManagerView(FieldManagerController controller);

}
