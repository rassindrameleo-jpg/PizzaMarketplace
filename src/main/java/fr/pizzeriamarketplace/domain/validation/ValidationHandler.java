package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.Cart;

public interface ValidationHandler {


   void setNext(ValidationHandler next) ;
    void validate(ValidationContext context); ;
}
