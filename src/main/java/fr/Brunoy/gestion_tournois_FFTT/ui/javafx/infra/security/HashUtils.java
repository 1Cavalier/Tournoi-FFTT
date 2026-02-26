package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class HashUtils {

    private HashUtils() {
    }

    public static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("Hash error", e);
        }
    }

    /**
     * Vérifie si un mot de passe correspond à son hash.
     */
    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        String hashedInput = hash(rawPassword);
        return hashedInput.equals(storedHash);
    }
}