package models;

import java.util.UUID; //für UUID (keine fortlaufenden Nummern als Primärschlüssel)

public abstract class Card {
    protected final UUID id;
    protected final String name;
    protected final double damage;
    protected final ElementType elementType;

    public enum ElementType {
        FIRE, WATER, NORMAL
    }

    //Konstruktor:
    public Card(UUID id, String name, double damage, ElementType elementType) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        //! Wahrscheinlich wird if und else nicht gebraucht werden:
        if (elementType != null) {
            this.elementType = elementType;
        }
        else {
            System.out.println("TEST");
            this.elementType = ElementType.NORMAL;
        }
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

    public abstract String getCardType();

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", damage=" + damage +
                ", elementType=" + elementType +
                '}';
    }
}