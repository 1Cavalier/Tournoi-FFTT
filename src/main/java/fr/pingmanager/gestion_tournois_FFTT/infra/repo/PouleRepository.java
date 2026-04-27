package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.Poule;

import java.util.List;
import java.util.Optional;

public interface PouleRepository {

    /** Sauvegarde une poule et tous ses slots et matchs. */
    void save(Poule poule, String tournamentId);

    /** Met à jour les matchs d'une poule (scores, statuts). */
    void update(Poule poule);

    /** Retourne toutes les poules d'un tableau. */
    List<Poule> findByTableau(String tournamentId, String tableauCode);

    Optional<Poule> findById(String pouleId);

    void deleteByTableau(String tournamentId, String tableauCode);
}