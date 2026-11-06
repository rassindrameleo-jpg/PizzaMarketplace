package fr.pizzeriamarketplace.infrastructure.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import fr.pizzeriamarketplace.domain.catalog.ChainCatalog;
import fr.pizzeriamarketplace.domain.catalog.Ingredients;
import fr.pizzeriamarketplace.domain.catalog.PizzaChain;
import fr.pizzeriamarketplace.domain.model.Cart;
import fr.pizzeriamarketplace.domain.model.Ingredient;
import fr.pizzeriamarketplace.domain.model.Pizza;
import fr.pizzeriamarketplace.domain.model.Size;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
            if (path.length >= 3 && path[1].equals("carts")) {
                handleCart(exchange, method, path);
                return;
            }
            sendJson(exchange, 404, error("Unknown endpoint"));
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
