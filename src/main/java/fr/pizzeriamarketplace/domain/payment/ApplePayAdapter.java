package fr.pizzeriamarketplace.domain.payment;

public class ApplePayAdapter  implements PaymentGateway{


    private  final ApplePayPayment  applePay ;

    public ApplePayAdapter(ApplePayPayment applePay) {
        this.applePay = applePay;
    }

    @Override
    public boolean pay(int amountCents) {

        applePay.processTransaction(amountCents, "EUR");
        return true;
    }
}
