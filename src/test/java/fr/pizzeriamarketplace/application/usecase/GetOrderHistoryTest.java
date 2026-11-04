package fr.pizzeriamarketplace.application.usecase;

import fr.pizzeriamarketplace.domain.model.Order;
import fr.pizzeriamarketplace.domain.model.OrderStatus;
import fr.pizzeriamarketplace.infrastructure.persistence.InMemoryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetOrderHistoryTest {
    private static final Instant MONDAY = Instant.parse("2026-11-02T12:00:00Z");
    private static final Instant TUESDAY = Instant.parse("2026-11-03T12:00:00Z");
    private static final Instant WEDNESDAY = Instant.parse("2026-11-04T12:00:00Z");

    private InMemoryOrderRepository repository;
    private GetOrderHistory getOrderHistory;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
        getOrderHistory = new GetOrderHistory(repository);
    }

    @Test
    void returnsCustomerOrdersMostRecentFirst() {
        Order monday = order("order-1", "alice", MONDAY, OrderStatus.DELIVERED);
        Order wednesday = order("order-2", "alice", WEDNESDAY, OrderStatus.PENDING);
        Order tuesday = order("order-3", "alice", TUESDAY, OrderStatus.PAID);
        repository.save(monday);
        repository.save(wednesday);
        repository.save(tuesday);

        List<Order> history = getOrderHistory.forCustomer("alice");

        assertEquals(List.of(wednesday, tuesday, monday), history);
    }

    @Test
    void doesNotReturnOtherCustomersOrders() {
        Order aliceOrder = order("order-1", "alice", MONDAY, OrderStatus.PAID);
        Order bobOrder = order("order-2", "bob", TUESDAY, OrderStatus.PAID);
        repository.save(aliceOrder);
        repository.save(bobOrder);

        List<Order> history = getOrderHistory.forCustomer("alice");

        assertEquals(List.of(aliceOrder), history);
    }

    @Test
    void filtersByStatus() {
        Order delivered = order("order-1", "alice", MONDAY, OrderStatus.DELIVERED);
        Order pending = order("order-2", "alice", TUESDAY, OrderStatus.PENDING);
        Order alsoDelivered = order("order-3", "alice", WEDNESDAY, OrderStatus.DELIVERED);
        repository.save(delivered);
        repository.save(pending);
        repository.save(alsoDelivered);

        List<Order> history = getOrderHistory.forCustomer("alice", OrderStatus.DELIVERED);

        assertEquals(List.of(alsoDelivered, delivered), history);
    }

    @Test
    void returnsEmptyHistoryForUnknownCustomer() {
        repository.save(order("order-1", "alice", MONDAY, OrderStatus.PAID));

        assertTrue(getOrderHistory.forCustomer("unknown").isEmpty());
    }

    private Order order(String id, String customerId, Instant createdAt, OrderStatus status) {
        return new Order(id, customerId, createdAt, List.of(), status);
    }
}