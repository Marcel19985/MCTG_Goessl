package models;

import java.util.ArrayList;
import java.util.List;

public class Package {
    private final List<Card> cards;

    public Package(List<Card> cards) {
        if (cards.size() != 5) {
            throw new IllegalArgumentException("A package must contain exactly 5 cards.");
        }
        this.cards = new ArrayList<>(cards); //übergebene Liste wird nun in Liste des Objekts gespeichert
    }

    public List<Card> getCards() {
        return cards;
    }

    @Override
    public String toString() {
        return "Package{" +
                "cards=" + cards +
                '}';
    }
}