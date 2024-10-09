package models;

import java.util.UUID;

public class SpellCard extends Card { //erbt von Klasse Card

    public SpellCard(UUID id, String name, double damage, ElementType elementType) {
        super(id, name, damage, elementType); //ruft überladenen Konstruktor der Elternklasse (Card) auf
    }

    @Override
    public String getCardType() { //Implementiert abstrakte Methode von Klasse Card
        return "Spell";
    }
}