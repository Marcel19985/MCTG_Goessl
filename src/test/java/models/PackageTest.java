package models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.PackageService;

import java.sql.SQLException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class PackageTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    public void testAddValidPackage() throws SQLException {
        List<Card> cards = List.of(
                CardFactory.createCard(UUID.randomUUID(), "FireGoblin", 10.0),
                CardFactory.createCard(UUID.randomUUID(), "WaterSpell", 15.0),
                CardFactory.createCard(UUID.randomUUID(), "NormalKnight", 20.0),
                CardFactory.createCard(UUID.randomUUID(), "FireSpell", 25.0),
                CardFactory.createCard(UUID.randomUUID(), "WaterGoblin", 30.0)
        );
        Package pkg = new Package(cards);
        PackageService packageService = new PackageService();
        boolean result = packageService.addPackage(pkg);
        assertTrue(result);
    }

    @Test
    public void testAddPackageWithLessThanFiveCards() {
        List<Card> cards = List.of(
                CardFactory.createCard(UUID.randomUUID(), "FireSpell", 25.0),
                CardFactory.createCard(UUID.randomUUID(), "WaterGoblin", 30.0)
        );
        assertThrows(IllegalArgumentException.class, () -> new Package(cards));
    }

    @Test
    public void testAddPackageWithMoreThanFiveCards() {
        List<Card> cards = List.of(
                CardFactory.createCard(UUID.randomUUID(), "FireGoblin", 10.0),
                CardFactory.createCard(UUID.randomUUID(), "WaterSpell", 15.0),
                CardFactory.createCard(UUID.randomUUID(), "NormalKnight", 20.0),
                CardFactory.createCard(UUID.randomUUID(), "FireSpell", 25.0),
                CardFactory.createCard(UUID.randomUUID(), "WaterGoblin", 30.0),
                CardFactory.createCard(UUID.randomUUID(), "FireGoblin", 35.0)
        );
        assertThrows(IllegalArgumentException.class, () -> new Package(cards));
    }

    @AfterEach
    void tearDown() {
    }
}