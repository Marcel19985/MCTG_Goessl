package services;

//für SQL statements:
import database.DatabaseConnector;
import models.*;
import models.Package;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; //kann SQL Fehler als exception ausgeben
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PackageService {

    public boolean addPackage(Package pkg) throws SQLException {
        String insertPackageSQL = "INSERT INTO packages (package_id) VALUES (?)";
        String insertCardSQL = "INSERT INTO cards (card_id, name, damage, type, element_type, package_id) VALUES (?, ?, ?, ?, ?, ?)"; //sowohl MonsterCard, als auch SpellCard werden in gleicher Tabelle gespeichert (Unterscheidung ist "type")

        try (Connection conn = DatabaseConnector.connect()) { //Verbindung zur Datenbank
            conn.setAutoCommit(false); //keine automatische Transaktions- commits in Datenbank (es sollen entweder Package und Cards eingefügt werden oder garnichts davon)

            //Package in Table packages einfügen:
            try (PreparedStatement insertPackageStmt = conn.prepareStatement(insertPackageSQL)) {
                insertPackageStmt.setObject(1, pkg.getId(), java.sql.Types.OTHER);
                insertPackageStmt.executeUpdate();
            }

            //Card's in cards einfügen:
            try (PreparedStatement insertCardStmt = conn.prepareStatement(insertCardSQL)) {
                for (Card card : pkg.getCards()) { //iteriert über alle Karten im Package
                    insertCardStmt.setObject(1, card.getId(), java.sql.Types.OTHER);
                    insertCardStmt.setString(2, card.getName());
                    insertCardStmt.setDouble(3, card.getDamage());
                    insertCardStmt.setString(4, card instanceof SpellCard ? "Spell" : "Monster"); //wenn card SpellCard ist, dann Type = Spell, ansonsten Type = Monster
                    insertCardStmt.setString(5, card.getElementType().name());
                    insertCardStmt.setObject(6, pkg.getId(), java.sql.Types.OTHER); // Reference package UUID
                    insertCardStmt.addBatch();
                }
                insertCardStmt.executeBatch(); //als batch hinzufügen ist effiziernter
            }

            conn.commit(); // Commit Transaktion, denn jetzt sind sowohl Packages und auch Card's eingefügt
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean acquirePackage(User user, UserService userService) throws SQLException {
        try (Connection conn = DatabaseConnector.connect()) {
            conn.setAutoCommit(false); //Start transaction

            //Schritt 1: verfügbares Paket auswählen:
            String packageQuery = "SELECT package_id FROM packages LIMIT 1 FOR UPDATE";
            UUID packageId;

            try (PreparedStatement packageStmt = conn.prepareStatement(packageQuery);
                 ResultSet packageRs = packageStmt.executeQuery()) {

                if (!packageRs.next()) { //Packages Tabelle leer
                    throw new IllegalStateException("No packages available.");
                }
                packageId = UUID.fromString(packageRs.getString("package_id"));
            }

            //Schritt 2: Karten des Pakets abrufen:
            String cardsQuery = "SELECT card_id, name, damage FROM cards WHERE package_id = ?";
            List<Card> cards = new ArrayList<>();
            try (PreparedStatement cardsStmt = conn.prepareStatement(cardsQuery)) {
                cardsStmt.setObject(1, packageId);
                ResultSet rs = cardsStmt.executeQuery();
                while (rs.next()) { //alle Karten vom Package durchgehen
                    UUID cardId = UUID.fromString(rs.getString("card_id"));
                    String name = rs.getString("name");
                    double damage = rs.getDouble("damage");

                    cards.add(CardFactory.createCard(cardId, name, damage));
                }
            }

            //Schritt 3: Paket erwerben:
            if (!user.buyPackage(new Package(cards), userService, conn)) {
                conn.rollback();
                return false;
            }

            deletePackageById(packageId, conn); //Schritt 4: Paket in von Datenbank löschen

            conn.commit(); //Transaktion abschließen
            return true;

        } catch (IllegalStateException e) {
            throw e;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void deletePackageById(UUID packageId, Connection conn) throws SQLException {
        //Setzt package_id auf NULL für alle Karten des Pakets:
        String updateCardsQuery = "UPDATE cards SET package_id = NULL WHERE package_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateCardsQuery)) {
            stmt.setObject(1, packageId);
            int rowsUpdated = stmt.executeUpdate();
            System.out.println("Cards updated to NULL: " + rowsUpdated);
        }

        //Löscht das Paket:
        String deletePackageQuery = "DELETE FROM packages WHERE package_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deletePackageQuery)) {
            stmt.setObject(1, packageId);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println("Packages deleted: " + rowsDeleted);
        }
    }


}
