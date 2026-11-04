package fr.pizzeriamarketplace.domain.model;

import java.util.Objects;

public final class CartLine {
    private final Pizza pizza;
    private final Size size;
    private final int quantity;

    public CartLine(Pizza pizza, Size size, int quantity) {
        this.pizza = Objects.requireNonNull(pizza, "pizza must not be null");
        this.size = Objects.requireNonNull(size, "size must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        this.quantity = quantity;
    }

    public Pizza getPizza() {
        return pizza;
    }

    public Size getSize() {
        return size;
    }

    public int getQuantity() {
        return quantity;
    }
}
