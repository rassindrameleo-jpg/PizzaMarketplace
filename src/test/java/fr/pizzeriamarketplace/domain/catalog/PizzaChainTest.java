package fr.pizzeriamarketplace.domain.catalog;

import fr.pizzeriamarketplace.domain.model.Pizza;
import fr.pizzeriamarketplace.domain.model.Size;
import fr.pizzeriamarketplace.domain.pricing.size.SizePricingStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks each chain against the examples of the subject (pizza price and size rule).
 */
class PizzaChainTest {

    @Test
    void daMicheleSellsMargheritaWithFixedSurcharges() {
        PizzaChain chain = new DaMichele();
        Pizza margherita = findOnMenu(chain, "Margherita");
        SizePricingStrategy pricing = chain.createSizePricing();

        assertEquals(900, margherita.getMediumPriceCents());
        assertEquals(700, pricing.priceFor(margherita.getMediumPriceCents(), Size.SMALL));
        assertEquals(1200, pricing.priceFor(margherita.getMediumPriceCents(), Size.LARGE));
        assertEquals(1400, pricing.priceFor(margherita.getMediumPriceCents(), Size.EXTRA_LARGE));
    }

    @Test
    void rossopomodoroSellsSupremeWithMultipliers() {
        PizzaChain chain = new Rossopomodoro();
        Pizza supreme = findOnMenu(chain, "Supreme");
        SizePricingStrategy pricing = chain.createSizePricing();

        assertEquals(1200, supreme.getMediumPriceCents());
        assertEquals(960, pricing.priceFor(supreme.getMediumPriceCents(), Size.SMALL));
        assertEquals(1560, pricing.priceFor(supreme.getMediumPriceCents(), Size.LARGE));
        assertEquals(1800, pricing.priceFor(supreme.getMediumPriceCents(), Size.EXTRA_LARGE));
    }

    @Test
    void pizziumSellsNapolitaineInSizeMediumOnly() {
        PizzaChain chain = new Pizzium();
        Pizza napolitaine = findOnMenu(chain, "Napolitaine");
        SizePricingStrategy pricing = chain.createSizePricing();

        assertEquals(1000, pricing.priceFor(napolitaine.getMediumPriceCents(), Size.MEDIUM));
        assertTrue(pricing.supports(Size.MEDIUM));
        assertFalse(pricing.supports(Size.SMALL));
        assertFalse(pricing.supports(Size.LARGE));
        assertFalse(pricing.supports(Size.EXTRA_LARGE));
    }

    private Pizza findOnMenu(PizzaChain chain, String pizzaName) {
        return chain.createMenu().stream()
                .filter(pizza -> pizza.getName().equals(pizzaName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(pizzaName + " is not on the menu of " + chain.getName()));
    }
}
