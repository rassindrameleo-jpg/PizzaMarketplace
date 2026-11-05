package fr.pizzeriamarketplace.domain.model;

import fr.pizzeriamarketplace.domain.catalog.PizzaChain;
import fr.pizzeriamarketplace.domain.pricing.size.SizePricingStrategy;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * A cart belongs to a single chain: the customer chooses a chain, then composes the order.
 */
public final class Cart {
    private static final int MAX_HISTORY_SIZE = 10;
    private final PizzaChain chain;
    private final SizePricingStrategy sizePricing;
    private final List<CartLine> lines = new ArrayList<>();
    private final Deque<CartMemento> undoHistory = new ArrayDeque<>();
    private final Deque<CartMemento> redoHistory = new ArrayDeque<>();

    public Cart(PizzaChain chain) {
        this.chain = Objects.requireNonNull(chain, "chain must not be null");
        this.sizePricing = chain.createSizePricing();
    }

    public PizzaChain getChain() {
        return chain;
    }

    /**
     * @throws IllegalArgumentException if the chain does not offer this size
     */
    public void addLine(Pizza pizza, Size size, int quantity) {
        CartLine line = new CartLine(pizza, size, quantity);
        requireOfferedSize(size);
        saveCurrentState();
        lines.add(line);
    }

    public CartLine removeLine(int index) {
        CartLine removedLine = lines.get(index);
        saveCurrentState();
        lines.remove(index);
        return removedLine;
    }

    /**
     * @throws IllegalArgumentException if the chain does not offer this size
     */
    public void changeSize(int index, Size size) {
        Objects.requireNonNull(size, "size must not be null");
        CartLine currentLine = lines.get(index);
        if (currentLine.getSize() == size) {
            return;
        }

        requireOfferedSize(size);
        saveCurrentState();
        lines.set(index, new CartLine(currentLine.getPizza(), size, currentLine.getQuantity()));
    }

    public List<CartLine> getLines() {
        return List.copyOf(lines);
    }

    /**
     * @return sum of the lines, with the chain's size rule applied, before any discount
     */
    public int subtotalCents() {
        return lines.stream().mapToInt(line -> line.totalCents(sizePricing)).sum();
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public void clear() {
        if (lines.isEmpty()) {
            return;
        }

        saveCurrentState();
        lines.clear();
    }

    public boolean undo() {
        if (undoHistory.isEmpty()) {
            return false;
        }

        redoHistory.push(new CartMemento(lines));
        restore(undoHistory.pop());
        return true;
    }

    public boolean redo() {
        if (redoHistory.isEmpty()) {
            return false;
        }

        undoHistory.push(new CartMemento(lines));
        restore(redoHistory.pop());
        return true;
    }

    public boolean canUndo() {
        return !undoHistory.isEmpty();
    }

    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }

    private void requireOfferedSize(Size size) {
        if (!sizePricing.supports(size)) {
            throw new IllegalArgumentException(chain.getName() + " does not offer size " + size);
        }
    }

    private void saveCurrentState() {
        undoHistory.push(new CartMemento(lines));
        while (undoHistory.size() > MAX_HISTORY_SIZE) {
            undoHistory.removeLast();
        }
        redoHistory.clear();
    }

    private void restore(CartMemento memento) {
        lines.clear();
        lines.addAll(memento.getLines());
    }
}
