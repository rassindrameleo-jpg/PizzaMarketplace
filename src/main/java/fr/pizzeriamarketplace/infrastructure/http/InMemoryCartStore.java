package fr.pizzeriamarketplace.infrastructure.http;

import fr.pizzeriamarketplace.domain.catalog.ChainCatalog;
import fr.pizzeriamarketplace.domain.model.Cart;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class InMemoryCartStore {
    private final Map<String, Cart> carts = new ConcurrentHashMap<>();
    private final ChainCatalog catalog;

    InMemoryCartStore(ChainCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog must not be null");
    }

    String create(String chainName) {
        Cart cart = catalog.findByName(chainName)
                .map(Cart::new)
                .orElseThrow(() -> new ApiException(404, "Unknown chain: " + chainName));
        String cartId = UUID.randomUUID().toString();
        carts.put(cartId, cart);
        return cartId;
    }

    Cart find(String cartId) {
        return Optional.ofNullable(carts.get(cartId))
                .orElseThrow(() -> new ApiException(404, "Unknown cart: " + cartId));
    }
}
