package fr.pizzeriamarketplace.domain.pricing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardPricingTest {

    @Test
    void returnsSubtotalUnchanged() {
        PricingStrategy pricing = new StandardPricing();

        int total = pricing.apply(1000);

        assertEquals(1000, total);
    }
}