package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail;

/**
 * Templates emails (sujets + contenus).
 * Garder ici uniquement du texte formaté, sans logique métier.
 */
public final class EmailTemplates {

    private EmailTemplates() {
    }

    public static final int EMAIL_VERIFICATION_TTL_MINUTES = 15;
    public static final int LOGIN_OTP_TTL_MINUTES = 10;

    public static String verificationSubject() {
        return "Vérification email — Tournoi FFTT";
    }

    public static String verificationBody(String code) {
        return """
                Bonjour,

                Voici votre code de vérification d'email :
                %s

                Ce code est valable %d minutes.

                — Tournoi FFTT
                """.formatted(code, EMAIL_VERIFICATION_TTL_MINUTES);
    }

    public static String loginOtpSubject() {
        return "Code de connexion — Tournoi FFTT";
    }

    public static String loginOtpBody(String otp) {
        return """
                Bonjour,

                Voici votre code de connexion :
                %s

                Ce code est valable %d minutes.
                Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.

                — Tournoi FFTT
                """.formatted(otp, LOGIN_OTP_TTL_MINUTES);
    }
}