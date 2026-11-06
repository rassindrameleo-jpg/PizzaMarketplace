package fr.pizzeriamarketplace.domain.catalog;

import fr.pizzeriamarketplace.domain.model.Base;
import fr.pizzeriamarketplace.domain.model.Ingredient;
import fr.pizzeriamarketplace.domain.model.Pizza;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder: composes a custom pizza step by step (base, then ingredients) and computes its price in {@link #build()}.
 * The size is not chosen here but when the pizza is added to the cart, like any menu pizza:
 * the chain's size rule is then applied to the reference price computed here.
 */
public final class CustomPizzaBuilder {
    public static final String NAME = "Pizza personnalisée";
    public static final int BASE_PRICE_CENTS = 500;

    private Base base;
    private final List<Ingredient> ingredients = new ArrayList<>();

    public CustomPizzaBuilder base(Base base) {
        this.base = Objects.requireNonNull(base, "base must not be null");
        return this;
    }

    /**
     * Adds an ingredient from {@link Ingredients#AVAILABLE}. The same ingredient may be added several times.
     *
     * @throws IllegalArgumentException if the ingredient is not available on the marketplace
     */
    public CustomPizzaBuilder addIngredient(Ingredient ingredient) {
        Objects.requireNonNull(ingredient, "ingredient must not be null");
        if (!Ingredients.AVAILABLE.contains(ingredient)) {
            throw new IllegalArgumentException("ingredient not available: " + ingredient.getName());
        }
        ingredients.add(ingredient);
        return this;
    }

    /**
     * @return a pizza whose reference price (size M) is the base price plus the price of each ingredient
     * @throws IllegalStateException if no base was chosen
     */
    public Pizza build() {
        if (base == null) {
            throw new IllegalStateException("a base must be chosen before building the pizza");
        }
        int ingredientsCents = ingredients.stream().mapToInt(Ingredient::getPriceInCents).sum();
        return new Pizza(NAME, base, ingredients, BASE_PRICE_CENTS + ingredientsCents);
    }
}
