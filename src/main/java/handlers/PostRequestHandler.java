package handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import models.Package;
import server.HttpHeaders;
import server.HttpRequestLine;
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

    public void handlePostRequest (HttpRequestLine requestLine, HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException, SQLException {
        ObjectMapper objectMapper = new ObjectMapper();
        if ("/users".equals(requestLine.getPath())) { //Registrierung von user
            final UserService userService = new UserService();
            User user = objectMapper.readValue(requestBody.toString(), User.class); //Wandelt JSON in Java Objekt User um
            boolean success = userService.registerUser(user); //führt registrierung durch
            if (success) {
                out.write("HTTP/1.1 201 Created\r\nContent-Type: text/plain\r\n\r\nUser registered successfully");
            } else {
                out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nUser already exists");
            }
            out.flush();

        } else if ("/sessions".equals(requestLine.getPath())) { //Login von user
            final UserService userService = new UserService();
            User user = objectMapper.readValue(requestBody.toString(), User.class);
            String token = userService.loginUser(user);
            if (token != null) {
                out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"token\":\"" + token + "\"}");
            } else {
                out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\nInvalid login credentials");
            }
            out.flush();

        } else if ("/packages".equals(requestLine.getPath())) { //Hinzufügen von Package
            //admin Token check:
            String authHeader = headers.getHeader("Authorization");
            if (authHeader == null || !authHeader.equals("Bearer admin-mtcgToken")) { //wenn nicht admin Token:
                out.write("HTTP/1.1 403 Forbidden\r\nContent-Type: text/plain\r\n\r\nUnauthorized access");
                out.flush();
                return;
            }

            //Liste bestehend aus Maps: Jede Card ist Map bestehend aus String (key) und Object (value)
            //fügt die JSON Daten aus requestBody in eine Liste ein:
            List<Map<String, Object>> cardData = objectMapper.readValue(
                    requestBody.toString(),
                    new TypeReference<List<Map<String, Object>>>() {} //Listenelemente: <Map<String, Object>
            );

            List<Card> cards = new ArrayList<>();
            for (Map<String, Object> data : cardData) { //geht alle Card's durch:
                UUID id = UUID.fromString((String) data.get("Id")); //Wert aus id Feld wird als String aus Map geholt und in UUID umgewandelt
                String name = (String) data.get("Name"); //extrahiere name
                double damage = ((Number) data.get("Damage")).doubleValue(); //extrahiere damage und konvertiert zu double
                Card card = CardFactory.createCard(id, name, damage); //CardFactory, um Card Object's zu erstellen: gibt Objekt der richtigen Kindklasse zurück
                cards.add(card); //Karte zur Liste hinzufügen
            }

            PackageService packageService = new PackageService();
            models.Package pkg = new Package(cards); //Package erstellen und Liste cards übergeben
            boolean success = packageService.addPackage(pkg); //Package + Card's zur Datenbank hinzufügen
            if (success) {
                out.write("HTTP/1.1 201 Created\r\nContent-Type: text/plain\r\n\r\nPackage added successfully");
            } else {
                out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nFailed to add package");
            }
            out.flush();

        //noch nicht implementiert:
        } else if (requestLine.getPath().startsWith("/tradings")) {
            createResponseDoesNotExist(out);
        } else if ("/battles".equals(requestLine.getPath())) {
            createResponseDoesNotExist(out);
        } else if ("/transactions/packages".equals(requestLine.getPath())) {
            String authHeader = headers.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\nAuthorization header missing or invalid.");
                out.flush();
                return;
            }

            String token = authHeader.substring("Bearer ".length());
            PackageService packageService = new PackageService();

            try {
                // Ruf die Methode im PackageService auf
                boolean success = packageService.acquirePackage(token);
                if (success) {
                    out.write("HTTP/1.1 201 Created\r\nContent-Type: text/plain\r\n\r\nPackage acquired successfully.");
                } else {
                    out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nFailed to acquire package.");
                }
                out.flush();
            } catch (IllegalArgumentException e) {
                out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage());
                out.flush();
            } catch (IllegalStateException e) {
                out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage());
                out.flush();
            } catch (SQLException e) {
                e.printStackTrace();
                out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDatabase error occurred.");
                out.flush();
            }

        } else {
            out.write("HTTP/1.1 405 Method Not Allowed\r\nContent-Type: text/plain\r\n\r\nThis HTTP method is not supported.");
            out.flush();
        }
    }

    public void createResponseDoesNotExist(BufferedWriter out) throws IOException {
        out.write("HTTP/1.1 501 Not Implemented\r\nContent-Type: text/plain\r\n\r\nThis method is not implemented yet.");
        out.flush();
    }

}
