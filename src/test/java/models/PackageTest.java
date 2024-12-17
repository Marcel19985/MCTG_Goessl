package models;

import database.DatabaseConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import services.PackageService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import static org.mockito.Mockito.*;

class PackageTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    public void testAddValidPackage() throws SQLException {
        // Mock die Datenbankverbindung und PreparedStatement
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        // Mock den DatabaseConnector
        try (MockedStatic<DatabaseConnector> dbMock = mockStatic(DatabaseConnector.class)) {
            dbMock.when(DatabaseConnector::connect).thenReturn(mockConnection);

            // Kartenliste erstellen
            List<Card> cards = List.of(
                    CardFactory.createCard(UUID.randomUUID(), "FireGoblin", 10.0),
                    CardFactory.createCard(UUID.randomUUID(), "WaterSpell", 15.0),
                    CardFactory.createCard(UUID.randomUUID(), "NormalKnight", 20.0),
                    CardFactory.createCard(UUID.randomUUID(), "FireSpell", 25.0),
                    CardFactory.createCard(UUID.randomUUID(), "WaterGoblin", 30.0)
            );

            // Testlogik
            Package pkg = new Package(cards);
            PackageService packageService = new PackageService();

            boolean result = packageService.addPackage(pkg);
            assertTrue(result);

            // Verifiziere, dass SQL-Abfragen ausgeführt wurden
            verify(mockStatement, times(1)).executeUpdate();
        }
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