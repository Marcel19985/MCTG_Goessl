import models.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.UserService;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BattleTest {
    private Battle battle;
    private User player1;
    private User player2;
    private Deck deck1;
    private Deck deck2;

    @BeforeEach
    void setUp() {
        battle = new Battle();

        // Spieler erstellen:
        player1 = new User(UUID.randomUUID(), "Player1", "password", "token1");
        player2 = new User(UUID.randomUUID(), "Player2", "password", "token2");

        deck1 = new Deck();
        deck2 = new Deck();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testStartBattle_SpecialityDragonGoblin() throws SQLException { //Dragon gewinnt immer gegen Goblin

        deck1.addCard(new MonsterCard(UUID.randomUUID(), "Goblin", 10, Card.ElementType.NORMAL));
        deck2.addCard(new MonsterCard(UUID.randomUUID(), "Dragon", 25, Card.ElementType.FIRE));

        //Decks an Spieler zuweisen:
        player1.setDeck(deck1);
        player2.setDeck(deck2);

        //Spieler 2 gewinnt durch Spezialfall (Dragon gewinnt immer gegen Goblin):
        List<String> battleLog = battle.startBattle(player1, player2);

        assertEquals(0, player1.getWins());
        assertEquals(1, player1.getLosses());
        assertEquals(1, player2.getWins());
        assertEquals(0, player2.getLosses());
        assertEquals(0, player1.getDraws());
        assertEquals(0, player2.getDraws());
    }

    @Test
    void testStartBattle_SpecialityWizzardOrk() throws SQLException { //Wizzard gewinnt immer gegen Ork

        deck1.addCard(new MonsterCard(UUID.randomUUID(), "Org", 10, Card.ElementType.NORMAL));
        deck2.addCard(new MonsterCard(UUID.randomUUID(), "Wizard", 25, Card.ElementType.FIRE));

        player1.setDeck(deck1);
        player2.setDeck(deck2);

        List<String> battleLog = battle.startBattle(player1, player2);

        assertEquals(0, player1.getWins());
        assertEquals(1, player1.getLosses());
        assertEquals(1, player2.getWins());
        assertEquals(0, player2.getLosses());
        assertEquals(0, player1.getDraws());
        assertEquals(0, player2.getDraws());
    }

    @Test
    void testStartBattle_SpecialityKnightWaterspell() throws SQLException { //WaterSpell gewinnt immer gegen Knight

        deck1.addCard(new MonsterCard(UUID.randomUUID(), "Knight", 10, Card.ElementType.NORMAL));
        deck2.addCard(new MonsterCard(UUID.randomUUID(), "WaterSpell", 25, Card.ElementType.FIRE));

        // Decks an Spieler zuweisen
        player1.setDeck(deck1);
        player2.setDeck(deck2);

        List<String> battleLog = battle.startBattle(player1, player2);

        assertEquals(0, player1.getWins());
        assertEquals(1, player1.getLosses());
        assertEquals(1, player2.getWins());
        assertEquals(0, player2.getLosses());
        assertEquals(0, player1.getDraws());
        assertEquals(0, player2.getDraws());
    }

    @Test
    void testStartBattle_SpecialityKrakenSpell() throws SQLException { //Kraken gewinnt immer gegen Spell

        deck1.addCard(new MonsterCard(UUID.randomUUID(), "Spell", 10, Card.ElementType.NORMAL));
        deck2.addCard(new MonsterCard(UUID.randomUUID(), "Kraken", 25, Card.ElementType.FIRE));

        player1.setDeck(deck1);
        player2.setDeck(deck2);

        List<String> battleLog = battle.startBattle(player1, player2);

        assertEquals(0, player1.getWins());
        assertEquals(1, player1.getLosses());
        assertEquals(1, player2.getWins());
        assertEquals(0, player2.getLosses());
        assertEquals(0, player1.getDraws());
        assertEquals(0, player2.getDraws());
    }

    @Test
    void testStartBattle_SpecialityFireelveDragon() throws SQLException { //FireElce gewinnt immer gegen Dragon

        deck1.addCard(new MonsterCard(UUID.randomUUID(), "Dragon", 10, Card.ElementType.NORMAL));
        deck2.addCard(new MonsterCard(UUID.randomUUID(), "FireElve", 25, Card.ElementType.FIRE));

        player1.setDeck(deck1);
        player2.setDeck(deck2);

        List<String> battleLog = battle.startBattle(player1, player2);

        assertEquals(0, player1.getWins());
        assertEquals(1, player1.getLosses());
        assertEquals(1, player2.getWins());
        assertEquals(0, player2.getLosses());
        assertEquals(0, player1.getDraws());
        assertEquals(0, player2.getDraws());
    }

    @Test
    void testStartBattle_Draw() throws SQLException { //Zwei gleiche Karten -> Unentschieden

        deck1.addCard(new MonsterCard(UUID.randomUUID(), "FireElve", 25, Card.ElementType.FIRE));
        deck2.addCard(new MonsterCard(UUID.randomUUID(), "FireElve", 25, Card.ElementType.FIRE));

        player1.setDeck(deck1);
        player2.setDeck(deck2);

        List<String> battleLog = battle.startBattle(player1, player2);

        assertEquals(1, player1.getDraws());
        assertEquals(1, player2.getDraws());
        assertEquals(0, player1.getWins());
        assertEquals(0, player2.getWins());
        assertEquals(0, player1.getLosses());
        assertEquals(0, player2.getLosses());
    }

    @Test
    void testStartBattle_EffectivnessWaterFire() throws SQLException { //Water effektiv gegen Feuer

        deck1.addCard(new SpellCard(UUID.randomUUID(), "WaterGoblin", 1, Card.ElementType.WATER)); //Damage*2 = 2
        deck2.addCard(new SpellCard(UUID.randomUUID(), "FireElve", 3, Card.ElementType.FIRE)); //Damage/2 = 1.5

        player1.setDeck(deck1);
        player2.setDeck(deck2);

        List<String> battleLog = battle.startBattle(player1, player2);

        assertEquals(1, player1.getWins());
        assertEquals(1, player2.getLosses());
    }

    @Test
    void testStartBattle_EffectivnessFireNormal() throws SQLException { //Fire effektiv gegen normal

        deck1.addCard(new SpellCard(UUID.randomUUID(), "FireElve", 1, Card.ElementType.FIRE)); //Damage*2 = 2
        deck2.addCard(new SpellCard(UUID.randomUUID(), "Goblin", 3, Card.ElementType.NORMAL)); //Damage/2 = 1.5

        player1.setDeck(deck1);
        player2.setDeck(deck2);

        List<String> battleLog = battle.startBattle(player1, player2);

        assertEquals(1, player1.getWins());
        assertEquals(1, player2.getLosses());
    }

    @Test
    void testStartBattle_EffectivnessNormalWater() throws SQLException { //Normal effektiv gegen water

        deck1.addCard(new SpellCard(UUID.randomUUID(), "Elve", 1, Card.ElementType.NORMAL)); //Damage*2 = 2
        deck2.addCard(new SpellCard(UUID.randomUUID(), "Watergoblin", 3, Card.ElementType.WATER)); //Damage/2 = 1.5

        player1.setDeck(deck1);
        player2.setDeck(deck2);

        List<String> battleLog = battle.startBattle(player1, player2);

        assertEquals(1, player1.getWins());
        assertEquals(1, player2.getLosses());
    }

    @Test
    void testStartBattle_NormalDamageComparison() throws SQLException { //Damage 5 < 3

        deck1.addCard(new MonsterCard(UUID.randomUUID(), "Elve", 5, Card.ElementType.NORMAL));
        deck2.addCard(new MonsterCard(UUID.randomUUID(), "Watergoblin", 3, Card.ElementType.WATER));

        player1.setDeck(deck1);
        player2.setDeck(deck2);

        List<String> battleLog = battle.startBattle(player1, player2);

        assertEquals(1, player1.getWins());
        assertEquals(1, player2.getLosses());
    }

    @Test
    void testStartBattle_EloCalculation() throws SQLException { //Elo calculation nach 2 Spielen (Sieg und Unentschieden)

        deck1.addCard(new MonsterCard(UUID.randomUUID(), "Elve", 5, Card.ElementType.NORMAL));
        deck2.addCard(new MonsterCard(UUID.randomUUID(), "Watergoblin", 3, Card.ElementType.WATER));

        player1.setDeck(deck1);
        player2.setDeck(deck2);

        List<String> battleLog = battle.startBattle(player1, player2); //Spieler 1 gewinnt (+3 und -5 bei Gegner)

        //Beide spieler haben jetzt gleiches Deck -> Unentschieden:
        deck2.addCard(new MonsterCard(UUID.randomUUID(), "Elve", 5, Card.ElementType.NORMAL));
        player2.setDeck(deck2);

        battleLog = battle.startBattle(player1, player2); //unentschieden -> Elo soll sich nicht ändern

        assertEquals(103, player1.getElo());
        assertEquals(95, player2.getElo());
    }


    @Test
    void addToBattleQueue() {
    }

    @Test
    void removeFromBattleQueue() {
    }

    @Test
    void startBattle() {
    }
}