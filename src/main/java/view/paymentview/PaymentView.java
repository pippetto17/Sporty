package view.paymentview;

import model.bean.FieldBean;
import model.bean.MatchBean;
import model.bean.PaymentBean;
import view.View;

public interface PaymentView extends View {
    void displayMatchInfo(MatchBean match, int availableShares);

    void displayBookingInfo(FieldBean field, MatchBean context);

    PaymentBean collectPaymentData( int maxShares);
}