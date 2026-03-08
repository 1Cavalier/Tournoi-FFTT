package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail;

/**
 * Implémentation dev : écrit les emails dans la console.
 */
public class ConsoleEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        String safeTo = to == null ? "" : to.trim();
        String safeSubject = subject == null ? "" : subject.trim();
        String safeBody = body == null ? "" : body;

        System.out.println("====== EMAIL ======");
        System.out.println("TO: " + safeTo);
        System.out.println("SUBJECT: " + safeSubject);
        System.out.println(safeBody);
        System.out.println("===================");
    }
}