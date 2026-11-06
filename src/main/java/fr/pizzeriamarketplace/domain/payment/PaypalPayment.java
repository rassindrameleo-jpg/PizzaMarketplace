package fr.pizzeriamarketplace.domain.payment;

public class PaypalPayment {
    public void sendPayment(int amount, String currency) {
        System.out.println("Paypal payment of " + amount + " " + currency);
    }
}
