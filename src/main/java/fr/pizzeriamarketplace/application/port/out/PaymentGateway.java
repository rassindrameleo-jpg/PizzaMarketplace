package fr.pizzeriamarketplace.application.port.out;

public interface PaymentGateway {
    boolean pay(int amountCents);
}
