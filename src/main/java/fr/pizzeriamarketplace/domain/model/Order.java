package fr.pizzeriamarketplace.domain.model;

import java.util.List;
import java.util.Objects;

public final class Order {
    private final String id;
    private final List<CartLine> lines;
    private OrderStatus status;

    public Order(String id, List<CartLine> lines, OrderStatus status) {
        this.id = id;
        this.lines = lines;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public List<CartLine> getLines() {
        return lines;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "newStatus must not be null");
    }
}
