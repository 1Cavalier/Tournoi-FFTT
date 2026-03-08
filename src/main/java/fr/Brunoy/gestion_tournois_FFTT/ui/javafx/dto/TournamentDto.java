package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto;

public record TournamentDto(
        String id,
        String organizerId,
        String name,
        String level,
        String phase,
        String startDate,
        String endDate,
        String status,
        Integer maxTableauxPerDay,
        String femaleExtraRule,
        String femaleExtraCode) {
}