package fr.pizzeriamarketplace.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderObserverTest {
    @Test
    void notifiesAllActiveListenersWhenStatusChanges() {
        Order order = new Order("order-1", List.of(), OrderStatus.PENDING);
        List<String> notifications = new ArrayList<>();
        OrderStatusListener firstListener = (updatedOrder, previous, current) ->
                notifications.add("first:" + previous + "->" + current);
        OrderStatusListener secondListener = (updatedOrder, previous, current) ->
                notifications.add("second:" + updatedOrder.getId());

        order.addStatusListener(firstListener);
        order.addStatusListener(secondListener);
        order.updateStatus(OrderStatus.PAID);

        assertEquals(List.of("first:PENDING->PAID", "second:order-1"), notifications);
    }

    @Test
    void removedListenerIsNotNotifiedAndSameStatusDoesNotNotify() {
        Order order = new Order("order-1", List.of(), OrderStatus.PENDING);
        List<OrderStatus> notifications = new ArrayList<>();
        OrderStatusListener listener = (updatedOrder, previous, current) -> notifications.add(current);

        order.addStatusListener(listener);
        order.updateStatus(OrderStatus.PAID);
        order.removeStatusListener(listener);
        order.updateStatus(OrderStatus.PREPARING);
        order.updateStatus(OrderStatus.PREPARING);

        assertEquals(List.of(OrderStatus.PAID), notifications);
    }
}
