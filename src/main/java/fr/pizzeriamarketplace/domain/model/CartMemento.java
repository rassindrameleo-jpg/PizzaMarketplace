package fr.pizzeriamarketplace.domain.model;

import java.util.List;

public final class CartMemento {
    private final List<CartLine> lines;

    CartMemento(List<CartLine> lines) {
        this.lines = List.copyOf(lines);
    }

    List<CartLine> getLines() {
        return lines;
    }
}
