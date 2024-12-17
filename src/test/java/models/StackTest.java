package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StackTest {

    private Stack stack;

    @BeforeEach
    void setUp() {
        stack = new Stack(); // Neues Stack-Objekt initialisieren
    }

    @Test
    void testAddCard() {
        //Karte hinzufügen:
        Card card = CardFactory.createCard(UUID.randomUUID(), "FireGoblin", 10.0);
        stack.addCard(card);

        //Überprüfen, ob die Karte im Stack ist:
        assertTrue(stack.getCards().contains(card));
        assertEquals(1, stack.getCards().size());
    }

    @Test
    void testRemoveCard() {
        //Karte hinzufügen und wieder entfernen:
        Card card = CardFactory.createCard(UUID.randomUUID(), "FireGoblin", 10.0);
        stack.addCard(card);
        stack.removeCard(card);

        //Überprüfen, ob die Karte entfernt wurde:
        assertFalse(stack.getCards().contains(card));
        assertTrue(stack.getCards().isEmpty());
    }

    @Test
    void testRemoveCardThatDoesNotExist() {
        //Eine Karte erstellen, die nicht im Stack ist:
        Card card = CardFactory.createCard(UUID.randomUUID(), "FireGoblin", 10.0);

        //Versuch, eine nicht existierende Karte zu entfernen:
        stack.removeCard(card);

        //Stack sollte leer bleiben:
        assertTrue(stack.getCards().isEmpty());
    }

    @Test
    void testGetCards() {
        //Karten vorbereiten:
        List<Card> cards = List.of(
                CardFactory.createCard(UUID.randomUUID(), "FireGoblin", 10.0),
                CardFactory.createCard(UUID.randomUUID(), "WaterSpell", 15.0)
        );

        for (Card card : cards) {
            stack.addCard(card);
        }

        //Überprüfen, ob getCards die korrekten Karten zurückgibt:
        assertEquals(2, stack.getCards().size());
        assertIterableEquals(cards, stack.getCards());
    }
}
