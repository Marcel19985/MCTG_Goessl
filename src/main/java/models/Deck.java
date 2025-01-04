package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Deck {
    private final List<Card> cards; //durch final kann cards Zeiger nicht auf eine andere Liste zeigen -> diese Liste kann aber noch verändert werden

    public Deck() {
        this.cards = new ArrayList<>();
    }

    public void setCards(List<Card> cards) {
        if (cards.size() != 4) {
            throw new IllegalArgumentException("A deck must consist of exactly1 4 cards.");
        }
        this.cards.clear();
        this.cards.addAll(cards);
    }

    public List<Card> getCards() {
        return cards;
    }

    public void clear() {
        this.cards.clear();
    }

    public void addCard(Card card) {
        this.cards.add(card);
    }

    public void deleteCard(Card card) {
        this.cards.remove(card);
    }

    public boolean containsCard(UUID cardId) {
        return cards.stream().anyMatch(card -> card.getId().equals(cardId));
    }

    public Card getRandomCard() {
        Random random = new Random();
        return cards.get(random.nextInt(cards.size()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Deck:\n");
        for (Card card : cards) {
            builder.append(card.getName())
                    .append(" (")
                    .append(card.getElementType())
                    .append(", ")
                    .append(card.getDamage())
                    .append(")\n");
        }
        return builder.toString();
    }
}
