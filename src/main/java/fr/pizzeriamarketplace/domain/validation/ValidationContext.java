package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.Cart;

public class ValidationContext {
    private Cart cart ;
    private String PostalCode ;
    private int subtotalCents; ;

    public ValidationContext(Cart cart, String PostalCode, int subtotalCents) {
        this.cart = cart;
        this.PostalCode = PostalCode;
        this.subtotalCents = subtotalCents;
    }


    public Cart getCart() {
        return cart;
    }

    public String getPostalCode() {
        return PostalCode;
    }

    public int getSubtotalCents() {
        return subtotalCents;
    }
}
