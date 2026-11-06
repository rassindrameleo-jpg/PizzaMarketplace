package fr.pizzeriamarketplace.application;

import fr.pizzeriamarketplace.application.port.out.PaymentGateway;
import fr.pizzeriamarketplace.domain.catalog.Pizzium;
import fr.pizzeriamarketplace.domain.model.Cart;
import fr.pizzeriamarketplace.domain.model.DeliveryZone;
import fr.pizzeriamarketplace.domain.model.OrderStatus;
import fr.pizzeriamarketplace.domain.model.Restaurant;
import fr.pizzeriamarketplace.domain.model.Size;
import fr.pizzeriamarketplace.domain.model.Stock;
import fr.pizzeriamarketplace.domain.pricing.StandardPricing;
import fr.pizzeriamarketplace.infrastructure.persistence.InMemoryOrderRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlaceOrderTest {
    @Test
    void validatesPaysAndSavesOrder() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        AtomicInteger paidAmountCents = new AtomicInteger();
        PaymentGateway paymentGateway = amountCents -> {
            paidAmountCents.set(amountCents);
            return true;
        };
        PlaceOrder placeOrder = new PlaceOrder(
                repository,
                paymentGateway,
                new StandardPricing(),
                Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC),
                () -> "order-1");
        Cart cart = new Cart(new Pizzium());
        cart.addLine(new Pizzium().createMenu().get(1), Size.MEDIUM, 2);

        var order = placeOrder.place(
                cart,
                "customer-1",
                "75001",
                new Restaurant(true),
                new DeliveryZone(List.of("75001")),
                new Stock(Map.of("Mozzarella", 10, "Jambon", 10)));

        assertEquals(2200, paidAmountCents.get());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(order, repository.findById("order-1").orElseThrow());
        assertEquals(Instant.parse("2026-01-01T12:00:00Z"), order.getCreatedAt());
    }

    @Test
    void doesNotSaveOrderWhenPaymentIsDeclined() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        PaymentGateway paymentGateway = amountCents -> false;
        PlaceOrder placeOrder = new PlaceOrder(repository, paymentGateway, new StandardPricing());
        Cart cart = new Cart(new Pizzium());
        cart.addLine(new Pizzium().createMenu().get(1), Size.MEDIUM, 2);

        assertThrows(IllegalStateException.class, () -> placeOrder.place(
                cart,
                "customer-1",
                "75001",
                new Restaurant(true),
                new DeliveryZone(List.of("75001")),
                new Stock(Map.of("Mozzarella", 10, "Jambon", 10))));

        assertEquals(0, repository.findAll().size());
    }

    @Test
    void doesNotAttemptPaymentOrSaveWhenCartIsEmpty() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        AtomicInteger paymentAttempts = new AtomicInteger();
        PaymentGateway paymentGateway = amountCents -> {
            paymentAttempts.incrementAndGet();
            return true;
        };
        PlaceOrder placeOrder = new PlaceOrder(repository, paymentGateway, new StandardPricing());

        assertThrows(IllegalArgumentException.class, () -> placeOrder.place(
                new Cart(new Pizzium()),
                "customer-1",
                "75001",
                new Restaurant(true),
                new DeliveryZone(List.of("75001")),
                new Stock(Map.of())));

        assertEquals(0, paymentAttempts.get());
        assertEquals(0, repository.findAll().size());
    }
}
