package fr.pizzeriamarketplace.domain.pricing.size;

import fr.pizzeriamarketplace.domain.model.Size;

import java.util.Objects;

/**
 * Offers a single size at the medium price (e.g. Pizzium: M only).
 * Any other size is refused.
 */
public final class SingleSizePricing implements SizePricingStrategy {
    private final Size offeredSize;

    /**
     * @param offeredSize the only size this chain sells
     */
    public SingleSizePricing(Size offeredSize) {
        this.offeredSize = Objects.requireNonNull(offeredSize, "offeredSize must not be null");
    }

    @Override
    public boolean supports(Size size) {
        return size == offeredSize;
    }

    @Override
    public int priceFor(int mediumPriceCents, Size size) {
        SizePricingChecks.check(this, mediumPriceCents, size);
        return mediumPriceCents;
    }
}
