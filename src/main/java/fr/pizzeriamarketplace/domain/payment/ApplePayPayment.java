package fr.pizzeriamarketplace.domain.payment;

public class ApplePayPayment {


    public void processTransaction(int amount, String currency) {
        System.out.println("Apple Pay: " + amount + " " + currency);
    }
}
