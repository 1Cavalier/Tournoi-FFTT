package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

public record TableauRow(
        String id,
        String tournamentId,
        String code,
        String label,
        int priceCents,
        int capacity) {
}
