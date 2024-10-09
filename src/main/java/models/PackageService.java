package models;

//für SQL statements:
import database.DatabaseConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException; //kann SQL Fehler als exception ausgeben

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


}
