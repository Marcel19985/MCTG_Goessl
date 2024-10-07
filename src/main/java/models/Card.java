package models;

public class Card {
    private final String name;
    private final int damage;
    private final String elementType;
    private final CardType cardType;

    public enum CardType {
        SPELL,
        MONSTER
    }

    public Card(String name, int damage, String elementType, CardType cardType) {
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.cardType = cardType;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public String getElementType() {
        return elementType;
    }

    public CardType getCardType() {
        return cardType;
    }

    @Override
    public String toString() {
        return "Card{" +
                "name='" + name + '\'' +
                ", damage=" + damage +
                ", elementType='" + elementType + '\'' +
                ", cardType=" + cardType +
                '}';
    }
}
