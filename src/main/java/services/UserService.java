package services;

import database.DatabaseConnector;
import models.Card;
import models.User;
import models.Package;

import java.sql.Connection; //für prepared statemenet object conn
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


public class UserService {

    //Registrierung:
    public boolean registerUser(User user) throws SQLException { //lieber nur Objekt übergeben statt string

        try (Connection conn = DatabaseConnector.connect()) {
            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { //unique contraint violation 23505: checkt, ob username eingefügt werden soll, der bereits existiert
                return false;
            } else {
                throw e; //Weiterwerfen von anderer Exception
            }
        }
    }

    //Token Generierung:
    public String generateToken(User user) {
        return user.getUsername() + "-mtcgToken";
    }

    //Login: gibt Token zurück (user spezifisch)
    public String loginUser(User user) throws SQLException {
        try (Connection conn = DatabaseConnector.connect()) {
            String selectQuery = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            selectStmt.setString(1, user.getUsername());
            selectStmt.setString(2, user.getPassword());

            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) { //wenn username und password übereinstimmen:
                String token = generateToken(user); //generiere token
                String updateQuery = "UPDATE users SET token = ? WHERE username = ?"; //speichere Token
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, token);
                updateStmt.setString(2, user.getUsername());
                updateStmt.executeUpdate();

                return token;
            }
        }
        return null;  //Login fehlgeschlagen
    }

    //Überprüfung, ob Token zu einem User gehört: wird vermutlich nicht in dieser Art verwendet werden!
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

    //löscht alle Datensätze aus Tabelle users (wird für einen Test benötigt):
    public void clearTable() throws SQLException {
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users")) {
            statement.executeUpdate();
        }
    }

    public void updateCoins(User user, Connection conn) throws SQLException {
        String updateCoinsQuery = "UPDATE users SET coins = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateCoinsQuery)) {
            stmt.setInt(1, user.getCoins());
            stmt.setObject(2, user.id); //!maybe getter verwenden
            stmt.executeUpdate();
        }
    }

    public void assignCardsToUser(Package pkg, User user, Connection conn) throws SQLException {
        String updateCardsQuery = "UPDATE cards SET user_id = ? WHERE card_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateCardsQuery)) {
            for (Card card : pkg.getCards()) {
                stmt.setObject(1, user.id);
                stmt.setObject(2, card.getId());
                stmt.executeUpdate();
            }
        }
    }

    public User getUserByToken(String token) throws SQLException {
        String query = "SELECT id, username, password, token, name, bio, image, coins FROM users WHERE token = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("token"),
                        rs.getString("name"),
                        rs.getString("bio"),
                        rs.getString("image"),
                        rs.getInt("coins")
                );
            }
        }
        return null; // Kein Benutzer mit dem angegebenen Token gefunden
    }

}