package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.ClubDto;

import java.util.List;
import java.util.Optional;

/**
 * Contrat d'accès aux clubs.
 */
public interface ClubRepository {

    String createClub(String clubNumberOrNull, String clubNameOrNull);

    Optional<ClubDto> findById(String clubId);

    Optional<ClubDto> findByOrganizerId(String organizerId);

    List<ClubDto> search(String query, int limit);

    void updateClubProfile(ClubDto club);
}