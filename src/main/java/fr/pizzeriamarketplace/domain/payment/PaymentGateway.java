package fr.pizzeriamarketplace.domain.payment;

public interface PaymentGateway {

    boolean pay(int amountCents);
}
