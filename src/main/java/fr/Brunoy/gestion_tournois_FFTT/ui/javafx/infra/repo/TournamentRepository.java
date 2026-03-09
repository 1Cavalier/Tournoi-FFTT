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

    List<TournamentDto> findActiveForClub(String clubId);

    List<TournamentDto> findDraftForClub(String clubId);

    String createDraftTournament(
            String clubId,
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