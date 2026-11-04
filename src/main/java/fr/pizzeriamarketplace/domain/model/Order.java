package fr.pizzeriamarketplace.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Order {
    private final String id;
    private final List<CartLine> lines;
    private final List<OrderStatusListener> statusListeners = new CopyOnWriteArrayList<>();
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

    public void addStatusListener(OrderStatusListener listener) {
        statusListeners.add(Objects.requireNonNull(listener, "listener must not be null"));
    }

    public void removeStatusListener(OrderStatusListener listener) {
        statusListeners.remove(Objects.requireNonNull(listener, "listener must not be null"));
    }

    public void updateStatus(OrderStatus newStatus) {
        Objects.requireNonNull(newStatus, "newStatus must not be null");
        if (status == newStatus) {
            return;
        }

        OrderStatus previousStatus = status;
        status = newStatus;
        for (OrderStatusListener listener : statusListeners) {
            listener.onStatusChanged(this, previousStatus, newStatus);
        }
    }
}
