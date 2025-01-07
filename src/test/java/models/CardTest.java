package models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void testMonsterCardCreation() {
        UUID id = UUID.randomUUID();
        String name = "FireDragon";
        double damage = 30.5;
        Card.ElementType elementType = Card.ElementType.FIRE;

        //Konstruktor:
        MonsterCard monsterCard = new MonsterCard(id, name, damage, elementType);
        //getter überprüfen:
        assertEquals(id, monsterCard.getId());
        assertEquals(name, monsterCard.getName());
        assertEquals(damage, monsterCard.getDamage());
        assertEquals(elementType, monsterCard.getElementType());
        assertEquals("Monster", monsterCard.getCardType());
    }

    @Test
    void testSpellCardCreation() {
        UUID id = UUID.randomUUID();
        String name = "WaterSpell";
        double damage = 20.0;
        Card.ElementType elementType = Card.ElementType.WATER;

        //Konstruktor:
        SpellCard spellCard = new SpellCard(id, name, damage, elementType);
        //getter überprüfen:
        assertEquals(id, spellCard.getId());
        assertEquals(name, spellCard.getName());
        assertEquals(damage, spellCard.getDamage());
        assertEquals(elementType, spellCard.getElementType());
        assertEquals("Spell", spellCard.getCardType());
    }

    @Test
    void testElementTypeNormalHandling() { //Kein Type im name angegeben
        UUID id = UUID.randomUUID();
        String name = "UnknownCard";
        double damage = 10.0;

        //ElementType nicht aus Name erkennbar, sollte automatisch auf NORMAL sein
        Card card = CardFactory.createCard(id, name, damage);
        assertEquals(Card.ElementType.NORMAL, card.getElementType());
    }

    @Test
    void testFactoryCreatesMonsterCard() { //Monster im Namen angegeben
        UUID id = UUID.randomUUID();
        String name = "FireMonster";
        double damage = 25.0;

        Card card = CardFactory.createCard(id, name, damage);

        assertTrue(card instanceof MonsterCard);
        assertEquals("Monster", card.getCardType());
    }

    @Test
    void testFactoryCreatesSpellCard() { //Spell im Namen angegeben
        UUID id = UUID.randomUUID();
        String name = "WaterSpell";
        double damage = 15.0;

        Card card = CardFactory.createCard(id, name, damage);

        assertTrue(card instanceof SpellCard);
        assertEquals("Spell", card.getCardType());
    }

    @AfterEach
    void tearDown() {
    }
}