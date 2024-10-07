package models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        // Ensure the user is registered before login
        userService.registerUser(user);
        assertEquals(userService.generateToken(user), userService.loginUser(user));
    }

    @AfterEach
    void tearDown() throws SQLException {
        userService.clearTable();
    }
}