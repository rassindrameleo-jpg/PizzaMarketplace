package fr.pizzeriamarketplace.application.port.out;

import fr.pizzeriamarketplace.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);

    Optional<Order> findById(String orderId);

    List<Order> findAll();

    List<Order> findByCustomerId(String customerId);
}
