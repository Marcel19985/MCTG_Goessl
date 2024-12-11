package services;
import models.User;
import server.HttpHeaders;

import java.sql.SQLException;

public class AuthorisationService {
    private final UserService userService = new UserService();

    /**
     * Validates if the authorization header contains a valid admin token.
     *
     //@param authHeader The Authorization header from the HTTP request.
     * @throws IllegalArgumentException if the token is missing or invalid.
     */
    public void validateAdmin(HttpHeaders headers) throws IllegalArgumentException {
        String authHeader = headers.getHeader("Authorization");
        if (authHeader == null || !authHeader.equals("Bearer admin-mtcgToken")) {
            throw new IllegalArgumentException("Unauthorized access. Admin token required.");
        }
    }

    /**
     * Validates a user token and returns the associated User object.
     *
     * @param authHeader The Authorization header from the HTTP request.
     * @return User associated with the token.
     * @throws IllegalArgumentException if the token is missing or invalid.
     * @throws SQLException if a database error occurs.
     */
    public User validateToken(String authHeader) throws SQLException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header missing or invalid.");
        }

        String token = authHeader.substring("Bearer ".length());
        User user = userService.getUserByToken(token);

        if (user == null) {
            throw new IllegalArgumentException("Invalid token.");
        }

        return user;
    }

    /**
     * Validates if the token corresponds to the specified username.
     *
     * @param authHeader The Authorization header from the HTTP request.
     * @param username The username to validate against.
     * @throws IllegalArgumentException if the user is not authorized to access this username.
     * @throws SQLException if a database error occurs.
     */
    public void authorize(String authHeader, String username) throws SQLException {
        User user = validateToken(authHeader);

        if (!user.getUsername().equals(username)) {
            throw new IllegalArgumentException("You are not allowed to access this user's data.");
        }
    }

}
