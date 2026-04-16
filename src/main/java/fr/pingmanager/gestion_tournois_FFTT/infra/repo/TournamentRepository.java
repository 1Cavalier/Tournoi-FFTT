package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import java.util.List;
import java.util.Optional;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;

public interface TournamentRepository {

    TournamentDto insert(TournamentDto tournament);

    Optional<TournamentDto> findById(String id);

    List<TournamentDto> findByClubId(String clubId);

    List<TournamentDto> findDraftForClub(String clubId);

    List<TournamentDto> findActiveForClub(String clubId);

    void update(TournamentDto tournament);

    void delete(String id);
}