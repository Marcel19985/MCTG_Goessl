package server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import models.Package;

//für Kommunikation vom und zum Client:
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID; //UID anstatt fortlaufende ID's in Datenbank

public class HttpServer {

    public static void handlePostRequest (HttpRequestLine requestLine, HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException, SQLException {
        ObjectMapper objectMapper = new ObjectMapper();
        if ("/users".equals(requestLine.getPath())) { //Registrierung von user
            final UserService userService = new UserService();
            User user = objectMapper.readValue(requestBody.toString(), User.class);
            boolean success = userService.registerUser(user);
            if (success) {
                out.write("HTTP/1.1 201 Created\\r\\nContent-Type: text/plain\\r\\n\\r\\nUser registered successfully");
            } else {
                out.write("HTTP/1.1 400 Bad Request\\r\\nContent-Type: text/plain\\r\\n\\r\\nUser already exists");
            }
            out.flush();
        } else if ("/sessions".equals(requestLine.getPath())) { //Login von user
            final UserService userService = new UserService();
            User user = objectMapper.readValue(requestBody.toString(), User.class);
            String token = userService.loginUser(user);
            if (token != null) {
                out.write("HTTP/1.1 200 OK\\r\\nContent-Type: application/json\\r\\n\\r\\n{\\\"token\\\":\\\"\" + token + \"\\\"}");
            } else {
                out.write("HTTP/1.1 401 Unauthorized\\r\\nContent-Type: text/plain\\r\\n\\r\\nInvalid login credentials");
            }
            out.flush();
        } else if ("/packages".equals(requestLine.getPath())) { //Hinzufügen von Package
            // Package creation logic
            String authHeader = headers.getHeader("Authorization");
            if (authHeader == null || !authHeader.equals("Bearer admin-mtcgToken")) {
                out.write("HTTP/1.1 403 Forbidden\r\nContent-Type: text/plain\r\n\r\nUnauthorized access");
                out.flush();
                return;
            }

            //Liste bestehend aus Maps: Jede Card ist Map bestehend aus String (key) und Object (value)
            List<Map<String, Object>> cardData = objectMapper.readValue(
                    requestBody.toString(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            List<Card> cards = new ArrayList<>();
            for (Map<String, Object> data : cardData) { //geht alle Card's durch:
                UUID id = UUID.fromString((String) data.get("Id"));
                String name = (String) data.get("Name");
                double damage = ((Number) data.get("Damage")).doubleValue();
                Card card = CardFactory.createCard(id, name, damage); //CardFactory, um Card Object's zu erstellen
                cards.add(card); //Karte zur Liste hinzufügen
            }

            PackageService packageService = new PackageService();
            Package pkg = new Package(cards); //Package erstellen und Liste cards übergeben
            boolean success = packageService.addPackage(pkg); //Package + Card's zur Datenbank hinzufügen

            if (success) {
                out.write("HTTP/1.1 201 Created\r\nContent-Type: text/plain\r\n\r\nPackage added successfully");
            } else {
                out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nFailed to add package");
            }
            out.flush();
        }
    }

    public static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            //Parse request line:
            String firstLine = in.readLine();
            HttpRequestLine requestLine = HttpRequestLine.parse(firstLine); //neues Objekt

            //Parse headers:
            HttpHeaders headers = HttpHeaders.parse(in); //neues Objekt

            //RequestBody auslesen:
            int contentLength = headers.getContentLength();
            StringBuilder requestBody = new StringBuilder(); //ist effizeint für String- operationen
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                in.read(bodyChars, 0, contentLength);
                requestBody.append(bodyChars);
            }

            //API Endpoints:
            if ("POST".equals(requestLine.getMethod())) {
                handlePostRequest(requestLine, headers, requestBody, out);
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}