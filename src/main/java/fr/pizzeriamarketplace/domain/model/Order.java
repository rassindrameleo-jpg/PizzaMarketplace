package fr.pizzeriamarketplace.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.Instant;

public final class Order {
    private final String id;
    private final String customerId;
    private final Instant createdAt;
    private final List<CartLine> lines;
    private final List<OrderStatusListener> statusListeners = new CopyOnWriteArrayList<>();
    private OrderStatus status;

    public Order(String id, String customerId, Instant createdAt, List<CartLine> lines, OrderStatus status) {
        this.id = id;
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.lines = lines;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
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
