package fr.pizzeriamarketplace.domain.pricing;

public final class FixedAmountDiscountPricing implements PricingStrategy {

    private final int amountCents;

    public FixedAmountDiscountPricing(int amountCents) {
        if (amountCents < 0) {
            throw new IllegalArgumentException("amountCents must not be negative, got " + amountCents);
        }
        this.amountCents = amountCents;
    }

    @Override
    public int apply(int subtotalCents) {
        return Math.max(0, subtotalCents - amountCents);
    }
}
