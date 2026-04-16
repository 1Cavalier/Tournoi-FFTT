package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import java.util.List;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.ClubAccessDto;

public interface ClubAccessRepository {

    List<ClubAccessDto> findByClubId(String clubId);

    boolean existsByClubIdAndEmail(String clubId, String email);

    void insert(ClubAccessDto access);

    void deleteById(String id);
}