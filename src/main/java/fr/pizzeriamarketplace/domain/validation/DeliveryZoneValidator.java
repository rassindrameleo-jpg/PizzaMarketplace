package fr.pizzeriamarketplace.domain.validation;

import fr.pizzeriamarketplace.domain.model.DeliveryZone;

import java.util.List;

public class DeliveryZoneValidator extends AbstractValidationHandler {


    private final DeliveryZone deliveryZone;
    public DeliveryZoneValidator(DeliveryZone deliveryZone) {
        this.deliveryZone = deliveryZone;
    }


    @Override
    public void validate(ValidationContext context) {

        String postalCode = context.getPostalCode();

        if (!deliveryZone.contains(postalCode)) {
            throw new IllegalArgumentException(
                    "Zone de livraison non disponible"
            );


        }
        validateNext(context);
    }
}
