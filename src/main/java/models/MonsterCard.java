package models;

import java.util.UUID;

public class MonsterCard extends Card { //erbt von Klasse Card

    public MonsterCard(UUID id, String name, double damage, ElementType elementType) { //Konstruktur (wird nach dem Defaultkonstruktor von Card aufgerufen)
        super(id, name, damage, elementType); //ruft den überladenen Konstruktor der Elternklasse auf
    }

    @Override
    public String getCardType() { //wird implementiert von Elternklasse (abstrakte Methode in Card Klasse)
        return "Monster";
    }
}