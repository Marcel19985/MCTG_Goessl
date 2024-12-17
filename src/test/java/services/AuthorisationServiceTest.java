package services;

import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpHeaders;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthorisationServiceTest {

    private AuthorisationService authorisationService;
    private UserService userServiceMock;
    private HttpHeaders headersMock;

    @BeforeEach
    void setUp() {
        userServiceMock = mock(UserService.class);
        authorisationService = new AuthorisationService();
        headersMock = mock(HttpHeaders.class);
    }

    @Test
    void testValidateAdmin_ValidAdminToken() {
        when(headersMock.getHeader("Authorization")).thenReturn("Bearer admin-mtcgToken");

        assertDoesNotThrow(() -> authorisationService.validateAdmin(headersMock));
    }

    @Test
    void testValidateAdmin_InvalidAdminToken() {
        when(headersMock.getHeader("Authorization")).thenReturn("Bearer invalid-token");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authorisationService.validateAdmin(headersMock);
        });

        assertEquals("Unauthorized access. Admin token required.", exception.getMessage());
    }

    @Test
    void testValidateUser_InvalidToken() throws SQLException {
        String token = "invalid-token";
        when(headersMock.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(userServiceMock.getUserByToken(token)).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authorisationService.validateUser(headersMock, "testUser");
        });

        assertEquals("You are not authorized to perform this action.", exception.getMessage());
    }

    @Test
    void testValidateToken_InvalidToken() throws SQLException {
        String token = "invalid-token";
        when(headersMock.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(userServiceMock.getUserByToken(token)).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authorisationService.validateToken(headersMock);
        });

        assertEquals("Invalid token.", exception.getMessage());
    }

    @Test
    void testAuthorize_InvalidUsername() throws SQLException {
        String token = "valid-token";
        String username = "testUser";
        String invalidUsername = "wrongUser";

        User mockUser = new User();
        mockUser.setUsername(username);

        when(headersMock.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(userServiceMock.getUserByToken(token)).thenReturn(mockUser);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authorisationService.authorize(headersMock, invalidUsername);
        });

        assertEquals("Invalid token.", exception.getMessage());
    }
}
