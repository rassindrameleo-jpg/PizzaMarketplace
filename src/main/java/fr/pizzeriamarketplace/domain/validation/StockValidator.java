package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.CartLine;
import fr.pizzeriamarketplace.domain.model.Ingredient;
import fr.pizzeriamarketplace.domain.model.Stock;

import java.util.HashMap;
import java.util.Map;

public class StockValidator  extends AbstractValidationHandler {
    @Override
    public void validate(ValidationContext context) {
        Stock stock = context.getStock() ;
        Map<String, Integer> required = new HashMap<>();

        for (CartLine line  : context.getCart().getLines() ){
            for (Ingredient ingredient : line.getPizza().getIngredients()){

                String name = ingredient.getName() ;

                        int quantity = line.getQuantity();

                required.put(name ,  required.getOrDefault(name, 0) + quantity);
            }
        }

for(Map.Entry<String ,Integer > entry : required.entrySet()){

    if(!stock.isAvailable(entry.getKey(), entry.getValue())){

        throw new IllegalArgumentException( "Stock insuffisant : " + entry.getKey()) ;
    }
}

        validateNext(context);
    }
}
