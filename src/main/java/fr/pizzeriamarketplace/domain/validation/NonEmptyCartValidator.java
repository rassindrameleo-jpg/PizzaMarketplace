package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.Cart;

public class NonEmptyCartValidator implements ValidationHandler{






    @Override
    public void validate(ValidationContext context) {
        if(context.getCart().isEmpty()){
            throw new IllegalArgumentException("le panier est vide ");
        }
    }
}
