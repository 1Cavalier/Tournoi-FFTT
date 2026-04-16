package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import java.util.Optional;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;

public interface TournamentRegulationRepository {

    TournamentRegulationDto insert(TournamentRegulationDto regulation);

    Optional<TournamentRegulationDto> findByTournamentId(String tournamentId);

    void update(TournamentRegulationDto regulation);

    void deleteByTournamentId(String tournamentId);
}