package fr.pizzeriamarketplace.domain.catalog;

import fr.pizzeriamarketplace.domain.model.Pizza;
import fr.pizzeriamarketplace.domain.pricing.size.SizePricingStrategy;

import java.util.List;

/**
 * Abstract Factory: each chain creates its own family of related objects (its menu and its size pricing rule),
 * so a menu can never be combined with another chain's prices.
 * Adding a chain means adding one class that implements this interface.
 */
public interface PizzaChain {
    String getName();

    List<Pizza> createMenu();

    SizePricingStrategy createSizePricing();
}
