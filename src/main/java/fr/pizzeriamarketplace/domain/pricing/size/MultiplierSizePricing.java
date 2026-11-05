package fr.pizzeriamarketplace.domain.pricing.size;

import fr.pizzeriamarketplace.domain.model.Size;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Multiplies the medium price by a percentage per size (e.g. Rossopomodoro: S ×0.8, L ×1.3, XL ×1.5).
 * Percentages are integers (80 means ×0.8) to stay in cents without floating-point values.
 * Only the sizes present in the map are offered. The result is rounded to the nearest cent.
 */
public final class MultiplierSizePricing implements SizePricingStrategy {
    private final Map<Size, Integer> percents;

    /**
     * @param percents percentage of the medium price for each offered size, must not be negative
     * @throws IllegalArgumentException if the map is empty or contains a negative percentage
     */
    public MultiplierSizePricing(Map<Size, Integer> percents) {
        Objects.requireNonNull(percents, "percents must not be null");
        if (percents.isEmpty()) {
            throw new IllegalArgumentException("percents must offer at least one size");
        }
        percents.forEach((size, percent) -> {
            Objects.requireNonNull(size, "size must not be null");
            Objects.requireNonNull(percent, "percent for " + size + " must not be null");
            if (percent < 0) {
                throw new IllegalArgumentException("percent for " + size + " must not be negative, got " + percent);
            }
        });
        this.percents = new EnumMap<>(percents);
    }

    @Override
    public boolean supports(Size size) {
        return percents.containsKey(size);
    }

    @Override
    public int priceFor(int mediumPriceCents, Size size) {
        SizePricingChecks.check(this, mediumPriceCents, size);
        return (int) Math.round(mediumPriceCents * (long) percents.get(size) / 100.0);
    }
}
