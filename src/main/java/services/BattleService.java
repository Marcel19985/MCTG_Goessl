package services;

import models.Card;
import models.Deck;
import models.User;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Random;

public class BattleService {
    private static final int MAX_ROUNDS = 100;
    private final List<String> battleLog = new ArrayList<>(); //BattleLog wird in Liste gespeichert, bevor es ausgegeben wird

    private static final Queue<User> battleQueue = new ConcurrentLinkedQueue<>(); // Warteschlange für Spieler, der auf einen Gegner wartet

    public static Queue<User> getBattleQueue() { //get Warteschlange
        return battleQueue;
    }

    public static void addToBattleQueue(User user) { //Spieler in Queue einfügen
        battleQueue.add(user);
        System.out.println("User added to queue: " + user.getUsername());
    }

    public static User removeFromBattleQueue() { //entfernt ersten Spieler aus Queue
        User user = battleQueue.poll();
        System.out.println("User removed from queue: " + (user != null ? user.getUsername() : "None"));
        return user;
    }

    //Battle Logik:
    public List<String> startBattle(User player1, User player2) throws SQLException {
        System.out.println("Starting battle between " + player1.getUsername() + " and " + player2.getUsername());
        System.out.println("Player 1 deck: " + player1.getDeck());
        System.out.println("Player 2 deck: " + player2.getDeck());

        Deck deck1 = player1.getDeck();
        Deck deck2 = player2.getDeck();

        if (deck1.getCards().isEmpty() || deck2.getCards().isEmpty()) { //Check, ob beide Spieler Karten im Deck haben: eigentlich unnöttig, weil immer 4 Karten im Deck durch curl
            throw new IllegalStateException("Both players must have cards in their decks to battle.");
        }

        int rounds = 0;
        int winStreak1 = 0; //Special feature
        int winStreak2 = 0;
        while (rounds < MAX_ROUNDS && !deck1.getCards().isEmpty() && !deck2.getCards().isEmpty()) {
            rounds++;
            battleLog.add("Round " + rounds + ":");

            //Zufällige Karten aus Deck wählen:
            Card card1 = getRandomCard(deck1);
            Card card2 = getRandomCard(deck2);

            battleLog.add(player1.getUsername() + " plays " + card1.getName());
            battleLog.add(player2.getUsername() + " plays " + card2.getName());

            if (winStreak1 > 2 || winStreak2 > 2) {//Spezialrunde
                Random random = new Random();
                int randomNumber = random.nextInt(3) + 1;
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
                deck2.getCards().remove(card2); //Additional feature 1: bei Unentschieden verlieren beide Spieler die Karten
                deck1.getCards().remove(card1);
            } else if (winner == card1) { //Gewinner der Runde bekommt Karte aus Gegner-Deck:
                battleLog.add(player1.getUsername() + " wins the round!");
                deck2.getCards().remove(card2);
                deck1.addCard(card2);
                winStreak1++;
                winStreak2 = 0;
            } else {
                battleLog.add(player2.getUsername() + " wins the round!");
                deck1.getCards().remove(card1);
                deck2.addCard(card1);
                winStreak2++;
                winStreak1 = 0;
            }
            System.out.println("Player 1 deck: " + player1.getDeck()); //!
            System.out.println("Player 2 deck: " + player2.getDeck()); //!
        }

        //Spielergebnisse:
        if (deck1.getCards().isEmpty()) {
            battleLog.add(player2.getUsername() + " wins the battle atfer " + rounds + " rounds.");
            updateStats(player2, true, false);
            updateStats(player1, false, false);
        } else if (deck2.getCards().isEmpty()) {
            battleLog.add(player1.getUsername() + " wins the battle atfer " + rounds + " rounds.");
            updateStats(player1, true, false);
            updateStats(player2, false, false);
        } else {
            battleLog.add("Battle ended in a draw after " + rounds + " rounds.");
            updateStats(player1, false, true);
            updateStats(player2, false, true);
        }

        return battleLog;
    }

    private Card getRandomCard(Deck deck) {
        Random random = new Random();
        return deck.getCards().get(random.nextInt(deck.getCards().size()));
    }

    private Card determineWinner(Card card1, Card card2) {

        //Logs für Debug:
        System.out.println("Determining winner between " + card1.getName() + " and " + card2.getName());

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
        if (card1 instanceof models.SpellCard || card2 instanceof models.SpellCard) { //water > fire; fire > normal; normal > water (effective: doppelte damage; not effective: damage halbiert; no effect: damages bleiben gleich)
            damage1 = applyEffectiveness(card1, card2);
            damage2 = applyEffectiveness(card2, card1);
        }

        System.out.println("Damage: " + damage1 + " vs " + damage2);
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

    private void updateStats(User player, boolean isWin, boolean isDraw) throws SQLException {
        UserService userService = new UserService();
        if (isWin) {
            player.setElo(player.getElo() + 3);
            player.setWins(player.getWins() + 1);
        } else if (isDraw) {
            player.setDraws(player.getDraws() + 1);
        } else {
            player.setElo(player.getElo() - 5);
            player.setLosses(player.getLosses() + 1);
        }

        userService.updateUserStats(player); //Datenbank aktualisieren
    }
}
