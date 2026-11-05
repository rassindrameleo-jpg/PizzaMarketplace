package fr.pizzeriamarketplace.domain.catalog;

import fr.pizzeriamarketplace.domain.model.Base;
import fr.pizzeriamarketplace.domain.model.Pizza;
import fr.pizzeriamarketplace.domain.model.Size;
import fr.pizzeriamarketplace.domain.pricing.size.FixedSurchargeSizePricing;
import fr.pizzeriamarketplace.domain.pricing.size.SizePricingStrategy;

import java.util.List;
import java.util.Map;

import static fr.pizzeriamarketplace.domain.catalog.Ingredients.HAM;
import static fr.pizzeriamarketplace.domain.catalog.Ingredients.MOZZARELLA;
import static fr.pizzeriamarketplace.domain.catalog.Ingredients.MUSHROOMS;

/**
 * Da Michele: fixed surcharge per size (S -2€, L +3€, XL +5€).
 */
public final class DaMichele implements PizzaChain {

    @Override
    public String getName() {
        return "Da Michele";
    }

    @Override
    public List<Pizza> createMenu() {
        return List.of(
                new Pizza("Margherita", Base.TOMATO, List.of(MOZZARELLA), 900),
                new Pizza("Regina", Base.TOMATO, List.of(MOZZARELLA, HAM, MUSHROOMS), 1100));
    }

    @Override
    public SizePricingStrategy createSizePricing() {
        return new FixedSurchargeSizePricing(Map.of(
                Size.SMALL, -200,
                Size.MEDIUM, 0,
                Size.LARGE, 300,
                Size.EXTRA_LARGE, 500));
    }
}
