package fr.pizzeriamarketplace.domain.pricing;

/**
 * Subtracts a fixed amount from the subtotal (e.g. promo code).
 * The result never goes below zero.
 */

public final class FixedAmountDiscountPricing implements PricingStrategy {

    private final int amountCents;

    /**
     * @param amountCents discount amount in cents, must not be negative
     * @throws IllegalArgumentException if amountCents is negative
     */

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
