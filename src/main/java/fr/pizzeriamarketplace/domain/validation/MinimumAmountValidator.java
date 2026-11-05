package fr.pizzeriamarketplace.domain.validation;

public class MinimumAmountValidator extends AbstractValidationHandler{
    @Override
    public void validate(ValidationContext context) {
        if (context.getSubtotalCents()<1500){

            throw new IllegalArgumentException("Mantant minimun 15 €") ;
        }

        validateNext(context);
    }
}
