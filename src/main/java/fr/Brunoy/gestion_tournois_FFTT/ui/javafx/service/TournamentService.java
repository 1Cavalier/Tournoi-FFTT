package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.service;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentStatus;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentOfficialAssignmentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.TournamentRegulationRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.TournamentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentRegulationRepository tournamentRegulationRepository;

    public TournamentService(
            TournamentRepository tournamentRepository,
            TournamentRegulationRepository tournamentRegulationRepository) {
        this.tournamentRepository = Objects.requireNonNull(tournamentRepository);
        this.tournamentRegulationRepository = Objects.requireNonNull(tournamentRegulationRepository);
    }

    public TournamentDto createDraft(CreateTournamentDraftCommand cmd) {
        Objects.requireNonNull(cmd);

        if (cmd.startDate() == null) {
            throw new IllegalArgumentException("La date de début est obligatoire.");
        }
        if (cmd.endDate() == null) {
            throw new IllegalArgumentException("La date de fin est obligatoire.");
        }
        if (cmd.endDate().isBefore(cmd.startDate())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être avant la date de début.");
        }
        if (cmd.level() == null) {
            throw new IllegalArgumentException("Le niveau du tournoi est obligatoire.");
        }
        if (cmd.phase() == null) {
            throw new IllegalArgumentException("La phase est obligatoire.");
        }

        String tournamentId = UUID.randomUUID().toString();
        String now = LocalDateTime.now().toString();

        TournamentDto tournament = new TournamentDto(
                tournamentId,
                required(cmd.clubId()),
                required(cmd.organizerId()),
                required(cmd.name()),
                optional(cmd.address1()),
                optional(cmd.address2()),
                required(cmd.city()),
                required(cmd.department()),
                cmd.level().name(),
                cmd.phase().name(),
                cmd.startDate().toString(),
                cmd.endDate().toString(),
                null,
                TournamentStatus.DRAFT.name(),
                now,
                now);

        TournamentDto insertedTournament = tournamentRepository.insert(tournament);

        TournamentRegulationDto emptyRegulation = new TournamentRegulationDto(
                tournamentId,

                null,
                null,
                null,

                null,
                null,
                null,
                null,

                null,

                null,
                null,
                null,
                null,
                null,

                null,
                null,

                null,
                null,
                null,

                null, // requiredJudgeGrade
                null, // recommendedJudgeCount
                null, // recommendedRefereeGrade
                null, // recommendedRefereeCount
                List.<TournamentOfficialAssignmentDto>of(), // assignedOfficials

                now,
                now);

        tournamentRegulationRepository.insert(emptyRegulation);

        return insertedTournament;
    }

    public TournamentDto updateGeneral(TournamentDto existing) {
        Objects.requireNonNull(existing);

        LocalDate startDate = parseRequiredDate(existing.startDate(), "La date de début est obligatoire.");
        LocalDate endDate = parseRequiredDate(existing.endDate(), "La date de fin est obligatoire.");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La date de fin ne peut pas être avant la date de début.");
        }

        TournamentDto updated = new TournamentDto(
                required(existing.id()),
                required(existing.clubId()),
                required(existing.organizerId()),
                required(existing.name()),
                optional(existing.address1()),
                optional(existing.address2()),
                required(existing.city()),
                required(existing.department()),
                required(existing.level()),
                required(existing.phase()),
                startDate.toString(),
                endDate.toString(),
                optional(existing.homologationNumber()),
                required(existing.status()),
                required(existing.createdAt()),
                LocalDateTime.now().toString());

        tournamentRepository.update(updated);
        return updated;
    }

    public TournamentRegulationDto getRegulation(String tournamentId) {
        return tournamentRegulationRepository.findByTournamentId(required(tournamentId))
                .orElseThrow(
                        () -> new IllegalArgumentException("Règlement introuvable pour le tournoi : " + tournamentId));
    }

    public TournamentRegulationDto updateRegulation(TournamentRegulationDto existing) {
        Objects.requireNonNull(existing);

        TournamentRegulationDto updated = new TournamentRegulationDto(
                required(existing.tournamentId()),

                optional(existing.organizerContactName()),
                optional(existing.organizerEmail()),
                optional(existing.organizerPhone()),

                optional(existing.venueName()),
                optional(existing.venueStreet()),
                optional(existing.venueZip()),
                optional(existing.venueCity()),

                existing.numberOfTables(),

                optional(existing.playingAreaPreset()),
                optional(existing.playingAreaInfoText()),
                existing.playingAreaLengthMeters(),
                existing.playingAreaWidthMeters(),
                existing.playingAreaCompliant(),

                optional(existing.ballBrandAndType()),
                optional(existing.ballProvisionPolicy()),

                optional(existing.registrationOpenTime()),
                optional(existing.registrationDeadline()),
                optional(existing.gymOpenTime()),

                optional(existing.requiredJudgeGrade()),
                existing.recommendedJudgeCount(),
                optional(existing.recommendedRefereeGrade()),
                existing.recommendedRefereeCount(),
                safeOfficialAssignments(existing.assignedOfficials()),

                required(existing.createdAt()),
                LocalDateTime.now().toString());

        tournamentRegulationRepository.update(updated);
        return updated;
    }

    public Optional<TournamentDto> findById(String id) {
        return tournamentRepository.findById(id);
    }

    public List<TournamentDto> findByClubId(String clubId) {
        return tournamentRepository.findByClubId(clubId);
    }

    public List<TournamentDto> findDraftForClub(String clubId) {
        return tournamentRepository.findDraftForClub(clubId);
    }

    public List<TournamentDto> findActiveForClub(String clubId) {
        return tournamentRepository.findActiveForClub(clubId);
    }

    public void delete(String id) {
        tournamentRepository.delete(id);
    }

    private List<TournamentOfficialAssignmentDto> safeOfficialAssignments(
            List<TournamentOfficialAssignmentDto> assignments) {
        return assignments == null ? List.of() : List.copyOf(assignments);
    }

    private LocalDate parseRequiredDate(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return LocalDate.parse(value.trim());
    }

    private String required(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Champ obligatoire manquant.");
        }
        return value.trim();
    }

    private String optional(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }
}