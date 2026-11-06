package fr.pizzeriamarketplace.domain.model;

public final class SmsNotificationListener implements OrderStatusListener {
    @Override
    public void onStatusChanged(Order order, OrderStatus previousStatus, OrderStatus newStatus) {
        System.out.printf("SMS: order %s changed from %s to %s%n",
                order.getId(), previousStatus, newStatus);
    }
}
