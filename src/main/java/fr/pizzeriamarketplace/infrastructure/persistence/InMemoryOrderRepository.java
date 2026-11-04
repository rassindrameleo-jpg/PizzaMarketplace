package fr.pizzeriamarketplace.infrastructure.persistence;

import fr.pizzeriamarketplace.application.port.out.OrderRepository;
import fr.pizzeriamarketplace.domain.model.Order;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> orders = new LinkedHashMap<>();

    @Override
    public void save(Order order) {
        Objects.requireNonNull(order, "order must not be null");
        orders.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public List<Order> findAll() {
        return List.copyOf(orders.values());
    }
}
