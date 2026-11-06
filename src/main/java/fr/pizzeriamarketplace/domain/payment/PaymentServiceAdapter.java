package fr.pizzeriamarketplace.domain.payment;

import java.util.Objects;

/** Combines the primary and fallback payment gateways behind one payment gateway. */
public final class PaymentServiceAdapter implements PaymentGateway {
    private final PaymentService paymentService;

    public PaymentServiceAdapter(PaymentGateway primaryGateway, PaymentGateway fallbackGateway) {
        this.paymentService = new PaymentService(
                Objects.requireNonNull(primaryGateway, "primaryGateway must not be null"),
                Objects.requireNonNull(fallbackGateway, "fallbackGateway must not be null"));
    }

    @Override
    public boolean pay(int amountCents) {
        return paymentService.pay(amountCents);
    }
}
