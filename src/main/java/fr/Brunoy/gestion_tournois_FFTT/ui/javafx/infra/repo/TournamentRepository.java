package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository {

    TournamentDto insert(TournamentDto tournament);

    Optional<TournamentDto> findById(String id);

    List<TournamentDto> findByClubId(String clubId);

    List<TournamentDto> findDraftForClub(String clubId);

    List<TournamentDto> findActiveForClub(String clubId);

    void update(TournamentDto tournament);

    void delete(String id);
}