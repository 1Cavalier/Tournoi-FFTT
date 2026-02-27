package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Utilitaires de hash.
 * Attention : SHA-256 seul n'est pas recommandé pour des mots de passe en
 * production.
 * (À terme : BCrypt/Argon2/PBKDF2 + sel)
 */
public final class HashUtils {

    private HashUtils() {
    }

    public static String hash(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input null.");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Hash error", e);
        }
    }

    /**
     * Vérifie si un mot de passe correspond à son hash.
     * Compare en temps constant pour éviter les fuites par timing.
     */
    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        String computed = hash(rawPassword);
        return constantTimeEquals(computed, storedHash);
    }

    private static String toHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        final char[] alphabet = "0123456789abcdef".toCharArray();

        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = alphabet[v >>> 4];
            hex[i * 2 + 1] = alphabet[v & 0x0F];
        }
        return new String(hex);
    }

    /**
     * Comparaison "constant-time" sur strings ASCII (ici hex).
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length())
            return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}