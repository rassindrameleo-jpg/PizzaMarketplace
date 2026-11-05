package fr.pizzeriamarketplace.application.usecase;

import fr.pizzeriamarketplace.application.port.out.OrderRepository;
import fr.pizzeriamarketplace.domain.model.Order;
import fr.pizzeriamarketplace.domain.model.OrderStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Returns a customer's order history, most recent first.
 * Reads orders through the {@link OrderRepository} port, so it does not depend on how orders are stored.
 */
public final class GetOrderHistory {
    private static final Comparator<Order> MOST_RECENT_FIRST =
            Comparator.comparing(Order::getCreatedAt).reversed();

    private final OrderRepository orderRepository;

    public GetOrderHistory(OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository must not be null");
    }

    /**
     * @param customerId the customer whose orders are returned
     * @return all orders of the customer, most recent first
     */
    public List<Order> forCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .sorted(MOST_RECENT_FIRST)
                .toList();
    }

    /**
     * @param customerId the customer whose orders are returned
     * @param status     only orders with this status are returned
     * @return matching orders of the customer, most recent first
     */
    public List<Order> forCustomer(String customerId, OrderStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        return orderRepository.findByCustomerId(customerId).stream()
                .filter(order -> order.getStatus() == status)
                .sorted(MOST_RECENT_FIRST)
                .toList();
    }
}