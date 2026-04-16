package fr.pingmanager.gestion_tournois_FFTT.infra.mail;

/**
 * Abstraction d'envoi d'email.
 * Permet de remplacer l'implémentation console (dev) par SMTP/API (prod).
 */
public interface EmailSender {

    void send(String to, String subject, String body);
}