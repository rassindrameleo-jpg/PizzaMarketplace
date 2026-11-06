package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.Cart;

public class NonEmptyCartValidator extends AbstractValidationHandler{






    @Override
    public void validate(ValidationContext context) {
        if(context.getCart().isEmpty()){
            throw new IllegalArgumentException("le panier est vide");
        }

        validateNext(context);
    }
}
