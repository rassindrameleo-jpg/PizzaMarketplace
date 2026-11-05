package fr.pizzeriamarketplace.domain.validation;

public class StoreOpenValidator extends AbstractValidationHandler{
    @Override
    public void validate(ValidationContext context) {
        if(!context.getRestaurant().isOpen()){


            throw new IllegalArgumentException("store not oppen yet ") ;
        }

        validateNext(context);
    }
}
