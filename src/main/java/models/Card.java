package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class Card {
    private final UUID id;
    private final String name;
    private final double damage;
    //In PDF-Angabe gibt es zusätzlich Element Type aber nicht im curl script!
    /*
    private final CardType type;
    public enum CardType {
        SPELL,
        MONSTER
    }*/

    //Konstruktor:
    @JsonCreator
    public Card(@JsonProperty("Id") UUID id, @JsonProperty("Name") String name, @JsonProperty("Damage") int damage) {
        this.id = id;
        this.name = name;
        this.damage = damage;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getDamage() {
        return damage;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", damage=" + damage +
                '}';
    }
}
