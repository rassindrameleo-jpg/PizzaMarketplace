package fr.pizzeriamarketplace.domain.model;

import java.util.List;
import java.util.Objects;

public final class Pizza {
    private final String name;
    private final Base base;
    private final List<Ingredient> ingredients;
    private final int mediumPriceCents;

    /**
     * @param mediumPriceCents reference price for size M, in cents; each chain's size rule is applied on top of it
     * @throws IllegalArgumentException if the name is blank or the price is negative
     */
    public Pizza(String name, Base base, List<Ingredient> ingredients, int mediumPriceCents) {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (mediumPriceCents < 0) {
            throw new IllegalArgumentException("mediumPriceCents must not be negative, got " + mediumPriceCents);
        }
        this.name = name;
        this.base = Objects.requireNonNull(base, "base must not be null");
        this.ingredients = List.copyOf(Objects.requireNonNull(ingredients, "ingredients must not be null"));
        this.mediumPriceCents = mediumPriceCents;
    }

    public String getName() {
        return name;
    }

    public Base getBase() {
        return base;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public int getMediumPriceCents() {
        return mediumPriceCents;
    }
}
