package fr.pizzeriamarketplace.domain.pricing;

public final class PercentageDiscountPricing implements PricingStrategy {
    private final int discountPercent;

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
