package fr.pizzeriamarketplace.domain.pricing.size;

import fr.pizzeriamarketplace.domain.model.Size;

import java.util.Objects;

/**
 * Argument checks shared by every {@link SizePricingStrategy}, so each strategy only holds its own formula.
 */
final class SizePricingChecks {
    private SizePricingChecks() {
        // Utility class, not meant to be instantiated
    }

    static void check(SizePricingStrategy pricing, int mediumPriceCents, Size size) {
        Objects.requireNonNull(size, "size must not be null");
        if (mediumPriceCents < 0) {
            throw new IllegalArgumentException("mediumPriceCents must not be negative, got " + mediumPriceCents);
        }
        if (!pricing.supports(size)) {
            throw new IllegalArgumentException("size " + size + " is not offered");
        }
    }
}
