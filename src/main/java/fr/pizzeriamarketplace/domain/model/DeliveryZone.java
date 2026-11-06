package fr.pizzeriamarketplace.domain.model;

import java.util.List;

public class DeliveryZone {

    private List<String> allowedPostalCodes;

    public DeliveryZone(List<String> allowedPostalCodes) {
        this.allowedPostalCodes = List.copyOf(allowedPostalCodes);
    }

    public boolean contains(String postalCode) {
        return allowedPostalCodes.contains(postalCode);
    }
}
