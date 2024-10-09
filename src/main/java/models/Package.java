package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Package {
    private final UUID id;
    private final List<Card> cards; //Datenstruktur für 5 Cards (ein Package besteht aus 5 Karten)

    public Package(List<Card> cards) { //Konstruktor
        if (cards.size() != 5) { //falls Liste mit ungleich 5 Karten übergeben wird
            throw new IllegalArgumentException("A package must contain exactly 5 cards.");
        }
        this.id = UUID.randomUUID(); //Package UUID kommt nicht von Curl, nur bei Card
        this.cards = new ArrayList<>(cards);
    }

    // Getter for the package ID
    public UUID getId() {
        return id;
    }

    // Getter for the list of cards
    public List<Card> getCards() {
        return cards;
    }

    @Override
    public String toString() {
        return "Package{" +
                "id=" + id +
                ", cards=" + cards +
                '}';
    }
}