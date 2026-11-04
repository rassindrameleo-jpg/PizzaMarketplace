package fr.pizzeriamarketplace.domain.pricing;

/**
 * Applies a percentage discount (e.g. happy hour, loyalty).
 * The result is rounded to the nearest cent.
 */

public final class PercentageDiscountPricing implements PricingStrategy {
    private final int discountPercent;

    /**
     * @param discountPercent discount between 0 and 100
     * @throws IllegalArgumentException if discountPercent is outside 0..100
     */

    public PercentageDiscountPricing(int discountPercent) {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("discountPercent must be between 0 and 100, got " + discountPercent);
        }
        this.discountPercent = discountPercent;
    }

    @Override
    public int apply(int subtotalCents) {
        return (int) Math.round(subtotalCents * (100 - this.discountPercent) / 100.0);
    }
}
