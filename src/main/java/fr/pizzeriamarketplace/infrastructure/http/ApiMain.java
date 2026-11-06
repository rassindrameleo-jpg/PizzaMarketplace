package fr.pizzeriamarketplace.infrastructure.http;

import fr.pizzeriamarketplace.domain.catalog.ChainCatalog;
import fr.pizzeriamarketplace.domain.catalog.DaMichele;
import fr.pizzeriamarketplace.domain.catalog.Pizzium;
import fr.pizzeriamarketplace.domain.catalog.Rossopomodoro;

import java.util.List;

public final class ApiMain {
    private ApiMain() {
    }

    public static void main(String[] args) {
        ChainCatalog catalog = new ChainCatalog(List.of(
                new DaMichele(),
                new Rossopomodoro(),
                new Pizzium()));
        PizzaMarketplaceHttpServer server = PizzaMarketplaceHttpServer.create(8080, catalog);
        server.start();
        System.out.println("Pizza Marketplace API started on http://localhost:8080");
    }
}
