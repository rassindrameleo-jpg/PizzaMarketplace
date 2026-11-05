package fr.pizzeriamarketplace.domain.pricing.size;

import fr.pizzeriamarketplace.domain.model.Size;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Adds a fixed amount per size to the medium price (e.g. Da Michele: S -2€, L +3€, XL +5€).
 * Only the sizes present in the map are offered. The result never goes below zero.
 */
public final class FixedSurchargeSizePricing implements SizePricingStrategy {
    private final Map<Size, Integer> surchargesCents;

    /**
     * @param surchargesCents amount in cents added for each offered size (may be negative)
     * @throws IllegalArgumentException if the map is empty
     */
    public FixedSurchargeSizePricing(Map<Size, Integer> surchargesCents) {
        Objects.requireNonNull(surchargesCents, "surchargesCents must not be null");
        if (surchargesCents.isEmpty()) {
            throw new IllegalArgumentException("surchargesCents must offer at least one size");
        }
        surchargesCents.forEach((size, surcharge) -> {
            Objects.requireNonNull(size, "size must not be null");
            Objects.requireNonNull(surcharge, "surcharge for " + size + " must not be null");
        });
        this.surchargesCents = new EnumMap<>(surchargesCents);
    }

    @Override
    public boolean supports(Size size) {
        return surchargesCents.containsKey(size);
    }

    @Override
    public int priceFor(int mediumPriceCents, Size size) {
        SizePricingChecks.check(this, mediumPriceCents, size);
        return Math.max(0, mediumPriceCents + surchargesCents.get(size));
    }
}
