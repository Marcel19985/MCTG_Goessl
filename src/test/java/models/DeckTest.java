package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = new Deck(); // Ein leeres Deck initialisieren
    }

    @Test
    void testSetCardsWithValidSize() {
        //Liste mit 4 gültigen Karten erstellen:
        List<Card> cards = List.of(
                CardFactory.createCard(UUID.randomUUID(), "FireDragon", 20.0),
                CardFactory.createCard(UUID.randomUUID(), "WaterSpell", 15.0),
                CardFactory.createCard(UUID.randomUUID(), "NormalKnight", 25.0),
                CardFactory.createCard(UUID.randomUUID(), "FireGoblin", 10.0)
        );

        deck.setCards(cards); //setter

        //Überprüfen, ob Karten korrekt gesetzt wurden:
        assertEquals(4, deck.getCards().size());
        assertEquals("FireDragon", deck.getCards().get(0).getName());
    }

    @Test
    void testSetCardsWithInvalidSize() {
        //Liste mit 2 Karten erstellen:
        List<Card> invalidCards = List.of(
                CardFactory.createCard(UUID.randomUUID(), "FireDragon", 20.0),
                CardFactory.createCard(UUID.randomUUID(), "WaterSpell", 15.0)
        );

        //Überprüfen, ob eine Exception geworfen wird:
        assertThrows(IllegalArgumentException.class, () -> deck.setCards(invalidCards));
    }

    @Test
    void testClearDeck() {
        // Vorbereiten und Hinzufügen von 4 Karten
        List<Card> cards = List.of(
                CardFactory.createCard(UUID.randomUUID(), "FireDragon", 20.0),
                CardFactory.createCard(UUID.randomUUID(), "WaterSpell", 15.0),
                CardFactory.createCard(UUID.randomUUID(), "NormalKnight", 25.0),
                CardFactory.createCard(UUID.randomUUID(), "FireGoblin", 10.0)
        );
        deck.setCards(cards);

        deck.clear(); //Deck löschen

        //Überprüfen, ob das Deck leer ist:
        assertTrue(deck.getCards().isEmpty());
    }

    @Test
    void testToString() {
        //Liste mit 4 Karten:
        List<Card> cards = List.of(
                CardFactory.createCard(UUID.randomUUID(), "FireDragon", 20.0),
                CardFactory.createCard(UUID.randomUUID(), "WaterSpell", 15.0),
                CardFactory.createCard(UUID.randomUUID(), "NormalKnight", 25.0),
                CardFactory.createCard(UUID.randomUUID(), "FireGoblin", 10.0)
        );
        deck.setCards(cards);

        //Überprüfen, ob die toString-Methode korrekt arbeitet:
        String deckString = deck.toString();
        assertTrue(deckString.contains("FireDragon"));
        assertTrue(deckString.contains("WaterSpell"));
        assertTrue(deckString.contains("NormalKnight"));
        assertTrue(deckString.contains("FireGoblin"));
    }
}
