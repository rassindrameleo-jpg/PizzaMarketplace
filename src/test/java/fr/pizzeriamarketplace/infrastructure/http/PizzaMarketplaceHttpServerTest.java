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
import static org.junit.jupiter.api.Assertions.assertTrue;

class PizzaMarketplaceHttpServerTest {
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
    void exposesChainsAndMenus() throws Exception {
        HttpResponse<String> chains = send("GET", "/api/chains", "");
        HttpResponse<String> menu = send("GET", "/api/chains/Da%20Michele/menu", "");

        assertEquals(200, chains.statusCode());
        assertTrue(chains.body().contains("Da Michele"));
        assertEquals(200, menu.statusCode());
        assertTrue(menu.body().contains("Margherita"));
    }

    @Test
    void createsAndUpdatesCartWithUndo() throws Exception {
        HttpResponse<String> created = send("POST", "/api/carts", "{\"chain\":\"Da Michele\"}");
        String cartId = created.body().split("\"")[3];

        HttpResponse<String> added = send("POST", "/api/carts/" + cartId + "/items",
                "{\"pizza\":\"Margherita\",\"size\":\"MEDIUM\",\"quantity\":2}");
        HttpResponse<String> undone = send("POST", "/api/carts/" + cartId + "/undo", "");

        assertEquals(201, created.statusCode());
        assertEquals(201, added.statusCode());
        assertTrue(added.body().contains("\"quantity\":2"));
        assertEquals(200, undone.statusCode());
        assertTrue(undone.body().contains("\"lines\":[]"));
    }

    @Test
    void rejectsUnknownChain() throws Exception {
        HttpResponse<String> response = send("POST", "/api/carts", "{\"chain\":\"Unknown\"}");

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Unknown chain"));
    }

    @Test
    void placesAndRetrievesAnOrderFromACart() throws Exception {
        HttpResponse<String> created = send("POST", "/api/carts", "{\"chain\":\"Da Michele\"}");
        String cartId = created.body().split("\"")[3];
        send("POST", "/api/carts/" + cartId + "/items",
                "{\"pizza\":\"Margherita\",\"size\":\"MEDIUM\",\"quantity\":2}");

        HttpResponse<String> placed = send("POST", "/api/orders",
                "{\"cartId\":\"" + cartId + "\",\"customerId\":\"customer-1\",\"postalCode\":\"75001\"}");
        String orderId = placed.body().split("\"")[3];
        HttpResponse<String> retrieved = send("GET", "/api/orders/" + orderId, "");

        assertEquals(201, placed.statusCode());
        assertTrue(placed.body().contains("\"status\":\"PAID\""));
        assertEquals(200, retrieved.statusCode());
        assertTrue(retrieved.body().contains("\"customerId\":\"customer-1\""));
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
