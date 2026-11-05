package fr.pizzeriamarketplace.domain.model;

import fr.pizzeriamarketplace.domain.pricing.size.SizePricingStrategy;

import java.util.Objects;

public final class CartLine {
    private final Pizza pizza;
    private final Size size;
    private final int quantity;

    /**
     * @throws IllegalArgumentException if quantity is not positive
     */
    public CartLine(Pizza pizza, Size size, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive, got " + quantity);
        }
        this.pizza = Objects.requireNonNull(pizza, "pizza must not be null");
        this.size = Objects.requireNonNull(size, "size must not be null");
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

    /**
     * @param sizePricing the size rule of the chain the line is ordered from
     * @return price of the line in cents: unit price for the size × quantity
     */
    public int totalCents(SizePricingStrategy sizePricing) {
        return sizePricing.priceFor(pizza.getMediumPriceCents(), size) * quantity;
    }
}
