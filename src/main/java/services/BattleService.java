package services;

import models.Card;
import models.Deck;
import models.User;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BattleService {
    private static final int MAX_ROUNDS = 100;
    private final List<String> battleLog = new ArrayList<>();

    private static final Queue<User> battleQueue = new ConcurrentLinkedQueue<>();

    public static Queue<User> getBattleQueue() {
        return battleQueue;
    }

    public static void addToBattleQueue(User user) {
        battleQueue.add(user);
        System.out.println("User added to queue: " + user.getUsername());
    }

    public static User removeFromBattleQueue() {
        User user = battleQueue.poll();
        System.out.println("User removed from queue: " + (user != null ? user.getUsername() : "None"));
        return user;
    }

    public List<String> startBattle(User player1, User player2) {
        System.out.println("Starting battle between " + player1.getUsername() + " and " + player2.getUsername());
        System.out.println("Player 1 deck: " + player1.getDeck());
        System.out.println("Player 2 deck: " + player2.getDeck());

        Deck deck1 = player1.getDeck();
        Deck deck2 = player2.getDeck();

        if (deck1.getCards().isEmpty() || deck2.getCards().isEmpty()) {
            throw new IllegalStateException("Both players must have cards in their decks to battle.");
        }

        int rounds = 0;
        while (rounds < MAX_ROUNDS && !deck1.getCards().isEmpty() && !deck2.getCards().isEmpty()) {
            rounds++;
            battleLog.add("Round " + rounds + ":");

            // Zufällige Karten auswählen
            Card card1 = getRandomCard(deck1);
            Card card2 = getRandomCard(deck2);

            // Log hinzufügen
            battleLog.add(player1.getUsername() + " plays " + card1.getName());
            battleLog.add(player2.getUsername() + " plays " + card2.getName());

            // Gewinner ermitteln
            Card winner = determineWinner(card1, card2);
            if (winner == null) {
                battleLog.add("It's a tie!");
            } else if (winner == card1) {
                battleLog.add(player1.getUsername() + " wins the round!");
                deck2.getCards().remove(card2);
                deck1.addCard(card2);
            } else {
                battleLog.add(player2.getUsername() + " wins the round!");
                deck1.getCards().remove(card1);
                deck2.addCard(card1);
            }
            System.out.println("Player 1 deck: " + player1.getDeck()); //!
            System.out.println("Player 2 deck: " + player2.getDeck()); //!
        }

        // Spielergebnisse
        if (deck1.getCards().isEmpty()) {
            battleLog.add(player2.getUsername() + " wins the battle!");
        } else if (deck2.getCards().isEmpty()) {
            battleLog.add(player1.getUsername() + " wins the battle!");
        } else {
            battleLog.add("Battle ended in a draw after " + MAX_ROUNDS + " rounds.");
        }

        return battleLog;
    }

    private Card getRandomCard(Deck deck) {
        Random random = new Random();
        return deck.getCards().get(random.nextInt(deck.getCards().size()));
    }

    private Card determineWinner(Card card1, Card card2) {
        // Logs für Fehlerprüfung
        System.out.println("Determining winner between " + card1.getName() + " and " + card2.getName());

        //Goblin vs. Dragon: Dragon gewinnt immer
        if (card1.getName().contains("Goblin") && card2.getName().contains("Dragon")) {
            return card2;
        }
        if (card2.getName().contains("Goblin") && card1.getName().contains("Dragon")) {
            return card1;
        }

        //Ork vs. Wizard: Wizard gewinnt immer
        if (card1.getName().contains("Ork") && card2.getName().contains("Wizard")) {
            return card2;
        }
        if (card2.getName().contains("Ork") && card1.getName().contains("Wizard")) {
            return card1;
        }

        //Knight vs WaterSpell: WaterSpell gewinnt immer
        if (card1.getName().contains("Knight") && card2.getName().contains("WaterSpell")) {
            return card2;
        }
        if (card2.getName().contains("Knight") && card1.getName().contains("WaterSpell")) {
            return card1;
        }

        //Kraken vs Spell: Kraken gewinnt immer
        if (card1.getName().contains("Kraken") && card2 instanceof models.SpellCard) {
            return card1;
        }
        if (card2.getName().contains("Kraken") && card1 instanceof models.SpellCard) {
            return card2;
        }

        //FireElf vs Dragon: FireElf gewinnt immer
        if (card1.getName().contains("FireElf") && card2.getName().contains("Dragon")) {
            return card1;
        }
        if (card2.getName().contains("FireElf") && card1.getName().contains("Dragon")) {
            return card2;
        }

        // Spell effectiveness
        double damage1 = card1.getDamage();
        double damage2 = card2.getDamage();

        if (card1 instanceof models.SpellCard || card2 instanceof models.SpellCard) { //water > fire; fire > normal; normal > water (effective: doppelte damage; not effective: damage halbiert; no effect: damages bleiben gleich)
            damage1 = applyEffectiveness(card1, card2);
            damage2 = applyEffectiveness(card2, card1);
        }

        System.out.println("Damage: " + damage1 + " vs " + damage2);

        if (damage1 > damage2) {
            return card1;
        } else if (damage2 > damage1) {
            return card2;
        } else {
            return null; // Unentschieden
        }
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
        return attacker.getDamage(); // Keine Effektivität
    }
}
