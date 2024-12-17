package services;

import models.Card;
import models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

class UserServiceTest {

    private User user;
    private final UserService userService = new UserService();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testUser");
        user.setPassword("testPassword");
    }

    @Test
    public void testRegisterUser() throws SQLException {
        assertTrue(userService.registerUser(user));
    }

    @Test
    public void testLoginUser() throws SQLException {
        userService.registerUser(user); //User muss vor dem Login registriert sein
        assertEquals(userService.generateToken(user), userService.loginUser(user));
    }

    @Test
    public void testRegisterDuplicateUser() throws SQLException {
        userService.registerUser(user);
        assertFalse(userService.registerUser(user), "Duplicate registration should return false");
    }

    @Test
    public void testUserExistsAfterRegistration() throws SQLException {
        userService.registerUser(user);
        User retrievedUser = userService.getUserByUsername("testUser");

        assertNotNull(retrievedUser);
        assertEquals("testUser", retrievedUser.getUsername());
    }

    @Test
    public void testGenerateToken() {
        String token = userService.generateToken(user);
        assertEquals("testUser-mtcgToken", token, "Token format should match 'username-mtcgToken'");
    }

    @AfterEach
    void tearDown() throws SQLException {
        userService.clearTable();
    }
}