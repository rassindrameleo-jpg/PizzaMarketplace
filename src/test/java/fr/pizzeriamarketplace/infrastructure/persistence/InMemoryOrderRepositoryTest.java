package fr.pizzeriamarketplace.infrastructure.persistence;

import fr.pizzeriamarketplace.domain.model.Base;
import fr.pizzeriamarketplace.domain.model.CartLine;
import fr.pizzeriamarketplace.domain.model.Ingredient;
import fr.pizzeriamarketplace.domain.model.Order;
import fr.pizzeriamarketplace.domain.model.OrderStatus;
import fr.pizzeriamarketplace.domain.model.Pizza;
import fr.pizzeriamarketplace.domain.model.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryOrderRepositoryTest {
    private InMemoryOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
    }

    @Test
    void savesAndFindsOrderById() {
        Order order = createOrder("order-1");

        repository.save(order);

        assertEquals(order, repository.findById("order-1").orElseThrow());
    }

    @Test
    void returnsEmptyWhenOrderDoesNotExist() {
        assertTrue(repository.findById("unknown").isEmpty());
    }

    @Test
    void returnsAllSavedOrdersInInsertionOrder() {
        Order firstOrder = createOrder("order-1");
        Order secondOrder = createOrder("order-2");
        repository.save(firstOrder);
        repository.save(secondOrder);

        assertEquals(List.of(firstOrder, secondOrder), repository.findAll());
    }

    @Test
    void returnedOrderListCannotModifyRepositoryContents() {
        repository.save(createOrder("order-1"));
        List<Order> savedOrders = repository.findAll();

        assertThrows(UnsupportedOperationException.class, () -> savedOrders.clear());
        assertEquals(1, repository.findAll().size());
    }

    private Order createOrder(String id) {
        Ingredient cheese = new Ingredient("Cheese", 100);
        Pizza pizza = new Pizza("Margherita", Base.TOMATO, List.of(cheese), 900);
        CartLine line = new CartLine(pizza, Size.MEDIUM, 1);
        return new Order(id, "customer-1", Instant.now(), List.of(line), OrderStatus.PENDING);
    }
}
