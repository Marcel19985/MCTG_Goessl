package handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.User;
import server.HttpHeaders;
import server.HttpRequestLine;
import services.UserService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PutRequestHandler {

    public void handlePutRequest(HttpRequestLine requestLine, HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException {
        if (requestLine.getPath().startsWith("/deck")) {
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

                ObjectMapper objectMapper = new ObjectMapper();
                List<UUID> cardIds = objectMapper.readValue(requestBody.toString(), new TypeReference<List<UUID>>() {});
                if (cardIds.size() != 4) {
                    out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nDeck must consist of exactly 4 cards.");
                    out.flush();
                    return;
                }

                userService.configureDeck(user, cardIds);

                out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nDeck configured successfully.");
                out.flush();

            } catch (IllegalArgumentException e) {
                out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage());
                out.flush();
            } catch (SQLException e) {
                e.printStackTrace();
                out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDatabase error occurred.");
                out.flush();
            }
        } else if (requestLine.getPath().startsWith("/users/")) {
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
                    out.write("HTTP/1.1 403 Forbidden\r\nContent-Type: text/plain\r\n\r\nYou are not allowed to update this user's data.");
                    out.flush();
                    return;
                }

                ObjectMapper objectMapper = new ObjectMapper();
                User updatedData = objectMapper.readValue(requestBody.toString(), User.class);

                boolean success = userService.updateUserData(user.getUsername(), updatedData);
                if (success) {
                    out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nUser data updated successfully.");
                } else {
                    out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nFailed to update user data.");
                }
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
                out.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nInvalid request body.");
                out.flush();
            } catch (SQLException e) {
                e.printStackTrace();
                out.write("HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDatabase error occurred.");
                out.flush();
            }
        }
    }

    public void createResponseDoesNotExist(BufferedWriter out) throws IOException {
        out.write("HTTP/1.1 501 Not Implemented\r\nContent-Type: text/plain\r\n\r\nThis method is not implemented yet.");
        out.flush();
    }

}
