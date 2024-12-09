package handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Card;
import models.User;
import server.HttpHeaders;
import server.HttpRequestLine;
import services.UserService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetRequestHandler { //Klasse hat bis jetzt noch keinen Nutzen außer Placeholder für API Endpoint

    public void handleGetRequest(HttpRequestLine requestLine, HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException {
        if (requestLine.getPath().startsWith("/users/")) { //Benutzerdaten lesen
            String[] pathParts = requestLine.getPath().split("/");
            if (pathParts.length != 3) {
                out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nInvalid URL format.");
                out.flush();
                return;
            }

            String username = pathParts[2];
            String authHeader = headers.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\nAuthorization header missing or invalid.");
                out.flush();
                return;
            }

            String token = authHeader.substring("Bearer ".length());
            try {
                UserService userService = new UserService();
                User user = userService.getUserByToken(token);
                if (user == null || !user.getUsername().equals(username)) {
                    out.write("HTTP/1.1 403 Forbidden\r\nContent-Type: text/plain\r\n\r\nYou are not allowed to access this user's data.");
                    out.flush();
                    return;
                }

                User requestedUser = userService.getUserByUsername(username);
                if (requestedUser == null) {
                    out.write("HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nUser not found.");
                    out.flush();
                    return;
                }

                // Nur die benötigten Felder in das JSON übernehmen
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> userData = new HashMap<>();
                userData.put("Username", requestedUser.getUsername());
                userData.put("Name", requestedUser.getName());
                userData.put("Bio", requestedUser.getBio());
                userData.put("Image", requestedUser.getImage());

                String jsonResponse = objectMapper.writeValueAsString(userData);

                out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + jsonResponse);
                out.flush();

            } catch (SQLException e) {
                e.printStackTrace();
                out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDatabase error occurred.");
                out.flush();
            }
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
            String authHeader = headers.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\nAuthorization header missing or invalid.");
                out.flush();
                return;
            }

            String token = authHeader.substring("Bearer ".length());
            try {
                UserService userService = new UserService();
                User user = userService.getUserByToken(token);
                if (user == null) {
                    out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\nInvalid token.");
                    out.flush();
                    return;
                }

                List<Card> deck = userService.getDeck(user);

                // Extrahiere Query-Parameter aus der URL
                String[] pathParts = requestLine.getPath().split("\\?");
                String format = "json"; // Standard: JSON
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
                    // Plain-Text-Ausgabe
                    StringBuilder plainOutput = new StringBuilder();
                    for (Card card : deck) {
                        plainOutput.append(card.getName()).append(" (")
                                .append(card.getElementType()).append(", ")
                                .append(card.getDamage()).append(")\n");
                    }
                    out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n" + plainOutput.toString());
                } else {
                    // JSON-Ausgabe
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

            } catch (SQLException e) {
                e.printStackTrace();
                out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDatabase error occurred.");
                out.flush();
            }
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
