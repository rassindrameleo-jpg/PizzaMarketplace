import fr.pizzeriamarketplace.domain.catalog.ChainCatalog;
import fr.pizzeriamarketplace.domain.catalog.DaMichele;
import fr.pizzeriamarketplace.domain.catalog.Pizzium;
import fr.pizzeriamarketplace.domain.catalog.Rossopomodoro;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("ouverture de la pizeria");

        ChainCatalog catalog = new ChainCatalog(List.of(new DaMichele(), new Rossopomodoro(), new Pizzium()));
    }
}
