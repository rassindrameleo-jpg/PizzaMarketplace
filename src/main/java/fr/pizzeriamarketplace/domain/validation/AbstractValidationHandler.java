package fr.pizzeriamarketplace.domain.validation;

public  abstract class AbstractValidationHandler implements ValidationHandler{

    private ValidationHandler next;

    @Override
    public void setNext(ValidationHandler next) {
        this.next = next;
    };


    protected void validateNext(ValidationContext context) {
        if (next != null) {
            next.validate(context);
        }
    }
}
