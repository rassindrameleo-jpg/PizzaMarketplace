package fr.pizzeriamarketplace.domain.catalog;

import fr.pizzeriamarketplace.domain.model.Base;
import fr.pizzeriamarketplace.domain.model.Pizza;
import fr.pizzeriamarketplace.domain.model.Size;
import fr.pizzeriamarketplace.domain.pricing.size.SingleSizePricing;
import fr.pizzeriamarketplace.domain.pricing.size.SizePricingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainCatalogTest {
    private final DaMichele daMichele = new DaMichele();
    private final Rossopomodoro rossopomodoro = new Rossopomodoro();
    private final Pizzium pizzium = new Pizzium();
    private final ChainCatalog catalog = new ChainCatalog(List.of(daMichele, rossopomodoro, pizzium));

    @Test
    void findsChainByName() {
        assertSame(rossopomodoro, catalog.findByName("Rossopomodoro").orElseThrow());
    }

    @Test
    void returnsEmptyForUnknownChain() {
        assertTrue(catalog.findByName("Unknown").isEmpty());
    }

    @Test
    void listsAllChainsInGivenOrder() {
        assertEquals(List.of(daMichele, rossopomodoro, pizzium), catalog.getAll());
    }

    @Test
    void rejectsTwoChainsWithSameName() {
        assertThrows(IllegalArgumentException.class, () -> new ChainCatalog(List.of(pizzium, new Pizzium())));
    }

    @Test
    void addingAChainOnlyNeedsANewClass() {
        ChainCatalog extendedCatalog = new ChainCatalog(List.of(daMichele, new TestChain()));

        assertEquals("Test Chain", extendedCatalog.findByName("Test Chain").orElseThrow().getName());
    }

    /**
     * A chain written only for this test: the catalog accepts it without any change.
     */
    private static final class TestChain implements PizzaChain {
        @Override
        public String getName() {
            return "Test Chain";
        }

        @Override
        public List<Pizza> createMenu() {
            return List.of(new Pizza("Test", Base.CREAM, List.of(), 800));
        }

        @Override
        public SizePricingStrategy createSizePricing() {
            return new SingleSizePricing(Size.LARGE);
        }
    }
}
