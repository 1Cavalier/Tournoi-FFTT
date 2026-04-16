package fr.pingmanager.gestion_tournois_FFTT.infra.mail;

/**
 * Templates emails (sujets + contenus).
 */
public final class EmailTemplates {

    private EmailTemplates() {
    }

    public static final int EMAIL_VERIFICATION_TTL_MINUTES = 15;
    public static final int LOGIN_OTP_TTL_MINUTES = 10;

    public static String verificationSubject() {
        return "Vérification email — PingManager";
    }

    public static String verificationBody(String code) {
        return """
                Bonjour,

                Voici votre code de vérification d'email :

                %s

                Ce code est valable %d minutes.

                — PingManager
                """.formatted(code, EMAIL_VERIFICATION_TTL_MINUTES);
    }

    public static String loginOtpSubject() {
        return "Code de connexion — PingManager";
    }

    public static String loginOtpBody(String otp) {
        return """
                Bonjour,

                Voici votre code de connexion :

                %s

                Ce code est valable %d minutes.
                Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.

                — PingManager
                """.formatted(otp, LOGIN_OTP_TTL_MINUTES);
    }
}