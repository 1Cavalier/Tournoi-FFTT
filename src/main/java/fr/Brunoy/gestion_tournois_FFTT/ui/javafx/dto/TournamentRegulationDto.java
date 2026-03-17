package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto;

import java.util.List;

/**
 * DTO UI / persistence pour le bloc règlement d'un tournoi.
 */
public record TournamentRegulationDto(

        String tournamentId,

        String organizerContactName,
        String organizerEmail,
        String organizerPhone,

        String venueName,
        String venueStreet,
        String venueZip,
        String venueCity,

        Integer numberOfTables,

        String playingAreaPreset,
        String playingAreaInfoText,
        Integer playingAreaLengthMeters,
        Integer playingAreaWidthMeters,
        Boolean playingAreaCompliant,

        String ballBrandAndType,
        String ballProvisionPolicy,

        String registrationOpenTime,
        String registrationDeadline,
        String gymOpenTime,

        String requiredJudgeGrade,
        Integer recommendedJudgeCount,
        String recommendedRefereeGrade,
        Integer recommendedRefereeCount,

        List<TournamentOfficialAssignmentDto> assignedOfficials,

        String createdAt,
        String updatedAt) {
}