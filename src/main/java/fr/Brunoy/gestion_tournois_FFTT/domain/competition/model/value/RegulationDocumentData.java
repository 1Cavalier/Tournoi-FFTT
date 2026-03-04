package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.RankingPhase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Snapshot "data-only" du règlement.
 * - Métier : structure stable et testable
 */
public record RegulationDocumentData(

        // Tournoi (identité)
        String tournamentName,
        TournamentLevel level,
        RankingPhase rankingPhase,
        Set<LocalDate> days,

        // Club organisateur (utile pour l'en-tête)
        String organizingClubName,
        String organizingClubNumber,
        String organizingClubCity,

        // Homologation
        String homologationNumber,

        // Contact organisateur
        String organizerContactName,
        String organizerEmail,
        String organizerPhone,

        // Salle
        String venueName,
        String venueStreet,
        String venueZip,
        String venueCity,

        // Matériel / aires / balles
        int numberOfTables,
        String playingAreaSummary, // ex: "NATIONAL_STANDARD - 14m x 7m" ou "CUSTOM ..."
        String ballBrandAndType,
        String ballProvisionPolicy, // enum -> String pour stabilité export

        // Dates clés
        LocalDateTime registrationDeadline,
        LocalDateTime checkInDeadline,
        LocalDateTime firstMatchesStart,

        // Fin prévisionnelle
        LocalTime expectedEndTime,

        // Tableaux (résumé)
        List<TableauLine> tableaux

) {
    public record TableauLine(
            String code,
            String designation,
            LocalDate date,
            LocalTime checkInEnd,
            LocalTime startTime,
            String genderPolicy,
            String pointsRule,
            Integer minPoints,
            Integer maxPoints,
            int maxPlayers,
            int waitlistCapacity,
            String feeSummary,
            String prizesSummary) {
    }
}