package fr.pingmanager.gestion_tournois_FFTT.infra.security;

/**
 * Politique de mot de passe.
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
     * Validation avec message explicite.
     */
    public static void validateOrThrow(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Mot de passe obligatoire.");
        }

        if (password.length() < MIN_LEN) {
            throw new IllegalArgumentException(
                    "Mot de passe trop court (min " + MIN_LEN + " caractères).");
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {

            if (!hasLetter && Character.isLetter(c)) {
                hasLetter = true;
            }

            if (!hasDigit && Character.isDigit(c)) {
                hasDigit = true;
            }

            if (hasLetter && hasDigit) {
                return;
            }
        }

        if (!hasLetter) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 1 lettre.");
        }

        if (!hasDigit) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 1 chiffre.");
        }
    }
}