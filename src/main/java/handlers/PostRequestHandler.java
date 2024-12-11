package handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Card;
import models.CardFactory;
import models.Package;
import models.User;
import server.HttpHeaders;
import server.HttpRequestLine;
import services.AuthorisationService;
import services.PackageService;
import services.UserService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostRequestHandler {

    private final AuthorisationService authorisationService = new AuthorisationService();
    private final UserService userService = new UserService();
    private final PackageService packageService = new PackageService();

    public void handlePostRequest(HttpRequestLine requestLine, HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws SQLException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            if ("/users".equals(requestLine.getPath())) {
                // Registrierung von User
                handleUserRegistration(requestBody, out);
            } else if ("/sessions".equals(requestLine.getPath())) {
                // Login von User
                handleUserLogin(requestBody, out);
            } else if ("/packages".equals(requestLine.getPath())) {
                // Hinzufügen eines neuen Packages
                handlePackageCreation(headers, requestBody, out);
            } else if ("/transactions/packages".equals(requestLine.getPath())) {
                // Paket von User erwerben
                handlePackageAcquisition(headers, out);
            } else if (requestLine.getPath().startsWith("/tradings")) {
                // Noch nicht implementierte Funktionalität
                createResponseDoesNotExist(out);
            } else if ("/battles".equals(requestLine.getPath())) {
                // Noch nicht implementierte Funktionalität
                createResponseDoesNotExist(out);
            } else {
                // Fallback für nicht unterstützte Endpoints
                out.write("HTTP/1.1 405 Method Not Allowed\r\nContent-Type: text/plain\r\n\r\nThis endpoint is not supported.");
                out.flush();
            }
        } catch (IllegalArgumentException e) {
            // Antwort für ungültige Autorisierung
            out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage());
            out.flush();
        } catch (IllegalStateException e) {
            // Antwort für ungültige Anfragen
            out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage());
            out.flush();
        } catch (SQLException e) {
            // Antwort für Datenbankfehler
            e.printStackTrace();
            out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDatabase error occurred.");
            out.flush();
        }
    }

    private void handleUserRegistration(StringBuilder requestBody, BufferedWriter out) throws IOException, SQLException {
        // JSON-Body in ein User-Objekt umwandeln
        User user = new ObjectMapper().readValue(requestBody.toString(), User.class);
        boolean success = userService.registerUser(user); // User registrieren
        if (success) {
            out.write("HTTP/1.1 201 Created\r\nContent-Type: text/plain\r\n\r\nUser registered successfully.");
        } else {
            out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nUser already exists.");
        }
        out.flush();
    }

    private void handleUserLogin(StringBuilder requestBody, BufferedWriter out) throws IOException, SQLException {
        // JSON-Body in ein User-Objekt umwandeln
        User user = new ObjectMapper().readValue(requestBody.toString(), User.class);
        String token = userService.loginUser(user); // Login durchführen und Token generieren
        if (token != null) {
            out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"token\":\"" + token + "\"}");
        } else {
            out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\nInvalid login credentials.");
        }
        out.flush();
    }

    private void handlePackageCreation(HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException, SQLException {
        // Admin-Berechtigung überprüfen
        authorisationService.validateAdmin(headers);

        // JSON-Body in eine Liste von Karten umwandeln
        List<Map<String, Object>> cardData = new ObjectMapper().readValue(
                requestBody.toString(),
                new TypeReference<List<Map<String, Object>>>() {}
        );

        // Karten erstellen
        List<Card> cards = new ArrayList<>();
        for (Map<String, Object> data : cardData) {
            UUID id = UUID.fromString((String) data.get("Id"));
            String name = (String) data.get("Name");
            double damage = ((Number) data.get("Damage")).doubleValue();
            Card card = CardFactory.createCard(id, name, damage);
            cards.add(card);
        }

        // Package erstellen und hinzufügen
        models.Package pkg = new Package(cards);
        boolean success = packageService.addPackage(pkg);
        if (success) {
            out.write("HTTP/1.1 201 Created\r\nContent-Type: text/plain\r\n\r\nPackage added successfully.");
        } else {
            out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nFailed to add package.");
        }
        out.flush();
    }

    private void handlePackageAcquisition(HttpHeaders headers, BufferedWriter out) throws SQLException, IOException {
        // Benutzer mit gültigem Token autorisieren
        User user = authorisationService.validateToken(headers.getHeader("Authorization"));

        // Paket erwerben
        boolean success = packageService.acquirePackage(user, userService);
        if (success) {
            out.write("HTTP/1.1 201 Created\r\nContent-Type: text/plain\r\n\r\nPackage acquired successfully.");
        } else {
            out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nAn unknown error occurred.");
        }
        out.flush();
    }

    public void createResponseDoesNotExist(BufferedWriter out) throws IOException {
        // Platzhalter für nicht implementierte Methoden
        out.write("HTTP/1.1 501 Not Implemented\r\nContent-Type: text/plain\r\n\r\nThis method is not implemented yet.");
        out.flush();
    }
}
