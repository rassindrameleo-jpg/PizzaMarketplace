package fr.pizzeriamarketplace.domain.model;

import java.util.ArrayList;
import java.util.List;

public final class Cart {
    private final List<CartLine> lines = new ArrayList<>();

    public void addLine(Pizza pizza, Size size, int quantity) {
        lines.add(new CartLine(pizza, size, quantity));
    }

    public CartLine removeLine(int index) {
        return lines.remove(index);
    }

    public List<CartLine> getLines() {
        return List.copyOf(lines);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public void clear() {
        lines.clear();
    }
}
