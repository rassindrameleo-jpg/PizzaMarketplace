package fr.pizzeriamarketplace.infrastructure.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import fr.pizzeriamarketplace.application.PlaceOrder;
import fr.pizzeriamarketplace.domain.catalog.ChainCatalog;
import fr.pizzeriamarketplace.domain.catalog.CustomPizzaBuilder;
import fr.pizzeriamarketplace.domain.catalog.Ingredients;
import fr.pizzeriamarketplace.domain.catalog.PizzaChain;
import fr.pizzeriamarketplace.domain.model.Base;
import fr.pizzeriamarketplace.domain.model.Cart;
import fr.pizzeriamarketplace.domain.model.DeliveryZone;
import fr.pizzeriamarketplace.domain.model.Ingredient;
import fr.pizzeriamarketplace.domain.model.Order;
import fr.pizzeriamarketplace.domain.model.Pizza;
import fr.pizzeriamarketplace.domain.model.Restaurant;
import fr.pizzeriamarketplace.domain.model.Size;
import fr.pizzeriamarketplace.domain.model.Stock;
import fr.pizzeriamarketplace.domain.payment.*;
import fr.pizzeriamarketplace.domain.pricing.StandardPricing;
import fr.pizzeriamarketplace.domain.validation.*;
import fr.pizzeriamarketplace.infrastructure.persistence.InMemoryOrderRepository;

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
            } catch (IllegalArgumentException | IllegalStateException exception) {
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
                Order order = placeOrder(JsonObject.parse(readBody(exchange)));
                sendJson(exchange, 201, orderJson(order));
                return;
            }
            if (method.equals("GET") && path.length == 3 && path[1].equals("orders")) {
                Order order = orderRepository.findById(path[2])
                        .orElseThrow(() -> new ApiException(404, "Unknown order: " + path[2]));
                sendJson(exchange, 200, orderJson(order));
                return;
            }
            if (path.length >= 3 && path[1].equals("carts")) {
                handleCart(exchange, method, path);
                return;
            }

            if (method.equals("POST")
                    && path.length == 2
                    && path[1].equals("payments")) {
                handlePayment(exchange);
                return;
            }
            if (method.equals("POST")
                    && path.length == 3
                    && path[1].equals("orders")
                    && path[2].equals("validate")) {
                handleValidation(exchange);
                return;
            }
            sendJson(exchange, 404, error("Unknown endpoint"));
        }

        private void handleValidation(HttpExchange exchange) throws IOException {
            JsonObject request = JsonObject.parse(readBody(exchange));

            Cart cart = cartStore.find(request.requiredString("cartId"));
            String postalCode = request.requiredString("postalCode");

            ValidationContext context = new ValidationContext(
                    new Restaurant(true),
                    new DeliveryZone(List.of("75001")),
                    availableStock(),
                    cart.subtotalCents(),
                    postalCode,
                    cart
            );

            NonEmptyCartValidator first = new NonEmptyCartValidator();
            MinimumAmountValidator minimum = new MinimumAmountValidator();
            DeliveryZoneValidator delivery = new DeliveryZoneValidator(
                    new DeliveryZone(List.of("75001"))
            );
            StoreOpenValidator store = new StoreOpenValidator();
            StockValidator stock = new StockValidator();

            first.setNext(minimum);
            minimum.setNext(delivery);
            delivery.setNext(store);
            store.setNext(stock);



            first.validate(context);

            sendJson(exchange, 200, "{\"valid\":true}");
        }
        private PaymentGateway createPaymentGateway(String method) {
            return switch (method.toLowerCase()) {
                case "stripe" -> new StripeAdapter(new StripePayment());
                case "paypal" -> new PaypalAdapter(new PaypalPayment());
                case "applepay" -> new ApplePayAdapter(new ApplePayPayment());
                default -> throw new ApiException(400, "Unknown payment method");
            };
        }
        private void handlePayment(HttpExchange exchange) throws IOException {
            JsonObject request = JsonObject.parse(readBody(exchange));

            int amountCents = request.requiredInt("amountCents");
            String method = request.requiredString("method");

            if (amountCents <= 0) {
                throw new ApiException(400, "Amount must be positive");
            }

            PaymentGateway gateway = switch (method.toLowerCase()) {
                case "stripe" -> new StripeAdapter(new StripePayment());
                case "paypal" -> new PaypalAdapter(new PaypalPayment());
                case "applepay" -> new ApplePayAdapter(new ApplePayPayment());
                default -> throw new ApiException(400, "Unknown payment method");
            };
            //if (gateway instanceof StripeAdapter stripe) {
               // stripe.setAvailable(false);
          //  }
            PaymentGateway fallback = method.equalsIgnoreCase("paypal")
                    ? new StripeAdapter(new StripePayment())
                    : new PaypalAdapter(new PaypalPayment());

            PaymentService service = new PaymentService(gateway, fallback);
            boolean success = service.pay(amountCents);

            sendJson(exchange, success ? 200 : 400,
                    "{\"success\":" + success + "}");
        }
        private Order placeOrder(JsonObject request) {
            Cart cart = cartStore.find(request.requiredString("cartId"));
            String customerId = request.requiredString("customerId");
            String postalCode = request.requiredString("postalCode");
            String paymentMethod = "stripe";

            try {
                paymentMethod = request.requiredString("paymentMethod");
            } catch (ApiException e) {
                if (!e.getMessage().equals("Missing field: paymentMethod")) {
                    throw e;
                }
            }
            PaymentGateway gateway = createPaymentGateway(paymentMethod);

            PaymentGateway fallback = paymentMethod.equalsIgnoreCase("paypal")
                    ? createPaymentGateway("stripe")
                    : createPaymentGateway("paypal");

            PaymentService paymentService = new PaymentService(gateway, fallback);

            PlaceOrder placeOrder = new PlaceOrder(
                    orderRepository,
                    paymentService::pay,
                    new StandardPricing()
            );

            return placeOrder.place(
                    cart,
                    customerId,
                    postalCode,
                    new Restaurant(true),
                    new DeliveryZone(List.of("75001")),
                    availableStock()
            );
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
            if (method.equals("POST") && path.length == 4 && path[3].equals("custom-items")) {
                addCustomItem(cart, readBody(exchange));
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
            cart.addLine(pizza, size(request), request.requiredInt("quantity"));
        }

        private void addCustomItem(Cart cart, String body) {
            JsonObject request = JsonObject.parse(body);
            CustomPizzaBuilder builder = new CustomPizzaBuilder().base(base(request));
            request.requiredStringList("ingredients").stream()
                    .map(this::ingredient)
                    .forEach(builder::addIngredient);
            cart.addLine(builder.build(), size(request), request.requiredInt("quantity"));
        }

        private static Size size(JsonObject request) {
            try {
                return Size.valueOf(request.requiredString("size").toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new ApiException(400, "Unknown size");
            }
        }

        private static Base base(JsonObject request) {
            try {
                return Base.valueOf(request.requiredString("base").toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new ApiException(400, "Unknown base");
            }
        }

        private Ingredient ingredient(String name) {
            return Ingredients.AVAILABLE.stream()
                    .filter(ingredient -> ingredient.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(400, "Unknown ingredient: " + name));
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
                            + "\"base\":\"" + line.getPizza().getBase() + "\","
                            + "\"ingredients\":" + ingredientNamesJson(line.getPizza()) + ","
                            + "\"size\":\"" + line.getSize() + "\","
                            + "\"quantity\":" + line.getQuantity() + "}")
                    .collect(Collectors.joining(",", "[", "]"));
            return "{\"chain\":\"" + escape(cart.getChain().getName()) + "\","
                    + "\"lines\":" + lines + ",\"subtotalCents\":" + cart.subtotalCents() + "}";
        }

        private static String ingredientNamesJson(Pizza pizza) {
            return pizza.getIngredients().stream()
                    .map(ingredient -> "\"" + escape(ingredient.getName()) + "\"")
                    .collect(Collectors.joining(",", "[", "]"));
        }

        private static String orderJson(Order order) {
            return "{\"orderId\":\"" + escape(order.getId()) + "\","
                    + "\"customerId\":\"" + escape(order.getCustomerId()) + "\","
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
