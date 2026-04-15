package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.OfficialSelectablePlayerDto;

import java.util.List;

/**
 * Contrat d'accès aux joueurs officiels (JA et arbitres).
 * Utilisé pour la recherche et la sélection dans
 * EditTournamentRegulationDialog.
 */
public interface PlayerRepository {

    /**
     * Recherche des juges-arbitres par nom, prénom ou numéro de licence.
     * Retourne uniquement les joueurs ayant une qualification JUGE_ARBITRE.
     *
     * @param query texte de recherche (nom, prénom ou licence)
     * @param limit nombre maximum de résultats
     */
    List<OfficialSelectablePlayerDto> searchJudgeReferees(String query, int limit);

    /**
     * Recherche des arbitres par nom, prénom ou numéro de licence.
     * Retourne uniquement les joueurs ayant une qualification ARBITRE.
     *
     * @param query texte de recherche (nom, prénom ou licence)
     * @param limit nombre maximum de résultats
     */
    List<OfficialSelectablePlayerDto> searchReferees(String query, int limit);
}