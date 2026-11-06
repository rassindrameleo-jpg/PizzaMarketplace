package fr.pizzeriamarketplace.application;

import fr.pizzeriamarketplace.application.port.out.OrderRepository;
import fr.pizzeriamarketplace.domain.payment.PaymentGateway;
import fr.pizzeriamarketplace.domain.model.Cart;
import fr.pizzeriamarketplace.domain.model.DeliveryZone;
import fr.pizzeriamarketplace.domain.model.Order;
import fr.pizzeriamarketplace.domain.model.OrderStatus;
import fr.pizzeriamarketplace.domain.model.Restaurant;
import fr.pizzeriamarketplace.domain.model.Stock;
import fr.pizzeriamarketplace.domain.pricing.PricingStrategy;
import fr.pizzeriamarketplace.domain.validation.DeliveryZoneValidator;
import fr.pizzeriamarketplace.domain.validation.MinimumAmountValidator;
import fr.pizzeriamarketplace.domain.validation.NonEmptyCartValidator;
import fr.pizzeriamarketplace.domain.validation.StockValidator;
import fr.pizzeriamarketplace.domain.validation.StoreOpenValidator;
import fr.pizzeriamarketplace.domain.validation.ValidationContext;
import fr.pizzeriamarketplace.domain.validation.ValidationHandler;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public final class PlaceOrder {
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final PricingStrategy pricingStrategy;
    private final Clock clock;
    private final Supplier<String> orderIdSupplier;

    public PlaceOrder(
            OrderRepository orderRepository,
            PaymentGateway paymentGateway,
            PricingStrategy pricingStrategy) {
        this(orderRepository, paymentGateway, pricingStrategy, Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public PlaceOrder(
            OrderRepository orderRepository,
            PaymentGateway paymentGateway,
            PricingStrategy pricingStrategy,
            Clock clock,
            Supplier<String> orderIdSupplier) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository must not be null");
        this.paymentGateway = Objects.requireNonNull(paymentGateway, "paymentGateway must not be null");
        this.pricingStrategy = Objects.requireNonNull(pricingStrategy, "pricingStrategy must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.orderIdSupplier = Objects.requireNonNull(orderIdSupplier, "orderIdSupplier must not be null");
    }

    public Order place(
            Cart cart,
            String customerId,
            String postalCode,
            Restaurant restaurant,
            DeliveryZone deliveryZone,
            Stock stock) {
        Objects.requireNonNull(cart, "cart must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(postalCode, "postalCode must not be null");
        Objects.requireNonNull(restaurant, "restaurant must not be null");
        Objects.requireNonNull(deliveryZone, "deliveryZone must not be null");
        Objects.requireNonNull(stock, "stock must not be null");
        int subtotalCents = cart.subtotalCents();
        ValidationContext context = new ValidationContext(
                restaurant, deliveryZone, stock, subtotalCents, postalCode, cart);
        createValidationChain(deliveryZone).validate(context);

        int amountToPayCents = pricingStrategy.apply(subtotalCents);
        if (amountToPayCents < 0) {
            throw new IllegalArgumentException("payment amount must not be negative");
        }
        Order order = new Order(
                Objects.requireNonNull(orderIdSupplier.get(), "generated order id must not be null"),
                customerId,
                Instant.now(clock),
                cart.getChain(),
                cart.getLines(),
                OrderStatus.PENDING);
        if (!paymentGateway.pay(amountToPayCents)) {
            throw new IllegalStateException("Payment was declined");
        }

        order.updateStatus(OrderStatus.PAID);
        orderRepository.save(order);
        return order;
    }

    private ValidationHandler createValidationChain(DeliveryZone deliveryZone) {
        ValidationHandler nonEmptyCart = new NonEmptyCartValidator();
        ValidationHandler minimumAmount = new MinimumAmountValidator();
        ValidationHandler stockAvailability = new StockValidator();
        ValidationHandler restaurantOpen = new StoreOpenValidator();
        ValidationHandler deliveryAllowed = new DeliveryZoneValidator(deliveryZone);

        nonEmptyCart.setNext(minimumAmount);
        minimumAmount.setNext(stockAvailability);
        stockAvailability.setNext(restaurantOpen);
        restaurantOpen.setNext(deliveryAllowed);
        return nonEmptyCart;
    }
}
