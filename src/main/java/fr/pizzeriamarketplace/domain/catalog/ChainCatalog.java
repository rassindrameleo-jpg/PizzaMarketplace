package fr.pizzeriamarketplace.domain.catalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The chains available on the marketplace.
 * Chains are given from outside (in Main), so this class never changes when a chain is added.
 */
public final class ChainCatalog {
    private final Map<String, PizzaChain> chainsByName = new LinkedHashMap<>();

    /**
     * @param chains the chains offered on the marketplace, each with a unique name
     * @throws IllegalArgumentException if two chains have the same name
     */
    public ChainCatalog(List<PizzaChain> chains) {
        Objects.requireNonNull(chains, "chains must not be null");
        for (PizzaChain chain : chains) {
            Objects.requireNonNull(chain, "chain must not be null");
            if (chainsByName.putIfAbsent(chain.getName(), chain) != null) {
                throw new IllegalArgumentException("duplicate chain name: " + chain.getName());
            }
        }
    }

    public Optional<PizzaChain> findByName(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return Optional.ofNullable(chainsByName.get(name));
    }

    /**
     * @return all chains, in the order they were given
     */
    public List<PizzaChain> getAll() {
        return List.copyOf(chainsByName.values());
    }
}
