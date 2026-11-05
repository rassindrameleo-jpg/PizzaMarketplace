package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.*;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class NonEmptyCartValidatorTest {



    @Test
    void rejectWhenCartIsEmpty (){
        Cart cart = new Cart();
        NonEmptyCartValidator validator = new NonEmptyCartValidator();
        ValidationContext context = new ValidationContext(
                new Restaurant(true),
                new DeliveryZone(List.of("75001")),
                new Stock(Map.of()),
                2000,
                "75001",
                cart
        );

assertThrows(IllegalArgumentException.class , ()->validator.validate(context)) ;
    }

    @Test
    void acceptWhenCartNotEmpty () {
       Cart cart = new Cart() ;
       NonEmptyCartValidator validator = new NonEmptyCartValidator();

       Pizza pizza = new Pizza("Margeritta",null , List.of()) ;
cart.addLine(pizza , Size.MEDIUM , 1);

          ValidationContext context = new ValidationContext(
                new Restaurant(true),
                new DeliveryZone(List.of("75001")),
                new Stock(Map.of()),
                2000,
                "75001",
                cart
        );




        validator.validate(context);
    }

    @Test
    void stopChainWhenCartIsEmpty(){
        Cart cart = new Cart() ;
        Pizza pizza = new Pizza("Margeritta",null , List.of()) ;
        cart.addLine(pizza , Size.MEDIUM , 1);

        ValidationContext context = new ValidationContext(
                new Restaurant(true),
                new DeliveryZone(List.of("75001")),
                new Stock(Map.of()),
                2000,
                "75001",
                cart
        );
NonEmptyCartValidator validator1 = new NonEmptyCartValidator() ;
MinimumAmountValidator validator2 = new MinimumAmountValidator() ;
validator1.setNext(validator2);
      //  validator1.validate(context) ;
assertThrows( IllegalArgumentException.class , ()->validator1.validate(context)) ;

    }
    }

