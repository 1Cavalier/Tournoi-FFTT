package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteDb {

    private final String jdbcUrl;

    public SqliteDb(Path dbFile) {
        try {
            Path parent = dbFile.getParent();
            if (parent != null)
                Files.createDirectories(parent);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de créer le dossier DB", e);
        }
        this.jdbcUrl = "jdbc:sqlite:" + dbFile.toString();
    }

    public Connection openConnection() throws SQLException {
        Connection c = DriverManager.getConnection(jdbcUrl);
        c.createStatement().execute("PRAGMA foreign_keys = ON;");
        return c;
    }
}
