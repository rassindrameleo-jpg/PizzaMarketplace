import fr.pizzeriamarketplace.application.PlaceOrder;
import fr.pizzeriamarketplace.domain.catalog.DaMichele;
import fr.pizzeriamarketplace.domain.model.Cart;
import fr.pizzeriamarketplace.domain.model.DeliveryZone;
import fr.pizzeriamarketplace.domain.model.EmailNotificationListener;
import fr.pizzeriamarketplace.domain.model.Order;
import fr.pizzeriamarketplace.domain.model.OrderStatus;
import fr.pizzeriamarketplace.domain.model.Restaurant;
import fr.pizzeriamarketplace.domain.model.Size;
import fr.pizzeriamarketplace.domain.model.SmsNotificationListener;
import fr.pizzeriamarketplace.domain.model.Stock;
import fr.pizzeriamarketplace.domain.payment.PaypalAdapter;
import fr.pizzeriamarketplace.domain.payment.PaypalPayment;
import fr.pizzeriamarketplace.domain.payment.StripeAdapter;
import fr.pizzeriamarketplace.domain.payment.StripePayment;
import fr.pizzeriamarketplace.domain.pricing.StandardPricing;
import fr.pizzeriamarketplace.infrastructure.payment.PaymentServiceAdapter;
import fr.pizzeriamarketplace.infrastructure.persistence.InMemoryOrderRepository;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("Pizzeria opening");

        DaMichele pizzaChain = new DaMichele();
        Cart cart = new Cart(pizzaChain);
        cart.addLine(pizzaChain.createMenu().get(1), Size.MEDIUM, 2);
        System.out.println("Cart created: " + cart.getLines().size() + " line(s)");

        InMemoryOrderRepository orderRepository = new InMemoryOrderRepository();
        PaymentServiceAdapter paymentGateway = new PaymentServiceAdapter(
                new StripeAdapter(new StripePayment()),
                new PaypalAdapter(new PaypalPayment()));
        PlaceOrder placeOrder = new PlaceOrder(orderRepository, paymentGateway, new StandardPricing());

        Order order = placeOrder.place(
                cart,
                "customer-1",
                "75001",
                new Restaurant(true),
                new DeliveryZone(List.of("75001", "75002")),
                new Stock(Map.of("Mozzarella", 10, "Jambon", 10, "Champignons", 10)));

        System.out.println("Order paid: " + order.getId() + " - " + order.getStatus());
        order.addStatusListener(new EmailNotificationListener());
        order.addStatusListener(new SmsNotificationListener());

        order.updateStatus(OrderStatus.PREPARING);
        order.updateStatus(OrderStatus.READY);
        order.updateStatus(OrderStatus.DELIVERED);

        System.out.println("Orders saved: " + orderRepository.findAll().size());
    }
}
