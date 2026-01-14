package model.service;

import model.bean.PaymentBean;

public class PaymentService {

    public boolean processPayment(PaymentBean paymentBean) {
        // Validation logic
        if (paymentBean == null) {
            return false;
        }

        // Basic validation of fields
        if (paymentBean.getCardNumber() == null || paymentBean.getCardNumber().trim().length() < 13) {
            return false;
        }

        if (paymentBean.getCvv() == null || paymentBean.getCvv().trim().length() != 3) {
            return false;
        }

        if (paymentBean.getExpiryDate() == null || paymentBean.getExpiryDate().isEmpty()) {
            return false;
        }

        if (paymentBean.getCardHolder() == null || paymentBean.getCardHolder().isEmpty()) {
            return false;
        }

        // Simulate processing time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Always return true for valid-looking data in this fake service
        return true;
    }
}
