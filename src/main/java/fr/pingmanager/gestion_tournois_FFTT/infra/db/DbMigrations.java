package fr.pingmanager.gestion_tournois_FFTT.infra.db;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Exécute des scripts SQL depuis les resources.
 * Les scripts doivent être idempotents (CREATE TABLE IF NOT EXISTS, etc.)
 */
public final class DbMigrations {

    private DbMigrations() {
    }

    public static void applySchema(Connection connection, String resourcePath) {

        String sql = readResource(resourcePath);
        String normalized = stripLineComments(sql);
        String[] statements = normalized.split(";\\s*\\n");

        for (String stmt : statements) {
            String s = stmt.trim();

            if (s.isEmpty()) {
                continue;
            }
            try (Statement st = connection.createStatement()) {

                st.execute(s);

            } catch (Exception e) {

                throw new RuntimeException(
                        "Erreur migration SQL (" + resourcePath + ") sur:\n" + s,
                        e);
            }
        }
    }

    public static void applySchemas(Connection connection, String... resourcePaths) {

        if (resourcePaths == null) {
            return;
        }

        for (String path : resourcePaths) {
            applySchema(connection, path);
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
     * Supprime les lignes de commentaire SQL commençant par "--".
     */
    private static String stripLineComments(String sql) {

        StringBuilder out = new StringBuilder(sql.length());
        String[] lines = sql.split("\n");

        for (String line : lines) {

            String trimmed = line.trim();

            if (trimmed.startsWith("--")) {
                continue;
            }

            out.append(line).append('\n');
        }

        return out.toString();
    }
}