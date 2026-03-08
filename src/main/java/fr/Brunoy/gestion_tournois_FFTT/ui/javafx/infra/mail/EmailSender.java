package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail;

/**
 * Abstraction d'envoi d'email.
 * Permet de remplacer l'implémentation console (dev) par SMTP/API (prod).
 */
public interface EmailSender {
    void send(String to, String subject, String body);
}