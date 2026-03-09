package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.ClubAccessDto;

import java.util.List;

public interface ClubAccessRepository {

    List<ClubAccessDto> findByClubId(String clubId);

    boolean existsByClubIdAndEmail(String clubId, String email);

    void insert(ClubAccessDto access);

    void deleteById(String id);
}