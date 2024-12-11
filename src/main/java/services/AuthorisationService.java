package services;
import models.User;
import java.sql.SQLException;

public class AuthorisationService {
    private final UserService userService;

    public AuthorisationService() { //Konstruktor
        this.userService = new UserService();
    }

    /**
     * Führt eine vollständige Autorisierung durch:
     * - Validiert das Authorization-Header-Format.
     * - Überprüft den Token.
     * - Stellt sicher, dass der Benutzer Zugriff auf die Ressource hat.
     *
     * @param authHeader Der Authorization-Header.
     * @param targetUsername Der Benutzername der Ressource.
     * @return Der autorisierte Benutzer, falls die Autorisierung erfolgreich ist.
     * @throws IllegalArgumentException Wenn der Header ungültig ist.
     * @throws IllegalStateException Wenn der Benutzer nicht autorisiert ist.
     * @throws SQLException Bei Datenbankfehlern.
     */
    public User authorize(String authHeader, String targetUsername) throws SQLException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header missing or invalid.");
        }

        String token = authHeader.substring("Bearer ".length());
        User user = userService.getUserByToken(token);

        if (user == null || !user.getUsername().equals(targetUsername)) {
            throw new IllegalStateException("You are not allowed to access this user's data.");
        }

        return user;
    }

    /**
     * Validiert das Authorization-Header-Format und überprüft den Benutzer.
     * @param authHeader Der Authorization-Header.
     * @return Der Benutzer, falls der Token gültig ist.
     * @throws IllegalArgumentException Wenn der Header ungültig ist.
     * @throws SQLException Bei Datenbankfehlern.
     */
    public User validateToken(String authHeader) throws SQLException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header missing or invalid.");
        }

        String token = authHeader.substring("Bearer ".length());
        User user = userService.getUserByToken(token);

        if (user == null) {
            throw new IllegalStateException("Invalid token.");
        }

        return user;
    }
}
