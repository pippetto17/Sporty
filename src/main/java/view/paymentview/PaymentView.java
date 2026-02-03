package view.paymentview;

import controller.ApplicationController;
import model.bean.FieldBean;
import model.bean.MatchBean;
import model.bean.PaymentBean;
import view.View;

public interface PaymentView extends View {
    void setApplicationController(ApplicationController applicationController);

    void showError(String message);

    void displayMatchInfo(MatchBean match, int availableShares);

    void displayBookingInfo(FieldBean field, MatchBean context);

    void showSuccess(String message);

    PaymentBean collectPaymentData(int maxShares);
}