package com.safecard.android.model.dataobjects;

public class ParkingData implements AccessData{

    private String PaymentMethodId;

    public String getPaymentMethodId() {
        return PaymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        PaymentMethodId = paymentMethodId;
    }
}
