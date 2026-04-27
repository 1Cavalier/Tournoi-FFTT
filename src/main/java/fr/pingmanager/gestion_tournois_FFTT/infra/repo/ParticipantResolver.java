package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

/**
 * Interface pour résoudre un Participant depuis son identifiant stable.
 *
 * Utilisée par les repositories de la phase de poules pour reconstruire
 * les entités domaine depuis la base de données.
 *
 * L'implémentation va chercher dans la table player (FFTT) ou guest (invités).
 */
public interface ParticipantResolver {

    /**
     * Retourne le Participant correspondant à l'identifiant donné.
     * Lève une RuntimeException si introuvable.
     *
     * @param participantId identifiant stable (numéro de licence FFTT, ID invité,
     *                      etc.)
     */
    Participant resolve(String participantId);
}