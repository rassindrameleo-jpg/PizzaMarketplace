package fr.pizzeriamarketplace.domain.pricing;

public final class StandardPricing implements PricingStrategy {
    @Override
    public int apply(int subtotalCents) {
        return subtotalCents;
    }
}
