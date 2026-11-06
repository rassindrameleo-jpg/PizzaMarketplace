package fr.pizzeriamarketplace.domain.payment;

public class PaypalAdapter implements PaymentGateway {

    private final PaypalPayment paypal;

    public PaypalAdapter(PaypalPayment paypal) {
        this.paypal = paypal;
    }

    @Override
    public boolean pay(int amountCents) {
        paypal.sendPayment(amountCents, "EUR");
        return true;
    }
}
