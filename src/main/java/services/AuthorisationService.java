package services;
import models.User;
import server.HttpHeaders;

import java.sql.SQLException;

public class AuthorisationService {
    private final UserService userService = new UserService();

    //Check, ob Admin Token in Request:
    public void validateAdmin(HttpHeaders headers) throws IllegalArgumentException {
        String authHeader = headers.getHeader("Authorization");
        if (authHeader == null || !authHeader.equals("Bearer admin-mtcgToken")) {
            throw new IllegalArgumentException("Unauthorized access. Admin token required.");
        }
    }

    //User wird abgefragt basierend auf übergebenen Token:
    public User validateUser(String authHeader, String username) throws IllegalArgumentException, SQLException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header missing or invalid.");
        }

        String token = authHeader.substring("Bearer ".length());
        User user = userService.getUserByToken(token);

        if (user == null || !user.getUsername().equals(username)) {
            throw new IllegalArgumentException("You are not authorized to perform this action.");
        }

        return user;
    }

    //"Authorization: Bearer altenhof-mtcgToken":
    public User validateToken(HttpHeaders headers) throws SQLException {
        String authHeader = headers.getHeader("Authorization");
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

    //Stimmt übergebener username mit Token überein?:
    public User authorize(HttpHeaders headers, String username) throws SQLException {
        User user = validateToken(headers);

        if (!user.getUsername().equals(username)) {
            throw new IllegalArgumentException("You are not allowed to access this user's data.");
        }
        return user;
    }

}
