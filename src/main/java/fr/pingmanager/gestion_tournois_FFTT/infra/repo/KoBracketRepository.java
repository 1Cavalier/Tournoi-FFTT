package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.KoBracket;

import java.util.Optional;

public interface KoBracketRepository {

    /** Sauvegarde un nouveau KoBracket avec tous ses matchs. */
    void save(KoBracket bracket, String tournamentId);

    /** Met à jour les matchs du bracket (scores, statuts, joueurs propagés). */
    void update(KoBracket bracket);

    Optional<KoBracket> findByTableau(String tournamentId, String tableauCode);

    void deleteByTableau(String tournamentId, String tableauCode);
}