package fr.pizzeriamarketplace.domain.model;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public final class Cart {
    private static final int MAX_HISTORY_SIZE = 10;
    private final List<CartLine> lines = new ArrayList<>();
    private final Deque<CartMemento> undoHistory = new ArrayDeque<>();
    private final Deque<CartMemento> redoHistory = new ArrayDeque<>();

    public void addLine(Pizza pizza, Size size, int quantity) {
        saveCurrentState();
        lines.add(new CartLine(pizza, size, quantity));
    }

    public CartLine removeLine(int index) {
        CartLine removedLine = lines.get(index);
        saveCurrentState();
        lines.remove(index);
        return removedLine;
    }

    public void changeSize(int index, Size size) {
        Objects.requireNonNull(size, "size must not be null");
        CartLine currentLine = lines.get(index);
        if (currentLine.getSize() == size) {
            return;
        }

        saveCurrentState();
        lines.set(index, new CartLine(currentLine.getPizza(), size, currentLine.getQuantity()));
    }

    public List<CartLine> getLines() {
        return List.copyOf(lines);
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
