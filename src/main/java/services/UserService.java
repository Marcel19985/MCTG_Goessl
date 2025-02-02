package services;

import database.DatabaseConnector;
import models.*;
import models.Package;

import java.sql.Connection; //für prepared statemenet object conn
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


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

    //löscht alle Datensätze aus Tabelle users (wird für einen Test benötigt):
    public void clearTable() throws SQLException {
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users")) {
            statement.executeUpdate();
        }
    }

    //Connection wird übergeben weil Transaktion schon gestartet hat oder noch nicht endet
    public void updateCoins(User user, Connection conn) throws SQLException { //davor werden in Methode bei user die Coins um 5 verringert
        String updateCoinsQuery = "UPDATE users SET coins = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateCoinsQuery)) {
            stmt.setInt(1, user.getCoins());
            stmt.setObject(2, user.getId());
            stmt.executeUpdate();
        }
    }

    public void assignCardsToUser(Package pkg, User user, Connection conn) throws SQLException {
        String updateCardsQuery = "UPDATE cards SET user_id = ? WHERE card_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateCardsQuery)) {
            for (Card card : pkg.getCards()) {
                stmt.setObject(1, user.getId());
                stmt.setObject(2, card.getId());
                stmt.executeUpdate();
            }
        }
    }

    public List<Card> getUserCards(User user) throws SQLException { //erstellt Liste an Karten, bei der Fremdschlüssel des users gespeichert ist
        String query = "SELECT card_id, name, damage, type, element_type FROM cards WHERE user_id = ?";
        List<Card> cards = new ArrayList<>();

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UUID cardId = UUID.fromString(rs.getString("card_id"));
                String name = rs.getString("name");
                double damage = rs.getDouble("damage");
                Card card = CardFactory.createCard(cardId, name, damage);
                cards.add(card);
            }
        }
        return cards;
    }

    public User getUserByToken(String token) throws SQLException {
        String query = "SELECT id, username, password, token, name, bio, image, coins FROM users WHERE token = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("token"),
                        rs.getString("name"),
                        rs.getString("bio"),
                        rs.getString("image"),
                        rs.getInt("coins")
                );

                List<Card> deckCards = getDeck(user); //Lade das Deck des Benutzers: (siehe Methode darunter)

                if (!deckCards.isEmpty()) { //gibt Deck in Datenbank
                    Deck newDeck = new Deck();
                    newDeck.setCards(deckCards); //Weise Karten dem Deck zu

                    user.setDeck(newDeck); //Weise Deck dem Benutzer zu
                }

                return user;
            }
        }
        return null; // Kein Benutzer mit dem angegebenen Token gefunden
    }

    public List<Card> getDeck(User user) throws SQLException {
        String query = "SELECT c.card_id, c.name, c.damage, c.type, c.element_type " +
                "FROM decks d JOIN cards c ON d.card_id = c.card_id " +
                "WHERE d.user_id = ?"; //JOIN -> Karten müssen im Deck und in Cards vorhanden sein
        List<Card> deck = new ArrayList<>();

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UUID cardId = UUID.fromString(rs.getString("card_id"));
                String name = rs.getString("name");
                double damage = rs.getDouble("damage");
                Card card = CardFactory.createCard(cardId, name, damage);
                deck.add(card);
            }
        }
        return deck;
    }

    public User getUserByUsername(String username) throws SQLException { //für Ausgabe von einem Userprofil
        String query = "SELECT id, username, name, bio, image, coins FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("username"),
                        null, //Passwort wird hier nicht benötigt
                        null, //Token wird hier nicht benötigt
                        rs.getString("name"),
                        rs.getString("bio"),
                        rs.getString("image"),
                        rs.getInt("coins")
                );
            }
        }
        return null; // Benutzer nicht gefunden
    }

    public boolean configureDeck(User user, List<UUID> cardIds) throws SQLException {
        if (cardIds.size() != 4) {
            throw new IllegalStateException("A deck must consist of exactly 4 cards.");
        }

        try (Connection conn = DatabaseConnector.connect()) {
            conn.setAutoCommit(false);

            //Überprüfen, ob alle Karten dem Benutzer gehören:
            String cardCheckQuery = "SELECT card_id, name, damage, type, element_type FROM cards WHERE user_id = ? AND card_id = ?";
            List<Card> newDeckCards = new ArrayList<>();

            try (PreparedStatement checkStmt = conn.prepareStatement(cardCheckQuery)) {
                for (UUID cardId : cardIds) {
                    checkStmt.setObject(1, user.getId());
                    checkStmt.setObject(2, cardId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        Card card = CardFactory.createCard(
                                UUID.fromString(rs.getString("card_id")),
                                rs.getString("name"),
                                rs.getDouble("damage")
                        );
                        newDeckCards.add(card);
                    } else {
                        throw new IllegalStateException("User does not own all the specified cards.");
                    }
                }
            }

            //Bestehendes Deck löschen:
            String deleteDeckQuery = "DELETE FROM decks WHERE user_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteDeckQuery)) {
                deleteStmt.setObject(1, user.getId());
                deleteStmt.executeUpdate();
            }

            //Neues Deck einfügen:
            String insertDeckQuery = "INSERT INTO decks (user_id, card_id) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertDeckQuery)) {
                for (UUID cardId : cardIds) {
                    insertStmt.setObject(1, user.getId());
                    insertStmt.setObject(2, cardId);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            conn.commit();
            user.getDeck().setCards(newDeckCards);
            return true;
        }
    }

    public boolean updateUserData(String username, User updatedData) throws SQLException {
        String query = "UPDATE users SET name = ?, bio = ?, image = ? WHERE username = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, updatedData.getName());
            stmt.setString(2, updatedData.getBio());
            stmt.setString(3, updatedData.getImage());
            stmt.setString(4, username);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public void updateUserStats(User user) throws SQLException {
        String query = "UPDATE users SET elo = ?, wins = ?, draws = ?, losses = ? WHERE id = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getElo());
            stmt.setInt(2, user.getWins());
            stmt.setInt(3, user.getDraws());
            stmt.setInt(4, user.getLosses());
            stmt.setObject(5, user.getId());
            stmt.executeUpdate();
        }
    }

    public Map<String, Object> getUserStats(User user) throws SQLException {
        String query = "SELECT username, elo, wins, draws, losses FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Benutzerstatistiken in einer Map speichern
                    Map<String, Object> userStats = new LinkedHashMap<>();
                    userStats.put("username", rs.getString("username"));
                    userStats.put("elo", rs.getInt("elo"));
                    userStats.put("wins", rs.getInt("wins"));
                    userStats.put("draws", rs.getInt("draws"));
                    userStats.put("losses", rs.getInt("losses"));
                    return userStats;
                }
            }
        }
        return null; // Benutzer nicht gefunden
    }

    public List<User> getAllUsersSortedByElo() throws SQLException {
        String query = "SELECT id, username, elo, wins, draws, losses FROM users ORDER BY elo DESC";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setId(UUID.fromString(rs.getString("id")));
                user.setUsername(rs.getString("username"));
                user.setElo(rs.getInt("elo"));
                user.setWins(rs.getInt("wins"));
                user.setDraws(rs.getInt("draws"));
                user.setLosses(rs.getInt("losses"));
                users.add(user);
            }
        }
        return users;
    }

}