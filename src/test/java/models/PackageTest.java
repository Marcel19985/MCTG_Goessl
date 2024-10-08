package models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
                new Card(UUID.randomUUID(), "Card1", 10),
                new Card(UUID.randomUUID(), "Card2", 15),
                new Card(UUID.randomUUID(), "Card3", 20),
                new Card(UUID.randomUUID(), "Card4", 25),
                new Card(UUID.randomUUID(), "Card5", 30)
        );
        Package pkg = new Package(cards);
        PackageService packageService = new PackageService();
        boolean result = packageService.addPackage(pkg);
        assertTrue(result);
    }

    @Test
    public void testAddPackageWithLessThanFiveCards() {
        List<Card> cards = List.of(
                new Card(UUID.randomUUID(), "Card1", 10),
                new Card(UUID.randomUUID(), "Card2", 15)
        );
        assertThrows(IllegalArgumentException.class, () -> new Package(cards));
    }

    @Test
    public void testAddPackageWithMoreThanFiveCards() {
        List<Card> cards = List.of(
                new Card(UUID.randomUUID(), "Card1", 10),
                new Card(UUID.randomUUID(), "Card2", 15),
                new Card(UUID.randomUUID(), "Card3", 20),
                new Card(UUID.randomUUID(), "Card4", 25),
                new Card(UUID.randomUUID(), "Card5", 30),
                new Card(UUID.randomUUID(), "Card6", 35)
        );
        assertThrows(IllegalArgumentException.class, () -> new Package(cards));
    }

    @AfterEach
    void tearDown() {
    }
}