package fr.pizzeriamarketplace.domain.pricing.size;

import fr.pizzeriamarketplace.domain.model.Size;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixedSurchargeSizePricingTest {
    private final SizePricingStrategy daMichelePricing = new FixedSurchargeSizePricing(Map.of(
            Size.SMALL, -200,
            Size.MEDIUM, 0,
            Size.LARGE, 300,
            Size.EXTRA_LARGE, 500));

    @Test
    void addsSurchargeOfEachSizeToMediumPrice() {
        int margheritaCents = 900;

        assertEquals(700, daMichelePricing.priceFor(margheritaCents, Size.SMALL));
        assertEquals(900, daMichelePricing.priceFor(margheritaCents, Size.MEDIUM));
        assertEquals(1200, daMichelePricing.priceFor(margheritaCents, Size.LARGE));
        assertEquals(1400, daMichelePricing.priceFor(margheritaCents, Size.EXTRA_LARGE));
    }

    @Test
    void neverGoesBelowZero() {
        int price = daMichelePricing.priceFor(100, Size.SMALL);

        assertEquals(0, price);
    }

    @Test
    void refusesSizeNotInTheMap() {
        SizePricingStrategy pricing = new FixedSurchargeSizePricing(Map.of(Size.MEDIUM, 0));

        assertFalse(pricing.supports(Size.LARGE));
        assertThrows(IllegalArgumentException.class, () -> pricing.priceFor(900, Size.LARGE));
    }

    @Test
    void rejectsNegativeMediumPrice() {
        assertThrows(IllegalArgumentException.class, () -> daMichelePricing.priceFor(-1, Size.MEDIUM));
    }

    @Test
    void rejectsEmptyMap() {
        assertThrows(IllegalArgumentException.class, () -> new FixedSurchargeSizePricing(Map.of()));
    }
}
