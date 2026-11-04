package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.Cart;

public interface ValidationHandler {

    void validate(ValidationContext context); ;
}
