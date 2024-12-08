package services;

//für SQL statements:
import database.DatabaseConnector;
import models.Card;
import models.Package;
import models.SpellCard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; //kann SQL Fehler als exception ausgeben
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

    public boolean acquirePackage(String token) throws SQLException {
        try (Connection conn = DatabaseConnector.connect()) {
            // Verify user and coins
            String userQuery = "SELECT id, coins FROM users WHERE token = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, token);

            ResultSet userRs = userStmt.executeQuery();
            if (!userRs.next()) {
                throw new IllegalArgumentException("Invalid token.");
            }

            UUID userId = UUID.fromString(userRs.getString("id"));
            int coins = userRs.getInt("coins");

            if (coins < 5) {
                throw new IllegalStateException("Not enough coins.");
            }

            // Get the first available package
            String packageQuery = "SELECT package_id FROM packages LIMIT 1";
            PreparedStatement packageStmt = conn.prepareStatement(packageQuery);
            ResultSet packageRs = packageStmt.executeQuery();

            if (!packageRs.next()) {
                throw new IllegalStateException("No packages available.");
            }

            UUID packageId = UUID.fromString(packageRs.getString("package_id"));

            // Deduct coins and assign package
            conn.setAutoCommit(false);

            try {
                // Update coins
                String updateCoinsQuery = "UPDATE users SET coins = coins - 5 WHERE id = ?";
                PreparedStatement updateCoinsStmt = conn.prepareStatement(updateCoinsQuery);
                updateCoinsStmt.setObject(1, userId);
                updateCoinsStmt.executeUpdate();

                // Insert package into user_packages
                String assignPackageQuery = "INSERT INTO user_packages (user_id, package_id) VALUES (?, ?)";
                PreparedStatement assignPackageStmt = conn.prepareStatement(assignPackageQuery);
                assignPackageStmt.setObject(1, userId);
                assignPackageStmt.setObject(2, packageId);
                assignPackageStmt.executeUpdate();

                // Delete package from packages table
                String deletePackageQuery = "DELETE FROM packages WHERE package_id = ?";
                PreparedStatement deletePackageStmt = conn.prepareStatement(deletePackageQuery);
                deletePackageStmt.setObject(1, packageId);
                deletePackageStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }


}
