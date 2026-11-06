package fr.pizzeriamarketplace.domain.pricing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixedAmountDiscountPricingTest {

    @Test
    void subtractsFixedAmountFromSubtotal() {
        PricingStrategy pricing = new FixedAmountDiscountPricing(300);

        int total = pricing.apply(1000);

        assertEquals(700, total);
    }

    @Test
    void neverGoesBelowZero() {
        PricingStrategy pricing = new FixedAmountDiscountPricing(500);

        int total = pricing.apply(300);

        assertEquals(0, total);
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> new FixedAmountDiscountPricing(-1));
    }
}