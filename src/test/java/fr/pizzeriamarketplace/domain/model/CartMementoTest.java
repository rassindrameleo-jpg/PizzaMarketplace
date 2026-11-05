package fr.pizzeriamarketplace.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartMementoTest {
    private final Pizza pizza = new Pizza("Margherita", Base.TOMATO, List.of(), 900);

    @Test
    void undoesAndRedoesCartChanges() {
        Cart cart = new Cart();
        cart.addLine(pizza, Size.MEDIUM, 1);
        cart.changeSize(0, Size.LARGE);

        assertTrue(cart.undo());
        assertEquals(Size.MEDIUM, cart.getLines().get(0).getSize());
        assertTrue(cart.redo());
        assertEquals(Size.LARGE, cart.getLines().get(0).getSize());
    }

    @Test
    void newChangeClearsRedoHistory() {
        Cart cart = new Cart();
        cart.addLine(pizza, Size.MEDIUM, 1);
        cart.changeSize(0, Size.LARGE);
        cart.undo();
        cart.changeSize(0, Size.SMALL);

        assertFalse(cart.canRedo());
        assertEquals(Size.SMALL, cart.getLines().get(0).getSize());
    }

    @Test
    void keepsOnlyTheTenMostRecentChanges() {
        Cart cart = new Cart();
        for (int i = 0; i < 11; i++) {
            cart.addLine(pizza, Size.MEDIUM, 1);
        }

        int undoCount = 0;
        while (cart.undo()) {
            undoCount++;
        }

        assertEquals(10, undoCount);
        assertEquals(1, cart.getLines().size());
    }
}
