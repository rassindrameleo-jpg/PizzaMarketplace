package fr.pizzeriamarketplace.domain.model;

import java.util.Map;

public class Stock {


    private final Map<String, Integer> quantities;

    public Stock(Map<String, Integer> quantities) {
        this.quantities = Map.copyOf(quantities);
    }

    public boolean isAvailable(String ingredient, int quantity) {
        return quantities.getOrDefault(ingredient, 0) >= quantity;
    }
}
