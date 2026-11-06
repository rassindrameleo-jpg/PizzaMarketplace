package fr.pizzeriamarketplace.domain.pricing.size;

import fr.pizzeriamarketplace.domain.model.Size;

public interface SizePricingStrategy {
    boolean supports(Size size);

    /**
     * @param mediumPriceCents prix de référence (taille M)
     * @throws IllegalArgumentException si la taille n'est pas proposée
     */
    int priceFor(int mediumPriceCents, Size size);
}
