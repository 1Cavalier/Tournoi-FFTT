package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;

import java.util.Optional;

public interface TournamentRegulationRepository {

    TournamentRegulationDto insert(TournamentRegulationDto regulation);

    Optional<TournamentRegulationDto> findByTournamentId(String tournamentId);

    void update(TournamentRegulationDto regulation);

    void deleteByTournamentId(String tournamentId);
}