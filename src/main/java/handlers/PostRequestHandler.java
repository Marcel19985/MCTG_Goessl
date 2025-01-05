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
import models.Battle;

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
    private final Battle battle = new Battle();

    public void handlePostRequest(HttpRequestLine requestLine, HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws SQLException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            if ("/users".equals(requestLine.getPath())) { //Registrierung
                handleUserRegistration(requestBody, out);
            } else if ("/sessions".equals(requestLine.getPath())) { //Login
                handleUserLogin(requestBody, out);
            } else if ("/packages".equals(requestLine.getPath())) { //Add Package
                handlePackageCreation(headers, requestBody, out);
            } else if ("/transactions/packages".equals(requestLine.getPath())) { //Acquire Package
                handlePackageAcquisition(headers, out);
            } else if (requestLine.getPath().startsWith("/tradings")) { //trade -> nicht implementiert
                createResponseDoesNotExist(out);
            } else if ("/battles".equals(requestLine.getPath())) { //battle
                handleBattle(headers, out);
            } else { //Ungültige Endpoints:
                out.write("HTTP/1.1 405 Method Not Allowed\r\nContent-Type: text/plain\r\n\r\nThis endpoint is not supported.");
                out.flush();
            }
        } catch (IllegalArgumentException e) {
            //Antwort für ungültige Autorisierung:
            out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage());
            out.flush();
        } catch (IllegalStateException e) {
            //Antwort für ungültige Anfragen:
            out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage());
            out.flush();
        } catch (SQLException e) {
            //Antwort Datenbankfehler:
            e.printStackTrace();
            out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDatabase error occurred.");
            out.flush();
        }
    }

    private void handleUserRegistration(StringBuilder requestBody, BufferedWriter out) throws IOException, SQLException {
        User user = new ObjectMapper().readValue(requestBody.toString(), User.class); //Wandelt JSON in Java Objekt User um
        boolean success = userService.registerUser(user); //führt registrierung durch
        if (success) {
            out.write("HTTP/1.1 201 Created\r\nContent-Type: text/plain\r\n\r\nUser registered successfully.");
        } else {
            out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nUser already exists.");
        }
        out.flush();
    }

    private void handleUserLogin(StringBuilder requestBody, BufferedWriter out) throws IOException, SQLException {
        User user = new ObjectMapper().readValue(requestBody.toString(), User.class); //JSON-Body in ein User-Objekt umwandeln
        String token = userService.loginUser(user); //Login durchführen: gibt Token zurück
        if (token != null) {
            out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"token\":\"" + token + "\"}");
        } else {
            out.write("HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\nInvalid login credentials.");
        }
        out.flush();
    }

    private void handlePackageCreation(HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException, SQLException {
        authorisationService.validateAdmin(headers); //Admin-Berechtigung überprüfen

        //Liste bestehend aus Maps: Jede Card ist Map bestehend aus Strings (keys) und Objects (values)
        //fügt die JSON Daten aus requestBody in eine Liste ein:
        List<Map<String, Object>> cardData = new ObjectMapper().readValue(
                requestBody.toString(),
                new TypeReference<List<Map<String, Object>>>() {}
        );

        //Karten erstellen:
        List<Card> cards = new ArrayList<>();
        for (Map<String, Object> data : cardData) { //geht alle Card's durch:
            UUID id = UUID.fromString((String) data.get("Id")); //Wert aus id Feld wird als String aus Map geholt und in UUID umgewandelt
            String name = (String) data.get("Name"); //extrahiere name
            double damage = ((Number) data.get("Damage")).doubleValue(); //extrahiere damage und konvertiert zu double
            Card card = CardFactory.createCard(id, name, damage); //CardFactory, um Card Object's zu erstellen: gibt Objekt der richtigen Kindklasse zurück
            cards.add(card); //Karte zur Liste hinzufügen
        }

        //Package erstellen und hinzufügen:
        models.Package pkg = new Package(cards); //Package erstellen und Liste cards übergeben
        boolean success = packageService.addPackage(pkg); //Package + Card's zur Datenbank hinzufügen
        if (success) {
            out.write("HTTP/1.1 201 Created\r\nContent-Type: text/plain\r\n\r\nPackage added successfully.");
        } else {
            out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nFailed to add package.");
        }
        out.flush();
    }

    private void handlePackageAcquisition(HttpHeaders headers, BufferedWriter out) throws SQLException, IOException {

        User user = authorisationService.validateToken(headers); //Benutzer mit Token autorisieren

        boolean success = packageService.acquirePackage(user, userService); //Paket erwerben
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

    private void handleBattle(HttpHeaders headers, BufferedWriter out) throws SQLException, IOException {
        User player = authorisationService.validateToken(headers);

        // Hinzufügen des Spielers zur BattleQueue
        synchronized (battle.getBattleQueue()) {
            if (battle.getBattleQueue().isEmpty()) {
                // Spieler wird der Queue hinzugefügt, wartet auf Gegner
                battle.addToBattleQueue(player);
                out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nYou are waiting for an opponent.");
            } else {
                //Spieler aus der Queue wird entfernt und Battle gestartet
                User opponent = battle.removeFromBattleQueue();
                List<String> battleLog = battle.startBattle(player, opponent);

                //Log als JSON zurückgeben:
                String response = new ObjectMapper().writeValueAsString(battleLog);
                out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + response + "\r\n\r\n");
            }
            out.flush();
        }
    }

}
