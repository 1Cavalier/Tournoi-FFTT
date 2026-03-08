package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrat d'accès aux tournois.
 */
public interface TournamentRepository {

    Optional<String> findCurrentTournamentId();

    Optional<TournamentDto> findById(String id);

    List<TournamentDto> findActiveForOrganizer(String organizerId);

    List<TournamentDto> findDraftForOrganizer(String organizerId);

    String createDraftTournament(
            String organizerId,
            String name,
            String level,
            String rankingPhase,
            LocalDate startDate,
            LocalDate endDate,
            int maxPerDay,
            String femaleRule,
            String femaleCode);
}