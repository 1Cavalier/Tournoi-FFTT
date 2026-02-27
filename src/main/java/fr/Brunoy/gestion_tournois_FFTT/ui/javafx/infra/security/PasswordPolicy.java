package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security;

/**
 * Politique de mot de passe (validation côté UI et côté service).
 * Les règles doivent rester simples et explicites.
 */
public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    public static final int MIN_LEN = 8;

    public static String rulesText() {
        return """
                Règles mot de passe :
                - au moins %d caractères
                - au moins 1 lettre
                - au moins 1 chiffre
                """.formatted(MIN_LEN);
    }

    public static boolean isValid(String password) {
        try {
            validateOrThrow(password);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Valide le mot de passe et lève IllegalArgumentException avec un message
     * clair.
     * Note : on ne "trim" pas le mot de passe, car les espaces peuvent faire partie
     * du secret.
     */
    public static void validateOrThrow(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Mot de passe obligatoire.");
        }

        if (password.length() < MIN_LEN) {
            throw new IllegalArgumentException("Mot de passe trop court (min " + MIN_LEN + " caractères).");
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (!hasLetter && Character.isLetter(ch))
                hasLetter = true;
            if (!hasDigit && Character.isDigit(ch))
                hasDigit = true;
            if (hasLetter && hasDigit)
                break;
        }

        if (!hasLetter) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 1 lettre.");
        }
        if (!hasDigit) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 1 chiffre.");
        }
    }
}