package fr.pizzeriamarketplace.infrastructure.payment;

import fr.pizzeriamarketplace.application.port.out.PaymentGateway;
import fr.pizzeriamarketplace.domain.payment.PaymentService;

import java.util.Objects;

/** Bridges the application payment port to the existing payment service and its fallback gateways. */
public final class PaymentServiceAdapter implements PaymentGateway {
    private final PaymentService paymentService;

    public PaymentServiceAdapter(
            fr.pizzeriamarketplace.domain.payment.PaymentGateway primaryGateway,
            fr.pizzeriamarketplace.domain.payment.PaymentGateway fallbackGateway) {
        this.paymentService = new PaymentService(
                Objects.requireNonNull(primaryGateway, "primaryGateway must not be null"),
                Objects.requireNonNull(fallbackGateway, "fallbackGateway must not be null"));
    }

    @Override
    public boolean pay(int amountCents) {
        return paymentService.pay(amountCents);
    }
}
