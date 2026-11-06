package fr.pizzeriamarketplace.domain.model;

import fr.pizzeriamarketplace.domain.catalog.DaMichele;
import fr.pizzeriamarketplace.domain.catalog.Pizzium;
import fr.pizzeriamarketplace.domain.catalog.Rossopomodoro;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartSubtotalTest {
    private final Pizza margherita = new Pizza("Margherita", Base.TOMATO, List.of(), 900);
    private final Pizza supreme = new Pizza("Supreme", Base.TOMATO, List.of(), 1200);

    @Test
    void appliesChainSizeRuleAndQuantity() {
        Cart cart = new Cart(new DaMichele());
        cart.addLine(margherita, Size.LARGE, 2);
        cart.addLine(margherita, Size.SMALL, 1);

        int subtotal = cart.subtotalCents();

        assertEquals(1200 * 2 + 700, subtotal);
    }

    @Test
    void sameLinesCostDifferentlyDependingOnChain() {
        Cart cart = new Cart(new Rossopomodoro());
        cart.addLine(supreme, Size.EXTRA_LARGE, 1);

        int subtotal = cart.subtotalCents();

        assertEquals(1800, subtotal);
    }

    @Test
    void subtotalFollowsChangeSizeAndUndo() {
        Cart cart = new Cart(new DaMichele());
        cart.addLine(margherita, Size.MEDIUM, 1);
        cart.changeSize(0, Size.EXTRA_LARGE);

        assertEquals(1400, cart.subtotalCents());
        cart.undo();
        assertEquals(900, cart.subtotalCents());
    }

    @Test
    void emptyCartCostsNothing() {
        assertEquals(0, new Cart(new Pizzium()).subtotalCents());
    }

    @Test
    void refusesSizeNotOfferedByChain() {
        Cart cart = new Cart(new Pizzium());

        assertThrows(IllegalArgumentException.class, () -> cart.addLine(margherita, Size.LARGE, 1));
        assertTrue(cart.isEmpty());
        assertFalse(cart.canUndo());
    }

    @Test
    void refusesChangeToSizeNotOfferedByChain() {
        Cart cart = new Cart(new Pizzium());
        cart.addLine(margherita, Size.MEDIUM, 1);

        assertThrows(IllegalArgumentException.class, () -> cart.changeSize(0, Size.SMALL));
        assertEquals(Size.MEDIUM, cart.getLines().get(0).getSize());
    }

    @Test
    void orderKeepsTheSubtotalOfItsCart() {
        Cart cart = new Cart(new DaMichele());
        cart.addLine(margherita, Size.LARGE, 1);

        Order order = new Order("order-1", "customer-1", Instant.now(), cart.getChain(), cart.getLines(),
                OrderStatus.PENDING);

        assertEquals(cart.subtotalCents(), order.subtotalCents());
    }
}
