package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.scene.control.Label;

public class StatusBadge extends Label {

    public StatusBadge(TournamentStatus status) {
        super(label(status));
        setStyle(AppTheme.badgeStyle(color(status)));
    }

    private static String label(TournamentStatus s) {
        if (s == null)
            return "DRAFT";
        return switch (s) {
            case DRAFT -> "DRAFT";
            case OPEN -> "OPEN";
            case RUNNING -> "RUNNING";
            case FINISHED -> "FINISHED";
            case CANCELLED -> "CANCELLED";
        };
    }

    private static String color(TournamentStatus s) {
        if (s == null)
            return "#64748B"; // gris
        return switch (s) {
            case DRAFT -> "#64748B"; // gris
            case OPEN -> AppTheme.COLOR_PRIMARY; // bleu
            case RUNNING -> "#16A34A"; // vert
            case FINISHED -> "#0F172A"; // sombre
            case CANCELLED -> "#B91C1C"; // rouge
        };
    }
}