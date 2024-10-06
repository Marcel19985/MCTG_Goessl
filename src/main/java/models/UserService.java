package models;

import database.DatabaseConnector;
import java.sql.Connection; //für prepared statemenet object conn
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    //Check, ob Benutzer schon in Tabelle existiert:
    public boolean userExists(String username) throws SQLException { //durch throws SQLException benötigt man keinen catch block
        try (Connection conn = DatabaseConnector.connect()) {
            String query = "SELECT 1 FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username); //übergabe der Parameter (1 für ersten Parameter)

            ResultSet rs = stmt.executeQuery();
            return rs.next(); //gibt true zurück falls user existiert (falls mindestens eine Zeile der Tabelle zurückgegeben wurde)
        }
    }

    //Registrierung:
    public boolean registerUser(String username, String password) throws SQLException {
        if (userExists(username)) {
            return false;  //Benutzer exisitiert bereits
        }

        try (Connection conn = DatabaseConnector.connect()) {
            String query = "INSERT INTO users (username, password, token) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, generateToken(username));

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;  //Falls eingefügt, return true
        }
    }

    //Token Generierung:
    public String generateToken(String username) {
        return username + "-mtcgToken";
    }

    //Login: gibt Token zurück (user spezifisch)
    public String loginUser(String username, String password) throws SQLException {
        try (Connection conn = DatabaseConnector.connect()) {
            String query = "SELECT token FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("token");  //return Token
            }
        }
        return null;  //Login fehlgeschlagen
    }

    //Überprüfung, ob Token zu einem User gehört:
    public boolean validateToken(String username, String token) throws SQLException {
        try (Connection conn = DatabaseConnector.connect()) {
            String query = "SELECT 1 FROM users WHERE username = ? AND token = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, token);

            ResultSet rs = stmt.executeQuery();
            return rs.next();  //gibt true zurück, wenn Token valide ist
        }
    }
}