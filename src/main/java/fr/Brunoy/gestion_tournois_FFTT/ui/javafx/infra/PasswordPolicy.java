package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra;

public final class PasswordPolicy {
    private PasswordPolicy() {
    }

    // 8+ ; 1 majuscule ; 1 chiffre ; 1 ponctuation ; 1 spécial
    // Ponctuation : . , ; : ! ?
    // Spécial : tout le reste non-alphanum hors ponctuation (ex: @#$%&*()_+-=...)
    public static boolean isValid(String password) {
        if (password == null)
            return false;
        if (password.length() < 8)
            return false;

        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasPunct = false;
        boolean hasSpecial = false;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c))
                hasUpper = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            else if (isPunctuation(c))
                hasPunct = true;
            else if (!Character.isLetterOrDigit(c))
                hasSpecial = true;
        }

        return hasUpper && hasDigit && hasPunct && hasSpecial;
    }

    private static boolean isPunctuation(char c) {
        return c == '.' || c == ',' || c == ';' || c == ':' || c == '!' || c == '?';
    }

    public static String rulesText() {
        return "Mot de passe: 8+ caractères, 1 majuscule, 1 chiffre, 1 ponctuation (. , ; : ! ?), 1 caractère spécial (ex: @#$%).";
    }
}
