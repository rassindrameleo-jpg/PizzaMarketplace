package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.Cart;
import fr.pizzeriamarketplace.domain.model.DeliveryZone;
import fr.pizzeriamarketplace.domain.model.Restaurant;
import fr.pizzeriamarketplace.domain.model.Stock;

public class ValidationContext {
    private final Cart cart;
    private final String postalCode;
    private final int subtotalCents;
    private final Restaurant restaurant;
    private final DeliveryZone deliveryZone;
    private final Stock stock;

    public ValidationContext(Restaurant restaurant, DeliveryZone deliveryZone, Stock stock, int subtotalCents, String postalCode, Cart cart) {
        this.restaurant = restaurant;
        this.deliveryZone = deliveryZone;
        this.stock = stock;
        this.subtotalCents = subtotalCents;
        this.postalCode = postalCode;
        this.cart = cart;
    }

    public Cart getCart() {
        return cart;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public int getSubtotalCents() {
        return subtotalCents;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public DeliveryZone getDeliveryZone() {
        return deliveryZone;
    }

    public Stock getStock() {
        return stock;
    }
}
