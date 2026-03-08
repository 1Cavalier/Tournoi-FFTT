package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto;

public record TournamentCardDto(
        String id,
        String organizerId,
        String name,
        String clubCity,
        String clubDepartmentCode,
        String level,
        String phase,
        String startDate,
        String endDate,
        String status,
        String homologationNumber,
        Integer numberOfTables,
        boolean hasJudgeReferee,
        boolean hasReferee,
        Integer maxTableauxPerDay,
        String femaleRuleLabel,
        Integer tableauCount,
        String selectionByLabel,
        String totalRewardLabel,
        boolean canManageRegistrations,
        boolean canPublish) {
}