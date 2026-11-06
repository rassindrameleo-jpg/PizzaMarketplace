package fr.pizzeriamarketplace.domain.catalog;

import fr.pizzeriamarketplace.domain.model.Base;
import fr.pizzeriamarketplace.domain.model.Cart;
import fr.pizzeriamarketplace.domain.model.Ingredient;
import fr.pizzeriamarketplace.domain.model.Pizza;
import fr.pizzeriamarketplace.domain.model.Size;
import org.junit.jupiter.api.Test;

import java.util.List;

import static fr.pizzeriamarketplace.domain.catalog.Ingredients.GORGONZOLA;
import static fr.pizzeriamarketplace.domain.catalog.Ingredients.MOZZARELLA;
import static fr.pizzeriamarketplace.domain.catalog.Ingredients.PEPPERONI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomPizzaBuilderTest {

    @Test
    void basePizzaCostsFiveEuros() {
        Pizza pizza = new CustomPizzaBuilder().base(Base.TOMATO).build();

        assertEquals(500, pizza.getMediumPriceCents());
        assertEquals(List.of(), pizza.getIngredients());
    }

    @Test
    void addsPriceOfEachIngredientToBasePrice() {
        Pizza pizza = new CustomPizzaBuilder()
                .base(Base.CREAM)
                .addIngredient(MOZZARELLA)
                .addIngredient(GORGONZOLA)
                .build();

        assertEquals(500 + 150 + 250, pizza.getMediumPriceCents());
        assertEquals(Base.CREAM, pizza.getBase());
        assertEquals(List.of(MOZZARELLA, GORGONZOLA), pizza.getIngredients());
    }

    @Test
    void countsDuplicatedIngredientEachTime() {
        Pizza pizza = new CustomPizzaBuilder()
                .base(Base.TOMATO)
                .addIngredient(MOZZARELLA)
                .addIngredient(MOZZARELLA)
                .build();

        assertEquals(500 + 150 + 150, pizza.getMediumPriceCents());
    }

    @Test
    void appliesSizeRuleOfEachChain() {
        Pizza pizza = new CustomPizzaBuilder()
                .base(Base.TOMATO)
                .addIngredient(MOZZARELLA)
                .addIngredient(PEPPERONI)
                .build(); // 500 + 150 + 200 = 850

        Cart daMicheleCart = new Cart(new DaMichele());
        daMicheleCart.addLine(pizza, Size.EXTRA_LARGE, 1);
        Cart rossopomodoroCart = new Cart(new Rossopomodoro());
        rossopomodoroCart.addLine(pizza, Size.LARGE, 1);

        assertEquals(850 + 500, daMicheleCart.subtotalCents());
        assertEquals(1105, rossopomodoroCart.subtotalCents()); // 850 × 1.3
    }

    @Test
    void pizziumOnlyAcceptsCustomPizzaInSizeMedium() {
        Pizza pizza = new CustomPizzaBuilder().base(Base.TOMATO).addIngredient(MOZZARELLA).build();
        Cart cart = new Cart(new Pizzium());

        assertThrows(IllegalArgumentException.class, () -> cart.addLine(pizza, Size.LARGE, 1));
        cart.addLine(pizza, Size.MEDIUM, 1);
        assertEquals(650, cart.subtotalCents());
    }

    @Test
    void refusesToBuildWithoutBase() {
        CustomPizzaBuilder builder = new CustomPizzaBuilder().addIngredient(MOZZARELLA);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void refusesIngredientNotOnTheMarketplace() {
        Ingredient truffle = new Ingredient("Truffe", 900);

        assertThrows(IllegalArgumentException.class, () -> new CustomPizzaBuilder().addIngredient(truffle));
    }
}
