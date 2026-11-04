package fr.pizzeriamarketplace.domain.model;

public final class Ingredient {
    private final String name;
    private final int priceInCents;

    public Ingredient(String name, int priceInCents) {
        this.name = name;
        this.priceInCents = priceInCents;
    }

    public String getName() {
        return name;
    }

    public int getPriceInCents(){
        return priceInCents;
    }
}
