package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model;

public record TableauRow(
        String id,
        String tournamentId,
        String code,
        String label,
        int priceCents,
        int capacity) {
}
