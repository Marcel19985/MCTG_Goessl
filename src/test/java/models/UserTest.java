package models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.UserService;

import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;

    @BeforeEach
    public void setUp() {
        UUID testId = UUID.randomUUID();
        user = new User(testId, "testUser", "testPassword", "testToken");
    }

    @Test
    public void testUserConstructor() {
        assertEquals("testUser", user.getUsername());
        assertEquals("testPassword", user.getPassword());
        assertEquals("testToken", user.getToken());
    }

    @Test
    public void testSetUsername() {
        user.setUsername("newuser");
        assertEquals("newuser", user.getUsername());
    }

    @Test
    public void testSetPassword() {
        user.setPassword("newpassword");
        assertEquals("newpassword", user.getPassword());
    }

    @Test
    public void testToJson() throws Exception {
        user.setName("Test Name");
        user.setBio("Test Bio");
        user.setImage("Test Image");

        String expectedJson = "{\"Username\":\"testUser\",\"Bio\":\"Test Bio\",\"Image\":\"Test Image\",\"Name\":\"Test Name\"}";
        assertEquals(expectedJson, user.toJson());
    }

    @Test
    public void testBuyPackage() throws SQLException {
        UserService mockService = mock(UserService.class);

        Package testPackage = new Package(List.of(
                CardFactory.createCard(UUID.randomUUID(), "FireGoblin", 10.0),
                CardFactory.createCard(UUID.randomUUID(), "WaterSpell", 15.0),
                CardFactory.createCard(UUID.randomUUID(), "NormalKnight", 20.0),
                CardFactory.createCard(UUID.randomUUID(), "FireSpell", 25.0),
                CardFactory.createCard(UUID.randomUUID(), "WaterGoblin", 30.0)
        ));

        //Coins vor dem Kauf:
        assertEquals(20, user.getCoins());

        user.buyPackage(testPackage, mockService, null);

        //Prüfe, ob die Coins reduziert wurden:
        assertEquals(15, user.getCoins());
    }

    @Test
    public void testIncreaseWin() {

        user.increaseWins();
        assertEquals(1, user.getWins());
        assertEquals(103, user.getElo());
    }

    @Test
    public void testIncreaseLosses() {

        user.increaseLosses();
        assertEquals(1, user.getLosses());
        assertEquals(95, user.getElo());
    }

    @Test
    public void testIncreaseDraws() {

        user.increaseDraws();
        assertEquals(1, user.getDraws());
        assertEquals(100, user.getElo());
    }

    @Test
    public void testSet() {

        user.increaseDraws();
        assertEquals(1, user.getDraws());
        assertEquals(100, user.getElo());
    }

    @AfterEach
    public void tearDown() {
        user = null;
    }
}
