package fr.pizzeriamarketplace.domain.validation;

public class MinimumAmountValidator implements ValidationHandler{
    @Override
    public void validate(ValidationContext context) {
        if (context.getSubtotalCents()<1500){

            throw new IllegalArgumentException("Mantant minimun 15 €") ;
        }
    }
}
