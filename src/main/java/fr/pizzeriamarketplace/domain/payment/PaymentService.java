package fr.pizzeriamarketplace.domain.payment;

public class PaymentService {
    private final PaymentGateway gateway;
    private final PaymentGateway fallbackGateway;
    public PaymentService(
            PaymentGateway gateway,
            PaymentGateway fallbackGateway
    ) {
        this.gateway = gateway;
        this.fallbackGateway = fallbackGateway;
    }

    public boolean pay(int amountCents) {

        boolean success = gateway.pay(amountCents);

        if (!success) {
            System.out.println("Paiement échoué, changement de méthode...");
            return fallbackGateway.pay(amountCents);
        }

        return true;
    }
}