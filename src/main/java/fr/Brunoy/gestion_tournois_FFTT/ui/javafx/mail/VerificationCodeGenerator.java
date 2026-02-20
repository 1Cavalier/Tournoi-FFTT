package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail;

import java.security.SecureRandom;

public final class VerificationCodeGenerator {

    private static final SecureRandom random = new SecureRandom();

    private VerificationCodeGenerator() {
    }

    public static String code6() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}