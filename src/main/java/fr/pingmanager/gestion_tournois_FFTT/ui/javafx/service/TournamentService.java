package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.TournamentStatus;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.TableauRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.TournamentRegulationRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.TournamentRepository;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentOfficialAssignmentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;

public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentRegulationRepository tournamentRegulationRepository;
    private final TableauRepository tableauRepository;

    public TournamentService(
            TournamentRepository tournamentRepository,
            TournamentRegulationRepository tournamentRegulationRepository,
            TableauRepository tableauRepository) {
        this.tournamentRepository = Objects.requireNonNull(tournamentRepository);
        this.tournamentRegulationRepository = Objects.requireNonNull(tournamentRegulationRepository);
        this.tableauRepository = Objects.requireNonNull(tableauRepository);
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
        if (cmd.address1() == null || cmd.address1().isBlank()) {
            throw new IllegalArgumentException("L'adresse 1 du tournoi est obligatoire.");
        }

        String tournamentId = UUID.randomUUID().toString();
        String now = LocalDateTime.now().toString();

        TournamentDto tournament = new TournamentDto(
                tournamentId,
                required(cmd.clubId()),
                required(cmd.organizerId()),
                required(cmd.name()),
                required(cmd.address1()),
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

                null,
                null,
                null,
                null,
                List.<TournamentOfficialAssignmentDto>of(),

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

    public TableauDto createTableau(TableauDto tableau) {
        Objects.requireNonNull(tableau);

        String now = LocalDateTime.now().toString();

        TableauDto toInsert = new TableauDto(
                tableau.id() == null || tableau.id().isBlank() ? UUID.randomUUID().toString() : tableau.id().trim(),
                required(tableau.tournamentId()),
                optional(tableau.code()),
                optional(tableau.designation()),
                optional(tableau.date()),
                optional(tableau.genderPolicy()),
                optional(tableau.agePolicyType()),
                optional(tableau.ageMinCategory()),
                optional(tableau.ageMaxCategory()),
                safeStringList(tableau.allowedAgeCategories()),
                optional(tableau.pointsRuleType()),
                tableau.minPoints(),
                tableau.maxPoints(),
                tableau.maxPlayers(),
                tableau.waitlistCapacity(),
                optional(tableau.checkInEnd()),
                optional(tableau.startTime()),
                tableau.prepaidFee(),
                tableau.onSiteFee(),
                safePrizeTiers(tableau.prizeTiers()),
                tableau.drawAlgorithmType() != null ? tableau.drawAlgorithmType() : "SNAKE",
                tableau.classificationMode() != null ? tableau.classificationMode() : "NONE",
                now,
                now);

        tableauRepository.insert(toInsert);
        return toInsert;
    }

    public TableauDto updateTableau(TableauDto tableau) {
        Objects.requireNonNull(tableau);

        TableauDto existing = tableauRepository.findById(required(tableau.id()))
                .orElseThrow(() -> new IllegalArgumentException("Tableau introuvable : " + tableau.id()));

        TableauDto updated = new TableauDto(
                required(existing.id()),
                required(tableau.tournamentId()),
                optional(tableau.code()),
                optional(tableau.designation()),
                optional(tableau.date()),
                optional(tableau.genderPolicy()),
                optional(tableau.agePolicyType()),
                optional(tableau.ageMinCategory()),
                optional(tableau.ageMaxCategory()),
                safeStringList(tableau.allowedAgeCategories()),
                optional(tableau.pointsRuleType()),
                tableau.minPoints(),
                tableau.maxPoints(),
                tableau.maxPlayers(),
                tableau.waitlistCapacity(),
                optional(tableau.checkInEnd()),
                optional(tableau.startTime()),
                tableau.prepaidFee(),
                tableau.onSiteFee(),
                safePrizeTiers(tableau.prizeTiers()),
                tableau.drawAlgorithmType() != null ? tableau.drawAlgorithmType() : "SNAKE",
                tableau.classificationMode() != null ? tableau.classificationMode() : "NONE",
                required(existing.createdAt()),
                LocalDateTime.now().toString());

        tableauRepository.update(updated);
        return updated;
    }

    public Optional<TableauDto> findTableauById(String id) {
        return tableauRepository.findById(id);
    }

    public List<TableauDto> findTableauxByTournamentId(String tournamentId) {
        return tableauRepository.findByTournamentId(required(tournamentId));
    }

    public void deleteTableau(String id) {
        tableauRepository.delete(required(id));
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

    private List<String> safeStringList(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private List<fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.PrizeTierDto> safePrizeTiers(
            List<fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.PrizeTierDto> tiers) {
        return tiers == null ? List.of() : List.copyOf(tiers);
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