package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public final class DbMigrations {

    private DbMigrations() {
    }

    /**
     * Applique un fichier SQL depuis /resources (ex: "/db/Club.sql").
     * Idempotent si ton SQL utilise IF NOT EXISTS / etc.
     */
    public static void applySchema(Connection c, String resourcePath) {
        String sql = readResource(resourcePath);

        // Split simple par ';' (OK pour tes scripts actuels)
        for (String stmt : sql.split(";")) {
            String s = stmt.trim();
            if (s.isEmpty())
                continue;

            try (var ps = c.prepareStatement(s)) {
                ps.execute();
            } catch (Exception e) {
                throw new RuntimeException("Erreur migration SQL sur: " + s, e);
            }
        }
    }

    /**
     * Applique plusieurs scripts dans l'ordre.
     */
    public static void applySchemas(Connection c, String... resourcePaths) {
        if (resourcePaths == null)
            return;
        for (String p : resourcePaths) {
            applySchema(c, p);
        }
    }

    private static String readResource(String path) {
        try (InputStream in = DbMigrations.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Resource introuvable: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de lire " + path, e);
        }
    }
}