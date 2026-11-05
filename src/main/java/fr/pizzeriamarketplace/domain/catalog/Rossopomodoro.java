package fr.pizzeriamarketplace.domain.catalog;

import fr.pizzeriamarketplace.domain.model.Base;
import fr.pizzeriamarketplace.domain.model.Pizza;
import fr.pizzeriamarketplace.domain.model.Size;
import fr.pizzeriamarketplace.domain.pricing.size.MultiplierSizePricing;
import fr.pizzeriamarketplace.domain.pricing.size.SizePricingStrategy;

import java.util.List;
import java.util.Map;

import static fr.pizzeriamarketplace.domain.catalog.Ingredients.GORGONZOLA;
import static fr.pizzeriamarketplace.domain.catalog.Ingredients.MOZZARELLA;
import static fr.pizzeriamarketplace.domain.catalog.Ingredients.MUSHROOMS;
import static fr.pizzeriamarketplace.domain.catalog.Ingredients.OLIVES;
import static fr.pizzeriamarketplace.domain.catalog.Ingredients.PEPPERONI;

/**
 * Rossopomodoro: size multiplier (S ×0.8, L ×1.3, XL ×1.5).
 */
public final class Rossopomodoro implements PizzaChain {

    @Override
    public String getName() {
        return "Rossopomodoro";
    }

    @Override
    public List<Pizza> createMenu() {
        return List.of(
                new Pizza("Supreme", Base.TOMATO, List.of(MOZZARELLA, PEPPERONI, MUSHROOMS, OLIVES), 1200),
                new Pizza("Due Formaggi", Base.CREAM, List.of(MOZZARELLA, GORGONZOLA), 1300));
    }

    @Override
    public SizePricingStrategy createSizePricing() {
        return new MultiplierSizePricing(Map.of(
                Size.SMALL, 80,
                Size.MEDIUM, 100,
                Size.LARGE, 130,
                Size.EXTRA_LARGE, 150));
    }
}
