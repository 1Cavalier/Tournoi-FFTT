package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.mail;

import java.security.SecureRandom;

/**
 * Générateur de codes courts (6 chiffres).
 * Utilise SecureRandom (adapté à des codes de vérification).
 */
public final class VerificationCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private VerificationCodeGenerator() {
    }

    public static String code6() {
        int value = RANDOM.nextInt(1_000_000); // 0..999999
        return String.format("%06d", value);
    }
}