package fr.pizzeriamarketplace.domain.pricing;

/**
 * Pricing without any discount: returns the subtotal unchanged.
 * Acts as a Null Object, so callers never need to check for "no discount".
 */

public final class StandardPricing implements PricingStrategy {
    @Override
    public int apply(int subtotalCents) {
        return subtotalCents;
    }
}
