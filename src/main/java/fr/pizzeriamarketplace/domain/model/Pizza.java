package fr.pizzeriamarketplace.domain.model;

import java.util.List;

public final class Pizza {
    private final String name;
    private final Base base;
    private final List<Ingredient> ingredients;

    public Pizza(String name, Base base, List<Ingredient> ingredients) {
        this.name = name;
        this.base = base;
        this.ingredients = ingredients;
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
}
