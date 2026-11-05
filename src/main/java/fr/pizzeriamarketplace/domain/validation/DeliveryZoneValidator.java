package fr.pizzeriamarketplace.domain.validation;

import java.util.List;

public class DeliveryZoneValidator extends AbstractValidationHandler {

    private List<String> allowedPostalCodes;

    public DeliveryZoneValidator(List<String> allowedPostalCodes) {
        this.allowedPostalCodes = allowedPostalCodes;
    }

    @Override
    public void validate(ValidationContext context) {

        String postalCode = context.getPostalCode();

        if (!allowedPostalCodes.contains(postalCode)) {
            throw new IllegalArgumentException(
                    "Zone de livraison non disponible"
            );


        }
        validateNext(context);
    }
}
