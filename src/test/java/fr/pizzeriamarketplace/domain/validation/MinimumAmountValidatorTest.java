package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.catalog.PizzaChain;
import fr.pizzeriamarketplace.domain.catalog.Pizzium;
import fr.pizzeriamarketplace.domain.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MinimumAmountValidatorTest {

    @Test
    void rejectWhenAmountBelowMinimum () {

        PizzaChain chain = new Pizzium();

        Cart cart = new Cart(chain);
        ValidationContext context = new ValidationContext(
                new Restaurant(true),
                new DeliveryZone(List.of("75001")),
                new Stock(Map.of()),
                cart.subtotalCents(),
                "75001",
                cart
        );

MinimumAmountValidator validator = new MinimumAmountValidator() ;

      //  validator.validate(context) ;
assertThrows(IllegalArgumentException.class, ()->validator.validate(context)) ;

    }

}
