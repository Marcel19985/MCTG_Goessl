package handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.User;
import server.HttpHeaders;
import server.HttpRequestLine;
import services.AuthorisationService;
import services.UserService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PutRequestHandler {

    private final AuthorisationService authorisationService = new AuthorisationService();
    private final UserService userService = new UserService();

    public void handlePutRequest(HttpRequestLine requestLine, HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException {

        try {
            if (requestLine.getPath().startsWith("/deck")) {
                // Deck-Konfiguration
                handleDeckConfiguration(headers, requestBody, out);
            } else if (requestLine.getPath().startsWith("/users/")) {
                // Benutzer-Daten aktualisieren
                handleUserUpdate(requestLine, headers, requestBody, out);
            } else {
                // Nicht implementierter Endpunkt
                createResponseDoesNotExist(out);
            }
        } catch (IllegalArgumentException e) {
            out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage());
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDatabase error occurred.");
            out.flush();
        }
    }

    private void handleDeckConfiguration(HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException, SQLException {
        // Benutzer mit Token validieren
        User user = authorisationService.validateToken(headers);

        // JSON-Body in eine Liste von Karten-IDs umwandeln
        ObjectMapper objectMapper = new ObjectMapper();
        List<UUID> cardIds = objectMapper.readValue(requestBody.toString(), new TypeReference<List<UUID>>() {});

        if (cardIds.size() != 4) {
            throw new IllegalArgumentException("Deck must consist of exactly 4 cards.");
        }

        // Deck konfigurieren
        userService.configureDeck(user, cardIds);
        out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nDeck configured successfully.");
        out.flush();
    }

    private void handleUserUpdate(HttpRequestLine requestLine, HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException, SQLException {
        // Pfad aufteilen und Benutzername extrahieren
        String[] pathParts = requestLine.getPath().split("/");
        if (pathParts.length != 3) {
            throw new IllegalArgumentException("Invalid URL format.");
        }
        String username = pathParts[2];

        // Berechtigung prüfen
        User user = authorisationService.validateUser(headers.getHeader("Authorization"), username);

        // JSON-Body in aktualisierte Benutzerdaten umwandeln
        ObjectMapper objectMapper = new ObjectMapper();
        User updatedData = objectMapper.readValue(requestBody.toString(), User.class);

        // Benutzerdaten aktualisieren
        boolean success = userService.updateUserData(user.getUsername(), updatedData);
        if (success) {
            out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nUser data updated successfully.");
        } else {
            out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nFailed to update user data.");
        }
        out.flush();
    }

    public void createResponseDoesNotExist(BufferedWriter out) throws IOException {
        out.write("HTTP/1.1 501 Not Implemented\r\nContent-Type: text/plain\r\n\r\nThis method is not implemented yet.");
        out.flush();
    }

}
