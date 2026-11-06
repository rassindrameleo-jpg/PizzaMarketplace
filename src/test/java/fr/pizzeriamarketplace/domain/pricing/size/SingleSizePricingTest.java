package fr.pizzeriamarketplace.domain.pricing.size;

import fr.pizzeriamarketplace.domain.model.Size;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleSizePricingTest {
    private final SizePricingStrategy pizziumPricing = new SingleSizePricing(Size.MEDIUM);

    @Test
    void keepsMediumPriceForOfferedSize() {
        int napolitaineCents = 1000;

        int price = pizziumPricing.priceFor(napolitaineCents, Size.MEDIUM);

        assertEquals(1000, price);
    }

    @Test
    void offersOnlyOneSize() {
        assertTrue(pizziumPricing.supports(Size.MEDIUM));
        assertFalse(pizziumPricing.supports(Size.SMALL));
        assertFalse(pizziumPricing.supports(Size.LARGE));
        assertFalse(pizziumPricing.supports(Size.EXTRA_LARGE));
    }

    @Test
    void refusesOtherSizes() {
        assertThrows(IllegalArgumentException.class, () -> pizziumPricing.priceFor(1000, Size.SMALL));
        assertThrows(IllegalArgumentException.class, () -> pizziumPricing.priceFor(1000, Size.LARGE));
        assertThrows(IllegalArgumentException.class, () -> pizziumPricing.priceFor(1000, Size.EXTRA_LARGE));
    }

    @Test
    void rejectsNullSize() {
        assertThrows(NullPointerException.class, () -> new SingleSizePricing(null));
    }
}
