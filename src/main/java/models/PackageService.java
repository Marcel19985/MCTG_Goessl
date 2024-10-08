package models;

import database.DatabaseConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PackageService {

    public boolean addPackage(Package pkg) throws SQLException { //Fügt Package und dessen Karten zu Tabelle packages und cards hinzu:

        String insertPackageSQL = "INSERT INTO packages DEFAULT VALUES RETURNING package_id"; //Statement, um neuen Datensatz (id) in die Tabelle Packages einzufügen
        String insertCardSQL = "INSERT INTO cards (card_id, name, damage, package_id) VALUES (?, ?, ?, ?)"; //Statement für Hinzufügen einer Card

        try (Connection conn = DatabaseConnector.connect()) {
            conn.setAutoCommit(false); //Falls insert in eine Datenbank funktioniert, wird sie nicht direkt gespeichert (sollen beide inserts in beide Tabellen funktionieren oder garkeine)
            UUID packageId;
            try (PreparedStatement insertPackageStmt = conn.prepareStatement(insertPackageSQL)) {
                var resultSet = insertPackageStmt.executeQuery(); //SQL command ausführen und RückgabeSet speichern
                System.out.println(resultSet);
                if (resultSet.next()) { //Überprüfen, ob Datensatz eingefügt wurde
                    packageId = (UUID) resultSet.getObject("package_id"); //packageId aus resultSet extrahieren, da packageId für Fremdschlüssel in Cards verwendet wird
                } else {
                    conn.rollback(); //Rollback falls Datensatz nicht hinzugefügt werden konnte
                    throw new SQLException("Failed to insert package.");
                }
            }

            //Jede Card in Cards Tabelle einfügen:
            try (PreparedStatement insertCardStmt = conn.prepareStatement(insertCardSQL)) {
                for (Card card : pkg.getCards()) { //Iteriere durch alle Cards des Packages
                    insertCardStmt.setObject(1, card.getId(), java.sql.Types.OTHER);
                    insertCardStmt.setString(2, card.getName());
                    insertCardStmt.setDouble(3, card.getDamage());
                    insertCardStmt.setObject(4, packageId, java.sql.Types.OTHER); //Fremdschlüssel
                    insertCardStmt.addBatch(); //Als batch ausführen ist effizienter
                }
                insertCardStmt.executeBatch();
            }

            conn.commit(); //Wenn beide SQL inserts funktioniert haben, kann committed werden:
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; //Fehler aufgetreten
        }
    }
}
