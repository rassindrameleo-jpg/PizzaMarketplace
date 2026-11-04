package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.Cart;

import fr.pizzeriamarketplace.domain.model.Pizza;
import fr.pizzeriamarketplace.domain.model.Size;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class NonEmptyCartValidatorTest {



    @Test
    void rejectWhenCartIsEmpty (){
        Cart cart = new Cart();
        NonEmptyCartValidator validator = new NonEmptyCartValidator();

assertThrows(IllegalArgumentException.class , ()->validator.validate(cart)) ;
    }

    @Test
    void acceptWhenCartNotEmpty () {
       Cart cart = new Cart() ;
       NonEmptyCartValidator validator = new NonEmptyCartValidator();

       Pizza pizza = new Pizza("Margeritta",null , List.of()) ;
cart.addLine(pizza , Size.MEDIUM , 1);

validator.validate(cart);

    }
}
