package fr.pizzeriamarketplace.domain.catalog;

import fr.pizzeriamarketplace.domain.model.Ingredient;

import java.util.List;

/**
 * Ingredients available on the marketplace, with their price in cents.
 * Shared by the chains' menus and by custom pizzas.
 */
public final class Ingredients {
    public static final Ingredient MOZZARELLA = new Ingredient("Mozzarella", 150);
    public static final Ingredient PEPPERONI = new Ingredient("Pepperoni", 200);
    public static final Ingredient MUSHROOMS = new Ingredient("Champignons", 100);
    public static final Ingredient HAM = new Ingredient("Jambon", 200);
    public static final Ingredient OLIVES = new Ingredient("Olives", 100);
    public static final Ingredient GORGONZOLA = new Ingredient("Gorgonzola", 250);

    /**
     * Ingredients a customer can add to a custom pizza.
     */
    public static final List<Ingredient> AVAILABLE =
            List.of(MOZZARELLA, PEPPERONI, MUSHROOMS, HAM, OLIVES, GORGONZOLA);

    private Ingredients() {
        // Constants holder, not meant to be instantiated
    }
}
