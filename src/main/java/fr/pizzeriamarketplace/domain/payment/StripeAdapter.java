package fr.pizzeriamarketplace.domain.payment;

public class StripeAdapter implements PaymentGateway {


    private final StripePayment stripe;

    public StripeAdapter(StripePayment stripe) {
        this.stripe = stripe;
    }

    private boolean available = true;

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public boolean pay(int amountCents) {
        if (!available) {
            System.out.println("Stripe indisponible !");
            return false;
        }

        stripe.makePayment(amountCents);
        return true;
    }
}

