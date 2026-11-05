package fr.pizzeriamarketplace.domain.pricing.size;

import fr.pizzeriamarketplace.domain.model.Size;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultiplierSizePricingTest {
    private final SizePricingStrategy rossopomodoroPricing = new MultiplierSizePricing(Map.of(
            Size.SMALL, 80,
            Size.MEDIUM, 100,
            Size.LARGE, 130,
            Size.EXTRA_LARGE, 150));

    @Test
    void multipliesMediumPriceByPercentOfEachSize() {
        int supremeCents = 1200;

        assertEquals(960, rossopomodoroPricing.priceFor(supremeCents, Size.SMALL));
        assertEquals(1200, rossopomodoroPricing.priceFor(supremeCents, Size.MEDIUM));
        assertEquals(1560, rossopomodoroPricing.priceFor(supremeCents, Size.LARGE));
        assertEquals(1800, rossopomodoroPricing.priceFor(supremeCents, Size.EXTRA_LARGE));
    }

    @Test
    void roundsToNearestCent() {
        int price = rossopomodoroPricing.priceFor(999, Size.LARGE);

        assertEquals(1299, price); // 999 × 1.3 = 1298.7
    }

    @Test
    void refusesSizeNotInTheMap() {
        SizePricingStrategy pricing = new MultiplierSizePricing(Map.of(Size.MEDIUM, 100));

        assertFalse(pricing.supports(Size.SMALL));
        assertThrows(IllegalArgumentException.class, () -> pricing.priceFor(1200, Size.SMALL));
    }

    @Test
    void rejectsNegativePercent() {
        assertThrows(IllegalArgumentException.class, () -> new MultiplierSizePricing(Map.of(Size.SMALL, -1)));
    }

    @Test
    void rejectsEmptyMap() {
        assertThrows(IllegalArgumentException.class, () -> new MultiplierSizePricing(Map.of()));
    }
}
