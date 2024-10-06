package server;

import com.fasterxml.jackson.databind.ObjectMapper; //Jackson: Für Umwandlung von JSON zu Java-Objekten und umgekehrt
import models.User;
import models.UserService;

import java.io.*; //für BufferedReader + BufferedWriter
import java.net.Socket;
import java.sql.SQLException;

public class HttpServer {

    private static UserService userService = new UserService(); //Erstellt UserService Objekt

    public static void handleClient(Socket clientSocket) { //Infos vom Client werden übergeben
        //BufferedReader: Daten vom Client lesen; BufferedWriter: für Antwort an den Client
        //durch Argumente im try- Block werden BufferedReader und BufferedWriter automatisch geschlossen (muss ansonsten extra mit close geschlossen werden)
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //clientSocket.getInputStream() liest eingehende Daten des Client, InputStreamReder wandelt diesen Bytestrom in Zeichen um, BufferedReader bewirkt effizientes lesen vom Zeichenstrom
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            String firstLine = in.readLine(); //speichert erste Zeile des BufferedReader -> request line: HTTP-Methode(POST,GET,...), Pfad (z.B. \\users) und HTTP Version
            StringBuilder requestBody = new StringBuilder(); //StringBuilder ist praktisch für effiziente Stringoperationen
            String line;
            int contentLength = 0;

            //Lesen des HTTP-Header, bis eine leere Zeile erreicht wird, die die Trennung zwischen Headern und Body markiert: Länge des Request-Body speichern
            while (!(line = in.readLine()).isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim()); //Extrahiert die Länge des Inhalts aus dem Header und konvertiert ihn in eine Ganzzahl.
                } //split teilt Zeichenkette beim Doppelpunkt; [1] zweiter Index im Array; trim() entfernt Leerzeichen
            }

            //Body auslesen
            if (contentLength > 0) { //wenn body nicht leer
                char[] bodyChars = new char[contentLength]; //character Array am heap
                in.read(bodyChars, 0, contentLength);  //liest gesamten Body
                requestBody.append(bodyChars); //Speichert Body in char-Array
            }

            ObjectMapper objectMapper = new ObjectMapper(); //damit kann man JSON in Java-Objekte konvertieren & umgekehrt

            //API Endpoints:
            //POST /users (Registrierung):
            if (firstLine.startsWith("POST /users")) {
                User user = objectMapper.readValue(requestBody.toString(), User.class); //requestBody wird zu Java-Objket umgewanldet (der Klasse User) -> Variablen des user Objekt bekommen direkt die Daten aus dem JSON Body
                boolean success = userService.registerUser(user.username, user.password);
                if (success) {
                    String response = "HTTP/1.1 201 Created\r\nContent-Type: text/plain\r\n\r\nUser registered successfully";
                    out.write(response);
                } else {
                    String response = "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nUser already exists";
                    out.write(response);
                }
                out.flush();
            }

            //POST /sessions (Login)
            if (firstLine.startsWith("POST /sessions")) {
                User user = objectMapper.readValue(requestBody.toString(), User.class);
                String token = userService.loginUser(user.username, user.password);
                if (token != null) {
                    String response = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"token\":\"" + token + "\"}"; //backslshr+backslashn = Zeilenumbruch im HTTP Protokoll
                    out.write(response);
                } else {
                    String response = "HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\nInvalid login credentials";
                    out.write(response);
                }
                out.flush(); //sendet Antwort an Client
            }

            // Handle Secure Actions (Protected by Token)
            if (firstLine.startsWith("POST /secure-action")) {
                User user = objectMapper.readValue(requestBody.toString(), User.class);
                String token = user.token;  // Get the token from the request

                // Validate the token before allowing the action
                if (userService.validateToken(user.username, token)) {
                    // Action is allowed
                    String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nAction allowed for user " + user.username;
                    out.write(response);
                } else {
                    // Invalid token, reject the action
                    String response = "HTTP/1.1 403 Forbidden\r\nContent-Type: text/plain\r\n\r\nInvalid token";
                    out.write(response);
                }
                out.flush();
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}