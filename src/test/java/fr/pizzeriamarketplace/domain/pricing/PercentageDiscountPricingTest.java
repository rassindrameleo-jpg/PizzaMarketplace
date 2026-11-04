package fr.pizzeriamarketplace.domain.pricing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PercentageDiscountPricingTest {

    @Test
    void appliesPercentageDiscount() {
        PricingStrategy pricing = new PercentageDiscountPricing(20);

        int total = pricing.apply(1000);

        assertEquals(800, total);
    }

    @Test
    void roundsToNearestCent() {
        PricingStrategy pricing = new PercentageDiscountPricing(20);

        // 999 * 0.8 = 799.2 -> 799
        int total = pricing.apply(999);

        assertEquals(799, total);
    }

    @Test
    void fullDiscountGivesZero() {
        PricingStrategy pricing = new PercentageDiscountPricing(100);

        int total = pricing.apply(1000);

        assertEquals(0, total);
    }

    @Test
    void zeroDiscountKeepsSubtotal() {
        PricingStrategy pricing = new PercentageDiscountPricing(0);

        int total = pricing.apply(1000);

        assertEquals(1000, total);
    }

    @Test
    void rejectsPercentAbove100() {
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscountPricing(150));
    }

}