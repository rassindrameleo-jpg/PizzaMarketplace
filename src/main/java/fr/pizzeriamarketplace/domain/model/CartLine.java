package fr.pizzeriamarketplace.domain.model;


public final class CartLine {
    private final Pizza pizza;
    private final Size size;
    private final int quantity;

    public CartLine(Pizza pizza, Size size, int quantity) {
        this.pizza = pizza;
        this.size = size;
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
