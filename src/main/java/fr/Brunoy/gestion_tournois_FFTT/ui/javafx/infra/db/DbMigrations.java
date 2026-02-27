package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Exécute des scripts SQL depuis les resources.
 * Les scripts doivent être idempotents (IF NOT EXISTS, etc.)
 */
public final class DbMigrations {

    private DbMigrations() {
    }

    public static void applySchema(Connection c, String resourcePath) {
        String sql = readResource(resourcePath);
        String normalized = stripLineComments(sql);

        // Split simple par ';' : OK si tes scripts sont simples (tables/index).
        // Si tu ajoutes triggers/procedures complexes, il faudra un parseur plus
        // robuste.
        String[] statements = normalized.split(";");
        for (String stmt : statements) {
            String s = stmt.trim();
            if (s.isEmpty())
                continue;

            try (Statement st = c.createStatement()) {
                st.execute(s);
            } catch (Exception e) {
                throw new RuntimeException("Erreur migration SQL (" + resourcePath + ") sur:\n" + s, e);
            }
        }
    }

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

    /**
     * Retire les commentaires "-- ..." ligne par ligne.
     * Ne gère pas tous les cas SQL possibles, mais évite beaucoup de faux
     * statements.
     */
    private static String stripLineComments(String sql) {
        StringBuilder out = new StringBuilder(sql.length());
        String[] lines = sql.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--"))
                continue;
            out.append(line).append('\n');
        }
        return out.toString();
    }
}