package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto;

public record TournamentOfficialAssignmentDto(

        String playerLicenseNumber,
        String firstName,
        String lastName,
        String clubName,

        String officialRoleType,
        String judgeGrade,
        String refereeGrade,

        Boolean designatedMainJudge,
        Boolean assistantJudge,
        Boolean activeForFinalsOnly) {
}