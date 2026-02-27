package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.ClubRow;

import java.util.List;
import java.util.Optional;

/**
 * Contrat d'accès aux clubs (indépendant du stockage).
 */
public interface ClubRepository {

    String createClub(String clubNumberOrNull, String clubNameOrNull);

    Optional<ClubRow> findById(String clubId);

    Optional<ClubRow> findByOrganizerId(String organizerId);

    List<ClubRow> search(String query, int limit);

    void updateClubProfile(ClubRow club);
}