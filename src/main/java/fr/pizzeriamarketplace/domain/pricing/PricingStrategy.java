package fr.pizzeriamarketplace.domain.pricing;

public interface PricingStrategy {
    int apply(int subtotalCents);
}
