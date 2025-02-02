package models;

import java.util.UUID;

public class CardFactory {

    public static Card createCard(UUID id, String name, double damage) {
        Card.ElementType elementType = Card.ElementType.NORMAL; //elementType default ist NORMAL
        if (name.contains("Fire")) {
            elementType = Card.ElementType.FIRE;
        } else if (name.contains("Water")) {
            elementType = Card.ElementType.WATER;
        }

        //Card wird created: je nachdem ob spellCard oder monsterCard
        if (name.contains("Spell")) { //SpellCard
            return new SpellCard(id, name, damage, elementType); //Aufruf vom Konstruktor
        } else { //MonsterCard
            return new MonsterCard(id, name, damage, elementType);
        }
    }
}