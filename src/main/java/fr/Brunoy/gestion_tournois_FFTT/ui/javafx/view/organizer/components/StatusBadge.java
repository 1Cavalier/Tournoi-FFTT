package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import javafx.scene.control.Label;

/**
 * Petit badge visuel pour afficher le statut d'un tournoi.
 */
public class StatusBadge extends Label {

    public StatusBadge(TournamentStatus status) {
        super(statusLabel(status));

        setStyle("""
                -fx-padding: 4 10 4 10;
                -fx-border-color: black;
                -fx-border-width: 1;
                -fx-background-color: white;
                -fx-font-weight: bold;
                """);
    }

    private static String statusLabel(TournamentStatus s) {
        if (s == null) return "DRAFT";
        return s.name();
    }
}