package org.example;

import models.User;
import models.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;

    @BeforeEach
    public void setUp() {
        UUID testId = UUID.randomUUID();  // Generates a valid UUID
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
    public void testValidateToken() throws SQLException {
        final UserService userService = new UserService();
        System.out.println(user.getUsername());
        System.out.println(user.getToken());
        try {
            assertFalse(userService.validateToken("lalala123", "lalala123"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
