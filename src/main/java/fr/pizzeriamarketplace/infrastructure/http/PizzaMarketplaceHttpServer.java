package fr.pizzeriamarketplace.infrastructure.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import fr.pizzeriamarketplace.application.PlaceOrder;
import fr.pizzeriamarketplace.domain.catalog.ChainCatalog;
import fr.pizzeriamarketplace.domain.catalog.Ingredients;
import fr.pizzeriamarketplace.domain.catalog.PizzaChain;
import fr.pizzeriamarketplace.domain.model.Cart;
import fr.pizzeriamarketplace.domain.model.DeliveryZone;
import fr.pizzeriamarketplace.domain.model.Ingredient;
import fr.pizzeriamarketplace.domain.model.Order;
import fr.pizzeriamarketplace.domain.model.Pizza;
import fr.pizzeriamarketplace.domain.model.Restaurant;
import fr.pizzeriamarketplace.domain.model.Size;
import fr.pizzeriamarketplace.domain.model.Stock;
import fr.pizzeriamarketplace.domain.pricing.StandardPricing;
import fr.pizzeriamarketplace.infrastructure.persistence.InMemoryOrderRepository;
import fr.pizzeriamarketplace.application.usecase.GetOrderHistory;
import fr.pizzeriamarketplace.domain.model.OrderStatus;
import fr.pizzeriamarketplace.domain.payment.PaymentGateway;
import fr.pizzeriamarketplace.domain.pricing.FixedAmountDiscountPricing;
import fr.pizzeriamarketplace.domain.pricing.PercentageDiscountPricing;
import fr.pizzeriamarketplace.domain.pricing.PricingStrategy;
import fr.pizzeriamarketplace.infrastructure.config.PricingRules;
import java.util.Map;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PizzaMarketplaceHttpServer implements AutoCloseable {
    private final HttpServer server;

    private PizzaMarketplaceHttpServer(HttpServer server) {
        this.server = server;
    }

    public static PizzaMarketplaceHttpServer create(int port, ChainCatalog catalog) {
        Objects.requireNonNull(catalog, "catalog must not be null");
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            new ApiHandler(catalog).register(server);
            return new PizzaMarketplaceHttpServer(server);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create HTTP server", exception);
        }
    }

    public void start() {
        server.start();
    }

    public int port() {
        return server.getAddress().getPort();
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private static final class ApiHandler {
        private final ChainCatalog catalog;
        private final InMemoryCartStore cartStore;
        private final InMemoryOrderRepository orderRepository = new InMemoryOrderRepository();
        private final PaymentGateway paymentGateway = amountCents -> true;
        private final GetOrderHistory orderHistory = new GetOrderHistory(orderRepository);
        private final Map<String, PricingStrategy> discounts = Map.of(
                "HAPPY_HOUR", new PercentageDiscountPricing(PricingRules.HAPPY_HOUR_DISCOUNT_PERCENT),
                "LOYALTY", new PercentageDiscountPricing(PricingRules.LOYALTY_DISCOUNT_PERCENT),
                "PROMO_CODE", new FixedAmountDiscountPricing(PricingRules.PROMO_CODE_DISCOUNT_CENTS));

        private ApiHandler(ChainCatalog catalog) {
            this.catalog = catalog;
            this.cartStore = new InMemoryCartStore(catalog);
        }

        private void register(HttpServer server) {
            server.createContext("/api", this::handle);
        }

        private void handle(HttpExchange exchange) {
            try {
                dispatch(exchange);
            } catch (ApiException exception) {
                sendJson(exchange, exception.statusCode(), error(exception.getMessage()));
            } catch (IllegalArgumentException exception) {
                sendJson(exchange, 400, error(exception.getMessage()));
            } catch (IOException exception) {
                sendJson(exchange, 500, error("Could not read request"));
            } finally {
                exchange.close();
            }
        }

        private void dispatch(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String[] path = segments(exchange.getRequestURI());

            if (method.equals("GET") && path.length == 2 && path[1].equals("chains")) {
                sendJson(exchange, 200, chainsJson());
                return;
            }
            if (method.equals("GET") && path.length == 2 && path[1].equals("ingredients")) {
                sendJson(exchange, 200, ingredientsJson());
                return;
            }
            if (method.equals("GET") && path.length == 3 && path[1].equals("chains")
                    && path[2].equals("menu")) {
                throw new ApiException(400, "Chain name is required in the path");
            }
            if (method.equals("GET") && path.length == 4 && path[1].equals("chains")
                    && path[3].equals("menu")) {
                sendJson(exchange, 200, menuJson(chain(path[2])));
                return;
            }
            if (method.equals("POST") && path.length == 2 && path[1].equals("carts")) {
                String chainName = JsonObject.parse(readBody(exchange)).requiredString("chain");
                sendJson(exchange, 201, "{\"cartId\":\"" + cartStore.create(chainName) + "\"}");
                return;
            }
            if (method.equals("POST") && path.length == 2 && path[1].equals("orders")) {
                JsonObject request = JsonObject.parse(readBody(exchange));
                PricingStrategy pricing = pricing(request);
                Order order = placeOrder(request, pricing);
                sendJson(exchange, 201, placedOrderJson(order, pricing));
                return;
            }
            if (method.equals("GET") && path.length == 3 && path[1].equals("orders")) {
                Order order = orderRepository.findById(path[2])
                        .orElseThrow(() -> new ApiException(404, "Unknown order: " + path[2]));
                sendJson(exchange, 200, orderJson(order));
                return;
            }
            if (method.equals("GET") && path.length == 4 && path[1].equals("customers")
                    && path[3].equals("orders")) {
                sendJson(exchange, 200, ordersJson(orderHistory(path[2], exchange.getRequestURI())));
                return;
            }
            if (path.length >= 3 && path[1].equals("carts")) {
                handleCart(exchange, method, path);
                return;
            }
            sendJson(exchange, 404, error("Unknown endpoint"));
        }

        private Order placeOrder(JsonObject request, PricingStrategy pricing) {
            Cart cart = cartStore.find(request.requiredString("cartId"));
            String customerId = request.requiredString("customerId");
            String postalCode = request.requiredString("postalCode");
            PlaceOrder placeOrder = new PlaceOrder(orderRepository, paymentGateway, pricing);
            return placeOrder.place(
                    cart,
                    customerId,
                    postalCode,
                    new Restaurant(true),
                    new DeliveryZone(List.of("75001")),
                    availableStock());
        }

        private PricingStrategy pricing(JsonObject request) {
            return request.optionalString("discount")
                    .map(this::discount)
                    .orElseGet(StandardPricing::new);
        }

        private PricingStrategy discount(String name) {
            PricingStrategy pricing = discounts.get(name);
            if (pricing == null) {
                throw new ApiException(400, "Unknown discount: " + name);
            }
            return pricing;
        }

        private List<Order> orderHistory(String customerId, URI uri) {
            String status = queryParameter(uri, "status");
            if (status == null) {
                return orderHistory.forCustomer(customerId);
            }
            try {
                return orderHistory.forCustomer(customerId, OrderStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException exception) {
                throw new ApiException(400, "Unknown status: " + status);
            }
        }

        private static String queryParameter(URI uri, String name) {
            String query = uri.getRawQuery();
            if (query == null) {
                return null;
            }
            for (String pair : query.split("&")) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2 && keyValue[0].equals(name)) {
                    return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                }
            }
            return null;
        }

        private void handleCart(HttpExchange exchange, String method, String[] path) throws IOException {
            Cart cart = cartStore.find(path[2]);
            if (method.equals("GET") && path.length == 3) {
                sendJson(exchange, 200, cartJson(cart));
                return;
            }
            if (method.equals("POST") && path.length == 4 && path[3].equals("items")) {
                addItem(cart, readBody(exchange));
                sendJson(exchange, 201, cartJson(cart));
                return;
            }
            if (method.equals("DELETE") && path.length == 5 && path[3].equals("items")) {
                removeItem(cart, path[4]);
                sendJson(exchange, 200, cartJson(cart));
                return;
            }
            if (method.equals("POST") && path.length == 4 && path[3].equals("undo")) {
                if (!cart.undo()) {
                    throw new ApiException(409, "Cart has no change to undo");
                }
                sendJson(exchange, 200, cartJson(cart));
                return;
            }
            if (method.equals("POST") && path.length == 4 && path[3].equals("redo")) {
                if (!cart.redo()) {
                    throw new ApiException(409, "Cart has no change to redo");
                }
                sendJson(exchange, 200, cartJson(cart));
                return;
            }
            sendJson(exchange, 404, error("Unknown cart endpoint"));
        }

        private void addItem(Cart cart, String body) {
            JsonObject request = JsonObject.parse(body);
            String pizzaName = request.requiredString("pizza");
            Pizza pizza = cart.getChain().createMenu().stream()
                    .filter(item -> item.getName().equals(pizzaName))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(400, "Pizza is not on the selected menu"));
            Size size;
            try {
                size = Size.valueOf(request.requiredString("size").toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new ApiException(400, "Unknown size");
            }
            cart.addLine(pizza, size, request.requiredInt("quantity"));
        }

        private void removeItem(Cart cart, String rawIndex) {
            try {
                cart.removeLine(Integer.parseInt(rawIndex));
            } catch (NumberFormatException exception) {
                throw new ApiException(400, "Item index must be an integer");
            } catch (IndexOutOfBoundsException exception) {
                throw new ApiException(400, "Item index is out of range");
            }
        }

        private PizzaChain chain(String encodedName) {
            String name = URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
            return catalog.findByName(name)
                    .orElseThrow(() -> new ApiException(404, "Unknown chain: " + name));
        }

        private String chainsJson() {
            return catalog.getAll().stream()
                    .map(chain -> "{\"name\":\"" + escape(chain.getName()) + "\"}")
                    .collect(Collectors.joining(",", "[", "]"));
        }

        private String menuJson(PizzaChain chain) {
            return chain.createMenu().stream()
                    .map(pizza -> "{\"name\":\"" + escape(pizza.getName()) + "\","
                            + "\"base\":\"" + pizza.getBase() + "\","
                            + "\"mediumPriceCents\":" + pizza.getMediumPriceCents() + "}")
                    .collect(Collectors.joining(",", "[", "]"));
        }

        private String ingredientsJson() {
            return Ingredients.AVAILABLE.stream()
                    .map(ingredient -> ingredientJson(ingredient))
                    .collect(Collectors.joining(",", "[", "]"));
        }

        private String cartJson(Cart cart) {
            String lines = cart.getLines().stream()
                    .map(line -> "{\"pizza\":\"" + escape(line.getPizza().getName()) + "\","
                            + "\"size\":\"" + line.getSize() + "\","
                            + "\"quantity\":" + line.getQuantity() + "}")
                    .collect(Collectors.joining(",", "[", "]"));
            return "{\"chain\":\"" + escape(cart.getChain().getName()) + "\","
                    + "\"lines\":" + lines + ",\"subtotalCents\":" + cart.subtotalCents() + "}";
        }

        private static String placedOrderJson(Order order, PricingStrategy pricing) {
            String json = orderJson(order);
            return json.substring(0, json.length() - 1)
                    + ",\"totalCents\":" + pricing.apply(order.subtotalCents()) + "}";
        }

        private static String ordersJson(List<Order> orders) {
            return orders.stream()
                    .map(order -> orderJson(order))
                    .collect(Collectors.joining(",", "[", "]"));
        }

        private static String orderJson(Order order) {
            return "{\"orderId\":\"" + escape(order.getId()) + "\","
                    + "\"customerId\":\"" + escape(order.getCustomerId()) + "\","
                    + "\"createdAt\":\"" + order.getCreatedAt() + "\","
                    + "\"status\":\"" + order.getStatus() + "\","
                    + "\"chain\":\"" + escape(order.getChain().getName()) + "\","
                    + "\"subtotalCents\":" + order.subtotalCents() + "}";
        }

        private static Stock availableStock() {
            return new Stock(Ingredients.AVAILABLE.stream()
                    .collect(Collectors.toMap(Ingredient::getName, ingredient -> 1_000)));
        }

        private static String ingredientJson(Ingredient ingredient) {
            return "{\"name\":\"" + escape(ingredient.getName()) + "\","
                    + "\"priceCents\":" + ingredient.getPriceInCents() + "}";
        }

        private static String readBody(HttpExchange exchange) throws IOException {
            try (InputStream input = exchange.getRequestBody()) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        private static String[] segments(URI uri) {
            String path = uri.getPath();
            return path.substring(1).split("/");
        }

        private static void sendJson(HttpExchange exchange, int status, String body) {
            try {
                byte[] response = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(status, response.length);
                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(response);
                }
            } catch (IOException exception) {
                throw new ApiException(500, "Could not write response");
            }
        }

        private static String error(String message) {
            return "{\"error\":\"" + escape(message) + "\"}";
        }

        private static String escape(String value) {
            return value.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}
