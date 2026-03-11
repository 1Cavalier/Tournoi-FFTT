package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.service;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentStatus;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.TournamentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class TournamentService {

    private final TournamentRepository tournamentRepository;

    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = Objects.requireNonNull(tournamentRepository);
    }

    public TournamentDto createDraft(CreateTournamentDraftCommand cmd) {
        Objects.requireNonNull(cmd);

        if (cmd.endDate().isBefore(cmd.startDate())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être avant la date de début.");
        }

        String now = LocalDateTime.now().toString();

        TournamentDto row = new TournamentDto(
                UUID.randomUUID().toString(),
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

        return tournamentRepository.insert(row);
    }

    public TournamentDto updateGeneral(TournamentDto existing) {
        Objects.requireNonNull(existing);

        LocalDate startDate = LocalDate.parse(existing.startDate());
        LocalDate endDate = LocalDate.parse(existing.endDate());

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
                existing.startDate(),
                existing.endDate(),
                optional(existing.homologationNumber()),
                required(existing.status()),
                existing.createdAt(),
                LocalDateTime.now().toString());

        tournamentRepository.update(updated);
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