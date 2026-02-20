package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail;

public final class EmailTemplates {

    private EmailTemplates() {
    }

    public static String verificationSubject() {
        return "Validation de votre compte organisateur";
    }

    public static String verificationBody(String code) {
        return """
                Bonjour,

                Voici votre code de validation : %s

                Ce code expire dans 15 minutes.

                Cordialement,
                Tournoi FFTT
                """.formatted(code);
    }
}