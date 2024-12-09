package handlers;

import models.Card;
import models.User;
import server.HttpHeaders;
import server.HttpRequestLine;
import services.UserService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class GetRequestHandler { //Klasse hat bis jetzt noch keinen Nutzen außer Placeholder für API Endpoint

    public void handleGetRequest(HttpRequestLine requestLine, HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException {
        if (requestLine.getPath().startsWith("/users")) {
            createResponseDoesNotExist(out);
        } else if ("/cards".equals(requestLine.getPath())) {
            String authHeader = headers.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\nAuthorization header missing or invalid.");
                out.flush();
                return;
            }

            String token = authHeader.substring("Bearer ".length());

            try {
                // Benutzer anhand des Tokens abrufen
                UserService userService = new UserService();
                User user = userService.getUserByToken(token);
                if (user == null) {
                    out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\nInvalid token.");
                    out.flush();
                    return;
                }

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

                // Entferne das letzte Komma und schließe das JSON-Array
                if (!cards.isEmpty()) {
                    jsonOutput.deleteCharAt(jsonOutput.length() - 1);
                }
                jsonOutput.append("]");

                // Erfolgreiche Antwort
                out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + jsonOutput.toString());
                out.flush();

            } catch (SQLException e) {
                e.printStackTrace();
                out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDatabase error occurred.");
                out.flush();
            }
        } else if (requestLine.getPath().startsWith("/deck")) {
            createResponseDoesNotExist(out);
        } else if ("/stats".equals(requestLine.getPath())) {
            createResponseDoesNotExist(out);
        } else if ("/scoreboard".equals(requestLine.getPath())) {
            createResponseDoesNotExist(out);
        } else if ("/tradings".equals(requestLine.getPath())) {
            createResponseDoesNotExist(out);
        }
    }

    public void createResponseDoesNotExist(BufferedWriter out) throws IOException {
        out.write("HTTP/1.1 501 Not Implemented\r\nContent-Type: text/plain\r\n\r\nThis method is not implemented yet.");
        out.flush();
    }

}
