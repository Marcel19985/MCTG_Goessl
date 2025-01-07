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
            if (requestLine.getPath().startsWith("/deck")) { //configure Deck
                handleDeckConfiguration(headers, requestBody, out);
            } else if (requestLine.getPath().startsWith("/users/")) { //Benutzer-Daten aktualisieren
                handleUserUpdate(requestLine, headers, requestBody, out);
            }
        } catch (IllegalStateException e) { //!!!
            out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage());
            out.flush();
        } catch (IllegalArgumentException e) { //!!!
            out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage());
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDatabase error occurred.");
            out.flush();
        }
    }

    private void handleDeckConfiguration(HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException, SQLException {
        User user = authorisationService.validateToken(headers); //Token validieren und User Objekt zurück geben

        //JSON-Body in eine Liste von Karten-IDs umwandeln:
        ObjectMapper objectMapper = new ObjectMapper();
        List<UUID> cardIds = objectMapper.readValue(requestBody.toString(), new TypeReference<List<UUID>>() {}); //Liste an Karten ID's

        if (userService.configureDeck(user, cardIds)) {
            out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nDeck configured successfully.");
        } else {
            out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nFailed to configure deck.");
        }
        out.flush();
    }


    private void handleUserUpdate(HttpRequestLine requestLine, HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException, SQLException {
        // Pfad aufteilen und Benutzername extrahieren
        String[] pathParts = requestLine.getPath().split("/");
        if (pathParts.length != 3) {
            throw new IllegalArgumentException("Invalid URL format.");
        }
        String username = pathParts[2];

        User user = authorisationService.validateUser(headers, username); //Berechtigung prüfen

        //JSON-Body in aktualisierte Benutzerdaten umwandeln
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

}
