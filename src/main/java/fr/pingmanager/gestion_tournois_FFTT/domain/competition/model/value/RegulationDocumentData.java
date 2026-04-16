package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.RankingPhase;

/**
 * Snapshot "data-only" du règlement.
 * - Métier : structure stable et testable
 * - Export/PDF : record simple
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
                String playingAreaSummary,
                String ballBrandAndType,
                String ballProvisionPolicy,

                // Dates clés
                LocalDateTime registrationDeadline,
                LocalDateTime checkInDeadline,
                LocalDateTime firstMatchesStart,

                // Fin prévisionnelle
                LocalTime expectedEndTime,

                // Tableaux (résumé)
                List<TableauLine> tableaux

) {
        public RegulationDocumentData {
                // Pro : évite NPE stupides si un jour ce record est construit ailleurs.
                Objects.requireNonNull(tournamentName, "tournamentName");
                Objects.requireNonNull(level, "level");
                Objects.requireNonNull(rankingPhase, "rankingPhase");
                Objects.requireNonNull(days, "days");
                Objects.requireNonNull(organizingClubName, "organizingClubName");
                Objects.requireNonNull(organizingClubNumber, "organizingClubNumber");
                Objects.requireNonNull(organizingClubCity, "organizingClubCity");
                Objects.requireNonNull(organizerEmail, "organizerEmail");
                Objects.requireNonNull(venueName, "venueName");
                Objects.requireNonNull(ballBrandAndType, "ballBrandAndType");
                Objects.requireNonNull(ballProvisionPolicy, "ballProvisionPolicy");
                Objects.requireNonNull(registrationDeadline, "registrationDeadline");
                Objects.requireNonNull(checkInDeadline, "checkInDeadline");
                Objects.requireNonNull(firstMatchesStart, "firstMatchesStart");
                Objects.requireNonNull(expectedEndTime, "expectedEndTime");
                Objects.requireNonNull(tableaux, "tableaux");
        }

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