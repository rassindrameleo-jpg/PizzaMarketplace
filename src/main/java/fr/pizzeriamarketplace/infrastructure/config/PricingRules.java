package fr.pizzeriamarketplace.infrastructure.config;

/**
 * Business values for discounts, gathered in one place.
 * Strategies receive them through their constructor when they are created (in Main).
 */
public final class PricingRules {
    public static final int HAPPY_HOUR_DISCOUNT_PERCENT = 20;
    public static final int LOYALTY_DISCOUNT_PERCENT = 10;
    public static final int PROMO_CODE_DISCOUNT_CENTS = 500;

    private PricingRules() {
        // Constants holder, not meant to be instantiated
    }
}