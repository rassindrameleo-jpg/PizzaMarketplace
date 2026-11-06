package fr.pizzeriamarketplace.domain.payment;

public class StripePayment {

    public void makePayment(int cents) {
        System.out.println("Stripe payment of " + cents + " cents");
    }
}
