package fr.pizzeriamarketplace.domain.catalog;

import fr.pizzeriamarketplace.domain.model.Base;
import fr.pizzeriamarketplace.domain.model.Pizza;
import fr.pizzeriamarketplace.domain.model.Size;
import fr.pizzeriamarketplace.domain.pricing.size.SingleSizePricing;
import fr.pizzeriamarketplace.domain.pricing.size.SizePricingStrategy;

import java.util.List;

import static fr.pizzeriamarketplace.domain.catalog.Ingredients.HAM;
import static fr.pizzeriamarketplace.domain.catalog.Ingredients.MOZZARELLA;
import static fr.pizzeriamarketplace.domain.catalog.Ingredients.OLIVES;

/**
 * Pizzium: single size M.
 */
public final class Pizzium implements PizzaChain {

    @Override
    public String getName() {
        return "Pizzium";
    }

    @Override
    public List<Pizza> createMenu() {
        return List.of(
                new Pizza("Napolitaine", Base.TOMATO, List.of(MOZZARELLA, OLIVES), 1000),
                new Pizza("Prosciutto", Base.TOMATO, List.of(MOZZARELLA, HAM), 1100));
    }

    @Override
    public SizePricingStrategy createSizePricing() {
        return new SingleSizePricing(Size.MEDIUM);
    }
}
