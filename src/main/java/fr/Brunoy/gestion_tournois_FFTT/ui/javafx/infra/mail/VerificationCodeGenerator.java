package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.mail;

import java.security.SecureRandom;

/**
 * Générateur de codes numériques courts pour vérification (OTP).
 */
public final class VerificationCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private VerificationCodeGenerator() {
    }

    /**
     * Génère un code à 6 chiffres.
     */
    public static String code6() {
        return generateDigits(6);
    }

    /**
     * Génère un code numérique de longueur donnée.
     */
    public static String generateDigits(int length) {

        if (length <= 0 || length > 9) {
            throw new IllegalArgumentException("Longueur de code invalide");
        }

        int bound = (int) Math.pow(10, length);

        int value = RANDOM.nextInt(bound);

        return String.format("%0" + length + "d", value);
    }
}