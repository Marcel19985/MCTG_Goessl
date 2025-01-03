package models;

import java.util.UUID; //für UUID (keine fortlaufenden Nummern als Primärschlüssel)

public abstract class Card {
    protected final UUID id;
    protected final String name;
    protected double damage;
    protected final ElementType elementType;

    public enum ElementType {
        FIRE, WATER, NORMAL
    }

    //Konstruktor:
    public Card(UUID id, String name, double damage, ElementType elementType) {
        this.id = id; //ID kommt aus CURL, daher nicht erst bei Datenbankoperation erstellt
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
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

    public ElementType getElementType() {
        return elementType;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public abstract String getCardType();

    @Override
    public String toString() { //gibt Objekt als String zurück
        return "Card{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", damage=" + damage +
                ", elementType=" + elementType +
                '}';
    }
}