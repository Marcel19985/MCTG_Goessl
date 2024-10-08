package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.User;
import models.UserService;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;

public class HttpServer {

    private static final UserService userService = new UserService();

    public static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            //Parse request line:
            String firstLine = in.readLine();
            HttpRequestLine requestLine = HttpRequestLine.parse(firstLine);

            //Parse headers:
            HttpHeaders headers = HttpHeaders.parse(in);

            //RequestBody auslesen:
            int contentLength = headers.getContentLength();
            StringBuilder requestBody = new StringBuilder();
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                in.read(bodyChars, 0, contentLength);
                requestBody.append(bodyChars);
            }

            ObjectMapper objectMapper = new ObjectMapper();

            //API Endpoints:
            if ("POST".equals(requestLine.getMethod()) && "/users".equals(requestLine.getPath())) {
                User user = objectMapper.readValue(requestBody.toString(), User.class);
                boolean success = userService.registerUser(user);
                String response = success
                        ? "HTTP/1.1 201 Created\r\nContent-Type: text/plain\r\n\r\nUser registered successfully"
                        : "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nUser already exists";
                out.write(response);
                out.flush();
            } else if ("POST".equals(requestLine.getMethod()) && "/sessions".equals(requestLine.getPath())) {
                User user = objectMapper.readValue(requestBody.toString(), User.class);
                String token = userService.loginUser(user);
                String response = token != null
                        ? "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"token\":\"" + token + "\"}"
                        : "HTTP/1.1 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\nInvalid login credentials";
                out.write(response);
                out.flush();
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}