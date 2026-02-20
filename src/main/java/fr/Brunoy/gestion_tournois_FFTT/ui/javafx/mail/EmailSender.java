package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail;

public class EmailSender {

    public void send(String to, String subject, String body) {
        System.out.println("====== EMAIL ======");
        System.out.println("TO: " + to);
        System.out.println("SUBJECT: " + subject);
        System.out.println(body);
        System.out.println("===================");
    }
}