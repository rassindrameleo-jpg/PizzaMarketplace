package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.Cart;
import fr.pizzeriamarketplace.domain.model.DeliveryZone;
import fr.pizzeriamarketplace.domain.model.Restaurant;
import fr.pizzeriamarketplace.domain.model.Stock;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MinimumAmountValidatorTest {

    @Test
    void rejectWhenAmountBelowMinimum () {
        Cart cart = new Cart () ;
        ValidationContext context = new ValidationContext(
                new Restaurant(true),
                new DeliveryZone(List.of("75001")),
                new Stock(Map.of()),
                2000,
                "75001",
                cart
        );

MinimumAmountValidator validator = new MinimumAmountValidator() ;

      //  validator.validate(context) ;
assertThrows(IllegalArgumentException.class, ()->validator.validate(context)) ;

    }

}
