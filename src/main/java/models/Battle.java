package models;

import services.UserService;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Random;

public class Battle {
    private final List<String> battleLog = new ArrayList<>(); //BattleLog wird in Liste gespeichert, bevor es ausgegeben wird

    private static final Queue<User> battleQueue = new ConcurrentLinkedQueue<>(); //Warteschlange für Spieler, der auf einen Gegner wartet

    public static Queue<User> getBattleQueue() { //get Warteschlange
        return battleQueue;
    }

    public static void addToBattleQueue(User user) { //Spieler in Queue einfügen
        battleQueue.add(user);
    }

    public static User removeFromBattleQueue() { //entfernt ersten Spieler aus Queue
        User user = battleQueue.poll();
        return user;
    }

    //Battle Logik:
    public List<String> startBattle(User player1, User player2) throws SQLException {

        Deck deck1 = player1.getDeck();
        Deck deck2 = player2.getDeck();

        int MAX_ROUNDS = 100;
        int rounds = 0;
        int winStreak1 = 0; //Special feature
        int winStreak2 = 0;

        while (rounds < MAX_ROUNDS && !deck1.getCards().isEmpty() && !deck2.getCards().isEmpty()) {
            rounds++;
            battleLog.add("Round " + rounds + ":");

            //Zufällige Karten aus Deck wählen:
            Card card1 = deck1.getRandomCard();
            Card card2 = deck2.getRandomCard();

            battleLog.add(player1.getUsername() + " plays " + card1.getName());
            battleLog.add(player2.getUsername() + " plays " + card2.getName());

            if (winStreak1 > 2 || winStreak2 > 2) {//Siegesserie
                Random random = new Random();
                int randomNumber = random.nextInt(3) + 1; //Zufallszahl 1, 2 oder 3
                if (winStreak1 > winStreak2) {
                    battleLog.add(player1.getUsername() + " has a win streak of " + winStreak1 + ". Damage of current card is enhanced by factor " + randomNumber);
                    card1.setDamage(card1.getDamage()*randomNumber);
                }
                else {
                    battleLog.add(player2.getUsername() + " has a win streak of " + winStreak2 + ". Damage of current card is enhanced by factor " + randomNumber);
                    card2.setDamage(card2.getDamage()*randomNumber);
                }
            }

            //Gewinner ermitteln:
            Card winner = determineWinner(card1, card2);
            if (winner == null) {
                battleLog.add("It's a tie!");
                deck2.deleteCard(card2); //Additional feature: bei Unentschieden verlieren beide Spieler die Karten
                deck1.deleteCard(card1);
            } else if (winner == card1) { //Gewinner der Runde bekommt Karte aus Gegner-Deck:
                battleLog.add(player1.getUsername() + " wins the round!");
                deck2.deleteCard(card2);
                deck1.deleteCard(card2);
                winStreak1++;
                winStreak2 = 0;
            } else {
                battleLog.add(player2.getUsername() + " wins the round!");
                deck1.deleteCard(card1);
                deck2.addCard(card1);
                winStreak2++;
                winStreak1 = 0;
            }
        }

        //Spielergebnisse:
        if (deck1.getCards().isEmpty() && deck2.getCards().isEmpty()) {
            battleLog.add("Battle ended in a draw after " + rounds + " rounds.");
            player1.increaseDraws();
            player2.increaseDraws();
        }
        else if (deck1.getCards().isEmpty()) {
            battleLog.add(player2.getUsername() + " wins the battle atfer " + rounds + " rounds.");
            player2.increaseWins();
            player1.increaseLosses();
        } else if (deck2.getCards().isEmpty()) {
            battleLog.add(player1.getUsername() + " wins the battle atfer " + rounds + " rounds.");
            player1.increaseWins();
            player2.increaseLosses();
        } else {
            battleLog.add("Battle ended in a draw after " + rounds + " rounds.");
            player1.increaseDraws();
            player2.increaseDraws();
        }
        UserService userService = new UserService();
        userService.updateUserStats(player1); //Datenbank aktualisieren
        userService.updateUserStats(player2); //Datenbank aktualisieren

        return battleLog;
    }

    private Card determineWinner(Card card1, Card card2) {

        //Goblin vs. Dragon: Dragon gewinnt immer
        if (card1.getName().contains("Goblin") && card2.getName().contains("Dragon")) {
            logSpecialities(card2, card1);
            return card2;
        }
        if (card2.getName().contains("Goblin") && card1.getName().contains("Dragon")) {
            logSpecialities(card1, card2);
            return card1;
        }

        //Ork vs. Wizard: Wizard gewinnt immer
        if (card1.getName().contains("Ork") && card2.getName().contains("Wizard")) {
            logSpecialities(card2, card1);
            return card2;
        }
        if (card2.getName().contains("Ork") && card1.getName().contains("Wizard")) {
            logSpecialities(card1, card2);
            return card1;
        }

        //Knight vs WaterSpell: WaterSpell gewinnt immer
        if (card1.getName().contains("Knight") && card2.getName().contains("WaterSpell")) {
            logSpecialities(card2, card1);
            return card2;
        }
        if (card2.getName().contains("Knight") && card1.getName().contains("WaterSpell")) {
            logSpecialities(card1, card2);
            return card1;
        }

        //Kraken vs Spell: Kraken gewinnt immer
        if (card1.getName().contains("Kraken") && card2 instanceof models.SpellCard) {
            logSpecialities(card1, card2);
            return card1;
        }
        if (card2.getName().contains("Kraken") && card1 instanceof models.SpellCard) {
            logSpecialities(card2, card1);
            return card2;
        }

        //FireElf vs Dragon: FireElf gewinnt immer
        if (card1.getName().contains("FireElf") && card2.getName().contains("Dragon")) {
            logSpecialities(card1, card2);
            return card1;
        }
        if (card2.getName().contains("FireElf") && card1.getName().contains("Dragon")) {
            logSpecialities(card2, card1);
            return card2;
        }

        //Damage:
        double damage1 = card1.getDamage();
        double damage2 = card2.getDamage();

        //Bei Spell-Cards: Damage anpassen:
        if (Objects.equals(card1.getCardType(), "Spell") || Objects.equals(card2.getCardType(), "Spell")) { //water > fire; fire > normal; normal > water (effective: doppelte damage; not effective: damage halbiert; no effect: damages bleiben gleich)
            damage1 = applyEffectiveness(card1, card2);
            damage2 = applyEffectiveness(card2, card1);
        }

        battleLog.add("Damage: " + damage1 + " vs " + damage2);

        if (damage1 > damage2) {
            return card1;
        } else if (damage2 > damage1) {
            return card2;
        } else {
            return null; //Unentschieden
        }
    }

    private void logSpecialities(Card winner, Card loser) {
        battleLog.add(winner.getName() + " wins against " + loser.getName());
    }

    private double applyEffectiveness(Card attacker, Card defender) {
        if (attacker.getElementType() == Card.ElementType.WATER && defender.getElementType() == Card.ElementType.FIRE) {
            return attacker.getDamage() * 2;
        }
        if (attacker.getElementType() == Card.ElementType.FIRE && defender.getElementType() == Card.ElementType.NORMAL) {
            return attacker.getDamage() * 2;
        }
        if (attacker.getElementType() == Card.ElementType.NORMAL && defender.getElementType() == Card.ElementType.WATER) {
            return attacker.getDamage() * 2;
        }
        if (attacker.getElementType() == Card.ElementType.FIRE && defender.getElementType() == Card.ElementType.WATER) {
            return attacker.getDamage() / 2;
        }
        if (attacker.getElementType() == Card.ElementType.NORMAL && defender.getElementType() == Card.ElementType.FIRE) {
            return attacker.getDamage() / 2;
        }
        if (attacker.getElementType() == Card.ElementType.WATER && defender.getElementType() == Card.ElementType.NORMAL) {
            return attacker.getDamage() / 2;
        }
        return attacker.getDamage(); //Keine Effektivität
    }
}
