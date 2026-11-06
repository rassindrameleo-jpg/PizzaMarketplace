package fr.pizzeriamarketplace.infrastructure.http;

import fr.pizzeriamarketplace.domain.catalog.ChainCatalog;
import fr.pizzeriamarketplace.domain.catalog.DaMichele;
import fr.pizzeriamarketplace.domain.catalog.Pizzium;
import fr.pizzeriamarketplace.domain.catalog.Rossopomodoro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderHistoryAndDiscountApiTest {
    private PizzaMarketplaceHttpServer server;
    private HttpClient client;

    @BeforeEach
    void setUp() {
        ChainCatalog catalog = new ChainCatalog(List.of(new DaMichele(), new Rossopomodoro(), new Pizzium()));
        server = PizzaMarketplaceHttpServer.create(0, catalog);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void returnsOnlyTheCustomerOrdersMostRecentFirst() throws Exception {
        String firstOrderId = placeOrder("alice");
        String secondOrderId = placeOrder("alice");
        placeOrder("bob");

        HttpResponse<String> history = send("GET", "/api/customers/alice/orders", "");

        assertEquals(200, history.statusCode());
        assertFalse(history.body().contains("\"customerId\":\"bob\""));
        // Most recent order comes first
        assertTrue(history.body().indexOf(secondOrderId) < history.body().indexOf(firstOrderId));
    }

    @Test
    void filtersHistoryByStatus() throws Exception {
        placeOrder("alice");

        HttpResponse<String> paid = send("GET", "/api/customers/alice/orders?status=PAID", "");
        HttpResponse<String> delivered = send("GET", "/api/customers/alice/orders?status=DELIVERED", "");

        assertTrue(paid.body().contains("\"status\":\"PAID\""));
        assertEquals("[]", delivered.body());
    }

    @Test
    void rejectsUnknownStatus() throws Exception {
        HttpResponse<String> response = send("GET", "/api/customers/alice/orders?status=UNKNOWN", "");

        assertEquals(400, response.statusCode());
    }

    @Test
    void appliesTheRequestedDiscount() throws Exception {
        String cartId = cartWithTwoMargheritas();

        HttpResponse<String> placed = send("POST", "/api/orders",
                "{\"cartId\":\"" + cartId + "\",\"customerId\":\"alice\",\"postalCode\":\"75001\","
                        + "\"discount\":\"HAPPY_HOUR\"}");

        // 2 x 900 = 1800 cents, minus 20% = 1440 cents
        assertEquals(201, placed.statusCode());
        assertTrue(placed.body().contains("\"subtotalCents\":1800"));
        assertTrue(placed.body().contains("\"totalCents\":1440"));
    }

    @Test
    void appliesNoDiscountByDefault() throws Exception {
        String cartId = cartWithTwoMargheritas();

        HttpResponse<String> placed = send("POST", "/api/orders",
                "{\"cartId\":\"" + cartId + "\",\"customerId\":\"alice\",\"postalCode\":\"75001\"}");

        assertTrue(placed.body().contains("\"totalCents\":1800"));
    }

    @Test
    void rejectsUnknownDiscount() throws Exception {
        String cartId = cartWithTwoMargheritas();

        HttpResponse<String> placed = send("POST", "/api/orders",
                "{\"cartId\":\"" + cartId + "\",\"customerId\":\"alice\",\"postalCode\":\"75001\","
                        + "\"discount\":\"BLACK_FRIDAY\"}");

        assertEquals(400, placed.statusCode());
        assertTrue(placed.body().contains("Unknown discount"));
    }

    private String placeOrder(String customerId) throws Exception {
        String cartId = cartWithTwoMargheritas();
        HttpResponse<String> placed = send("POST", "/api/orders",
                "{\"cartId\":\"" + cartId + "\",\"customerId\":\"" + customerId + "\","
                        + "\"postalCode\":\"75001\"}");
        // Make sure two orders never share the same creation instant
        Thread.sleep(5);
        return placed.body().split("\"")[3];
    }

    private String cartWithTwoMargheritas() throws Exception {
        HttpResponse<String> created = send("POST", "/api/carts", "{\"chain\":\"Da Michele\"}");
        String cartId = created.body().split("\"")[3];
        send("POST", "/api/carts/" + cartId + "/items",
                "{\"pizza\":\"Margherita\",\"size\":\"MEDIUM\",\"quantity\":2}");
        return cartId;
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + server.port() + path));
        if (method.equals("POST")) {
            request.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
        } else {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}