package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OrderValidationChainTest {



    @Test
    void shouldAcceptValidOrder(){

        Ingredient tomate = new Ingredient("tomate" , 100) ;
        Pizza pizza = new Pizza("Margherita", null , List.of(tomate)) ;

        Cart cart = new Cart() ;
        cart.addLine(pizza , Size.MEDIUM , 2);
        Restaurant restaurant = new Restaurant(true);
        DeliveryZone deliveryZone =
                new DeliveryZone(List.of("75001", "75002"));

        Stock stock = new Stock(Map.of("tomate", 10)) ;

        ValidationContext context = new ValidationContext(
                restaurant,
                deliveryZone ,
                stock ,
                2000 ,
                "75001" ,
                cart
        );
        NonEmptyCartValidator validator1 = new NonEmptyCartValidator();
        MinimumAmountValidator validator2 = new MinimumAmountValidator();
        DeliveryZoneValidator validator3 = new DeliveryZoneValidator(deliveryZone);
        StoreOpenValidator validator4 = new StoreOpenValidator();
        StockValidator validator5 = new StockValidator();

        validator1.setNext(validator2);
        validator2.setNext(validator3);
        validator3.setNext(validator4);
        validator4.setNext(validator5);

        assertDoesNotThrow(() -> validator1.validate(context));


    }



}
