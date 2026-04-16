package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.TournamentStatus;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.scene.control.Label;

public final class StatusBadge extends Label {

    public StatusBadge(String status) {
        this(parse(status));
    }

    public StatusBadge(TournamentStatus status) {
        super(label(normalize(status)));
        setStyle(AppTheme.badgeStyle(color(normalize(status))));
    }

    private static TournamentStatus parse(String status) {
        if (status == null || status.isBlank()) {
            return TournamentStatus.DRAFT;
        }

        try {
            return TournamentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TournamentStatus.DRAFT;
        }
    }

    private static TournamentStatus normalize(TournamentStatus status) {
        return status == null ? TournamentStatus.DRAFT : status;
    }

    private static String label(TournamentStatus status) {
        return status.name();
    }

    private static String color(TournamentStatus status) {
        return switch (status) {
            case DRAFT -> "#64748B";
            case OPEN -> AppTheme.COLOR_PRIMARY;
            case RUNNING -> "#16A34A";
            case FINISHED -> "#0F172A";
            case CANCELLED -> "#B91C1C";
        };
    }
}