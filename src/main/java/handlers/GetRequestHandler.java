package handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Card;
import models.User;
import server.HttpHeaders;
import server.HttpRequestLine;
import services.AuthorisationService;
import services.UserService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetRequestHandler {

    private final AuthorisationService authorisationService = new AuthorisationService();
    private final UserService userService = new UserService();

    public void handleGetRequest(HttpRequestLine requestLine, HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException {

        try {
            if (requestLine.getPath().startsWith("/users/")) {
                handleUserDetails(requestLine, headers, out);
            } else if ("/cards".equals(requestLine.getPath())) {
                handleUserCards(headers, out);
            } else if (requestLine.getPath().startsWith("/deck")) {
                handleDeck(requestLine, headers, out);
            } else if ("/stats".equals(requestLine.getPath())) {
                createResponseDoesNotExist(out);
            } else if ("/scoreboard".equals(requestLine.getPath())) {
                createResponseDoesNotExist(out);
            } else if ("/tradings".equals(requestLine.getPath())) {
                createResponseDoesNotExist(out);
            } else {
                out.write("HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nEndpoint not found.");
                out.flush();
            }
        } catch (IllegalArgumentException e) {
            out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage());
            out.flush();
        } catch (IllegalStateException e) {
            out.write("HTTP/1.1 403 Forbidden\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage());
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDatabase error occurred.");
            out.flush();
        }
    }

    private void handleUserDetails(HttpRequestLine requestLine, HttpHeaders headers, BufferedWriter out) throws SQLException, IOException {
        String[] pathParts = requestLine.getPath().split("/");
        if (pathParts.length != 3) {
            out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nInvalid URL format.");
            out.flush();
            return;
        }

        String username = pathParts[2];
        String authHeader = headers.getHeader("Authorization");

        // Autorisierung durchführen
        authorisationService.authorize(authHeader, username);

        // Benutzer abrufen
        User requestedUser = userService.getUserByUsername(username);
        if (requestedUser == null) {
            out.write("HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nUser not found.");
            out.flush();
            return;
        }

        // Nur relevante Daten zurückgeben
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> userData = new HashMap<>();
        userData.put("Username", requestedUser.getUsername());
        userData.put("Name", requestedUser.getName());
        userData.put("Bio", requestedUser.getBio());
        userData.put("Image", requestedUser.getImage());

        String jsonResponse = objectMapper.writeValueAsString(userData);

        out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + jsonResponse);
        out.flush();
    }

    private void handleUserCards(HttpHeaders headers, BufferedWriter out) throws SQLException, IOException {
        String authHeader = headers.getHeader("Authorization");

        // Autorisierung durchführen
        User user = authorisationService.validateToken(authHeader);

        // Karten des Benutzers abrufen
        List<Card> cards = userService.getUserCards(user);

        // Karten als JSON formatieren
        StringBuilder jsonOutput = new StringBuilder("[");
        for (Card card : cards) {
            jsonOutput.append("{")
                    .append("\"id\":\"").append(card.getId()).append("\",")
                    .append("\"name\":\"").append(card.getName()).append("\",")
                    .append("\"damage\":").append(card.getDamage()).append(",")
                    .append("\"elementType\":\"").append(card.getElementType()).append("\",")
                    .append("\"type\":\"").append(card.getCardType()).append("\"")
                    .append("},");
        }

        if (!cards.isEmpty()) {
            jsonOutput.deleteCharAt(jsonOutput.length() - 1);
        }
        jsonOutput.append("]");

        out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + jsonOutput.toString());
        out.flush();
    }

    private void handleDeck(HttpRequestLine requestLine, HttpHeaders headers, BufferedWriter out) throws SQLException, IOException {
        String authHeader = headers.getHeader("Authorization");

        // Autorisierung durchführen
        User user = authorisationService.validateToken(authHeader);

        // Deck abrufen
        List<Card> deck = userService.getDeck(user);

        // Query-Parameter auslesen
        String[] pathParts = requestLine.getPath().split("\\?");
        String format = "json"; // Standardformat
        if (pathParts.length > 1) {
            String query = pathParts[1];
            for (String param : query.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "format".equals(keyValue[0]) && "plain".equals(keyValue[1])) {
                    format = "plain";
                    break;
                }
            }
        }

        if ("plain".equals(format)) {
            StringBuilder plainOutput = new StringBuilder();
            for (Card card : deck) {
                plainOutput.append(card.getName()).append(" (")
                        .append(card.getElementType()).append(", ")
                        .append(card.getDamage()).append(")\n");
            }
            out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n" + plainOutput.toString());
        } else {
            StringBuilder jsonOutput = new StringBuilder("[");
            for (Card card : deck) {
                jsonOutput.append("{")
                        .append("\"id\":\"").append(card.getId()).append("\",")
                        .append("\"name\":\"").append(card.getName()).append("\",")
                        .append("\"damage\":").append(card.getDamage()).append(",")
                        .append("\"elementType\":\"").append(card.getElementType()).append("\",")
                        .append("\"type\":\"").append(card.getCardType()).append("\"")
                        .append("},");
            }
            if (!deck.isEmpty()) {
                jsonOutput.deleteCharAt(jsonOutput.length() - 1);
            }
            jsonOutput.append("]");
            out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + jsonOutput.toString());
        }
        out.flush();
    }

    public void createResponseDoesNotExist(BufferedWriter out) throws IOException {
        out.write("HTTP/1.1 501 Not Implemented\r\nContent-Type: text/plain\r\n\r\nThis method is not implemented yet.");
        out.flush();
    }

}
