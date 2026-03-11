package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentRow;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository {

    TournamentRow insert(TournamentRow tournament);

    Optional<TournamentRow> findById(String id);

    List<TournamentRow> findByClubId(String clubId);

    List<TournamentRow> findDraftForClub(String clubId);

    List<TournamentRow> findActiveForClub(String clubId);

    void update(TournamentRow tournament);

    void delete(String id);
}