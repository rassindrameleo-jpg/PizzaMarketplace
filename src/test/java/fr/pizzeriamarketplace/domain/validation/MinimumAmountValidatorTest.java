package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.Cart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MinimumAmountValidatorTest {

    @Test
    void rejectWhenAmountBelowMinimum () {
        Cart cart = new Cart () ;
        ValidationContext context =  new ValidationContext(cart ,  "75001" , 1100) ;

MinimumAmountValidator validator = new MinimumAmountValidator() ;

      //  validator.validate(context) ;
assertThrows(IllegalArgumentException.class, ()->validator.validate(context)) ;

    }

}
