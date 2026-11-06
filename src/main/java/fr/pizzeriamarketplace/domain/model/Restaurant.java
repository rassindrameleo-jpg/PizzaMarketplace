package fr.pizzeriamarketplace.domain.model;

public class Restaurant {


    private boolean open;

    public Restaurant(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }
}
