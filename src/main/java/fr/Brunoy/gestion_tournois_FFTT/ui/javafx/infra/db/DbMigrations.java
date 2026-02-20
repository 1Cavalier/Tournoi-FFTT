package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public final class DbMigrations {

    private DbMigrations() {
    }

    public static void applySqlResource(Connection c, String resourcePath) {
        String sql = readResource(resourcePath);
        for (String stmt : sql.split(";")) {
            String s = stmt.trim();
            if (!s.isEmpty()) {
                try (var ps = c.prepareStatement(s)) {
                    ps.execute();
                } catch (Exception e) {
                    throw new RuntimeException("Erreur migration SQL sur: " + s, e);
                }
            }
        }
    }

    private static String readResource(String path) {
        try (InputStream in = DbMigrations.class.getResourceAsStream(path)) {
            if (in == null)
                throw new IllegalStateException("Resource introuvable: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de lire " + path, e);
        }
    }
}