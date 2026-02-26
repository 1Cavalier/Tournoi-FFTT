package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail;

public final class EmailTemplates {

    private EmailTemplates() {
    }

    public static String verificationSubject() {
        return "Vérification email — Tournoi FFTT";
    }

    public static String verificationBody(String code) {
        return """
                Bonjour,

                Voici votre code de vérification d'email :
                %s

                Ce code est valable 15 minutes.

                — Tournoi FFTT
                """.formatted(code);
    }

    public static String loginOtpSubject() {
        return "Code de connexion — Tournoi FFTT";
    }

    public static String loginOtpBody(String otp) {
        return """
                Bonjour,

                Voici votre code de connexion :
                %s

                Ce code est valable 10 minutes.
                Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.

                — Tournoi FFTT
                """.formatted(otp);
    }
}