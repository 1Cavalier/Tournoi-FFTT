package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.draw;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolSlot;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.List;

/**
 * Interface Strategy pour les algorithmes de tirage des poules.
 *
 * Permet d'avoir plusieurs façons de constituer les poules
 * selon le contexte du tournoi, tout en gardant le même aggregate Poule.
 *
 * Implémentations prévues :
 * - SnakeDrawAlgorithm : méthode du serpent FFTT (par défaut)
 * - (futur) RandomDrawAlgorithm : tirage aléatoire pur
 * - (futur) ManualDrawAlgorithm : placement manuel par le JA
 *
 * L'algorithme reçoit une liste de participants triés par points décroissants
 * et retourne une liste de listes de PoolSlot (une liste = une poule).
 */
public interface DrawAlgorithm {

    /**
     * Répartit les participants en poules.
     *
     * @param rankedParticipants participants triés du meilleur (index 0) au moins
     *                           bon.
     *                           Le rang serpent est leur index + 1.
     * @param poolSize           taille de chaque poule (3 ou 4 actuellement)
     * @return liste de poules, chaque poule étant une liste de PoolSlot.
     *         L'ordre des slots dans chaque sous-liste reflète les positions 1, 2,
     *         3.
     */
    List<List<PoolSlot>> draw(List<Participant> rankedParticipants, int poolSize);

    /**
     * Identifiant lisible de l'algorithme (pour la persistence et les logs).
     */
    DrawAlgorithmType type();
}