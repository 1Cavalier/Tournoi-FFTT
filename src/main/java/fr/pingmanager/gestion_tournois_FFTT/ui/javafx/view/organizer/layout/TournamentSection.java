package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.layout;

/**
 * Sections de navigation pour un tournoi.
 * Utilisé par la sidebar et l'AppRouter pour déterminer
 * quelle vue afficher dans le contenu principal.
 */
public enum TournamentSection {

    /** Page d'accueil du tableau de bord (aucun tournoi sélectionné). */
    HOME(null, false),

    /** Informations générales du tournoi (dates, ville, niveau…). */
    GENERAL("Général", true),

    /** Règlement officiel du tournoi (lieu, tables, balles, officiels…). */
    REGLEMENT("Règlement", true),

    /** Gestion des tableaux et de l'algorithme de tirage. */
    TABLEAUX("Tableaux", true),

    /** Documents générés (affiches, règlement PDF…). */
    DOCUMENTS("Documents", true),

    /** Inscriptions des joueurs — à venir. */
    INSCRIPTIONS("Inscriptions", false),

    /** Interface de gestion en direct du tournoi — à venir. */
    DIRECT("Direct", false);

    private final String label;
    /** true = section implémentée et cliquable, false = à venir (grisée). */
    private final boolean available;

    TournamentSection(String label, boolean available) {
        this.label = label;
        this.available = available;
    }

    public String label() {
        return label;
    }

    public boolean available() {
        return available;
    }
}