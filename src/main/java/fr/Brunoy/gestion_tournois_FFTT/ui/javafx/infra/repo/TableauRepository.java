package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;

import java.util.List;

/**
 * Contrat d'accès aux tableaux d'un tournoi.
 */
public interface TableauRepository {

    void insertMany(String tournamentId, List<Tableau> tableaux);

    List<TableauDto> findByTournamentId(String tournamentId);
}