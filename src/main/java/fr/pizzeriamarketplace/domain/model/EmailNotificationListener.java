package fr.pizzeriamarketplace.domain.model;

public final class EmailNotificationListener implements OrderStatusListener {
    @Override
    public void onStatusChanged(Order order, OrderStatus previousStatus, OrderStatus newStatus) {
        System.out.printf("Email: order %s changed from %s to %s%n",
                order.getId(), previousStatus, newStatus);
    }
}
