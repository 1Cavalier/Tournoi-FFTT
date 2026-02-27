package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Wrapper simple pour ouvrir des connexions SQLite.
 */
public class SqliteDb {

    private final String jdbcUrl;

    public SqliteDb(Path dbFile) {
        try {
            Path parent = dbFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de créer le dossier DB", e);
        }
        this.jdbcUrl = "jdbc:sqlite:" + dbFile;
    }

    public Connection openConnection() throws SQLException {
        Connection c = DriverManager.getConnection(jdbcUrl);

        // SQLite : les clés étrangères sont désactivées par défaut par connexion.
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }

        return c;
    }

    public String jdbcUrl() {
        return jdbcUrl;
    }
}