package fr.pizzeriamarketplace.domain.model;

@FunctionalInterface
public interface OrderStatusListener {
    void onStatusChanged(Order order, OrderStatus previousStatus, OrderStatus newStatus);
}
