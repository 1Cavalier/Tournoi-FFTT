package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.draw;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolSlot;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.FfttParticipant;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.*;

/**
 * Implémentation de la méthode du serpent FFTT pour la constitution des poules.
 *
 * <h2>Principe (Manuel JA, section I-6.1)</h2>
 * <p>
 * Les joueurs sont classés 1 à N par points décroissants.
 * Ils sont répartis en P poules selon un parcours en zigzag :
 * </p>
 * 
 * <pre>
 *   Aller  → : poule 1, 2, 3, ..., P
 *   Retour ← : poule P, P-1, ..., 1
 *   Aller  → : poule 1, 2, 3, ..., P
 *   ...
 * </pre>
 *
 * <p>
 * Exemple : 9 joueurs en 3 poules de 3 (joueurs triés 1..9)
 * </p>
 * 
 * <pre>
 *   Tour aller  : J1→P1, J2→P2, J3→P3
 *   Tour retour : J4→P3, J5→P2, J6→P1
 *   Tour aller  : J7→P1, J8→P2, J9→P3
 *
 *   P1 = {J1(pos1), J6(pos2), J7(pos3)}
 *   P2 = {J2(pos1), J5(pos2), J8(pos3)}
 *   P3 = {J3(pos1), J4(pos2), J9(pos3)}
 * </pre>
 *
 * <h2>Contrainte de club (article I.302 des Règlements Sportifs)</h2>
 * <p>
 * On évite que deux joueurs du même club soient dans la même poule.
 * Si c'est inévitable (plus de joueurs d'un club que de poules), on les place
 * de façon à se rencontrer le plus tôt possible.
 * En cas de conflit, c'est toujours le moins bien classé qui est déplacé,
 * en respectant l'ordre du serpent.
 * </p>
 */
public final class SnakeDrawAlgorithm implements DrawAlgorithm {

    @Override
    public DrawAlgorithmType type() {
        return DrawAlgorithmType.SNAKE;
    }

    @Override
    public List<List<PoolSlot>> draw(List<Participant> rankedParticipants, int poolSize) {
        Objects.requireNonNull(rankedParticipants, "rankedParticipants");
        if (rankedParticipants.size() < poolSize) {
            throw new BusinessException(ErrorCode.DRAW_NOT_ENOUGH_PLAYERS);
        }

        int nbPlayers = rankedParticipants.size();
        int nbPools = nbPlayers / poolSize;
        // Les joueurs restants (si nbPlayers % poolSize != 0) vont dans des poules de 4
        // Ce cas est géré en amont par le service qui appelle le draw

        // Initialiser les poules (listes mutables)
        List<List<Participant>> pools = new ArrayList<>();
        for (int i = 0; i < nbPools; i++) {
            pools.add(new ArrayList<>());
        }

        // ---- Placement initial en serpent ----
        boolean goingRight = true;
        int poolIndex = 0;
        for (Participant p : rankedParticipants) {
            pools.get(poolIndex).add(p);

            if (goingRight) {
                poolIndex++;
                if (poolIndex == nbPools) {
                    poolIndex = nbPools - 1;
                    goingRight = false;
                }
            } else {
                poolIndex--;
                if (poolIndex < 0) {
                    poolIndex = 0;
                    goingRight = true;
                }
            }
        }

        // ---- Contrainte de club ----
        applyClubConstraint(pools, rankedParticipants);

        // ---- Convertir en PoolSlot ----
        List<List<PoolSlot>> result = new ArrayList<>();
        for (List<Participant> pool : pools) {
            List<PoolSlot> slots = new ArrayList<>();
            for (int pos = 0; pos < pool.size(); pos++) {
                Participant p = pool.get(pos);
                int seedRank = rankedParticipants.indexOf(p) + 1;
                int posInPool = pos + 1;
                slots.add(new PoolSlot(seedRank, posInPool, p));
            }
            result.add(Collections.unmodifiableList(slots));
        }
        return Collections.unmodifiableList(result);
    }

    // -------------------------------------------------------------------------
    // CONTRAINTE DE CLUB
    // -------------------------------------------------------------------------

    /**
     * Applique la contrainte de club en déplaçant les joueurs en conflit.
     *
     * Principe FFTT : le moins bien classé (rang serpent le plus élevé) est déplacé
     * vers la prochaine poule disponible ne contenant pas de joueur de son club.
     *
     * On effectue plusieurs passes jusqu'à ce qu'aucun conflit ne reste
     * ou que le problème soit insoluble (plus joueurs d'un club que de poules).
     */
    private void applyClubConstraint(List<List<Participant>> pools,
            List<Participant> rankedParticipants) {
        boolean changed = true;
        int maxIterations = pools.size() * rankedParticipants.size(); // sécurité boucle infinie

        while (changed && maxIterations-- > 0) {
            changed = false;

            for (int i = 0; i < pools.size(); i++) {
                List<Participant> pool = pools.get(i);

                // Vérifier si deux joueurs de la même association sont dans cette poule
                List<Participant> conflict = findClubConflict(pool);
                if (conflict == null)
                    continue;

                // conflict.get(0) = mieux classé (à garder)
                // conflict.get(1) = moins bien classé (à déplacer)
                Participant toMove = leastRanked(conflict, rankedParticipants);

                // Chercher une poule cible sans joueur de son club
                int targetPool = findTargetPool(toMove, pools, i, rankedParticipants);
                if (targetPool == -1) {
                    // Impossible de résoudre : on laisse tel quel (cas > nb poules)
                    continue;
                }

                // Déplacer en échangeant avec un joueur de rang similaire
                swapIntoPool(pools, i, targetPool, toMove, rankedParticipants);
                changed = true;
                break; // recommencer depuis le début après chaque modif
            }
        }
    }

    /**
     * Retourne les deux premiers joueurs en conflit de club dans une poule,
     * ou null si pas de conflit.
     */
    private List<Participant> findClubConflict(List<Participant> pool) {
        for (int i = 0; i < pool.size(); i++) {
            for (int j = i + 1; j < pool.size(); j++) {
                if (sameClub(pool.get(i), pool.get(j))) {
                    return List.of(pool.get(i), pool.get(j));
                }
            }
        }
        return null;
    }

    /**
     * Retourne le joueur le moins bien classé (rang serpent le plus élevé).
     */
    private Participant leastRanked(List<Participant> candidates,
            List<Participant> ranked) {
        return candidates.stream()
                .max(Comparator.comparingInt(ranked::indexOf))
                .orElseThrow();
    }

    /**
     * Cherche une poule cible (différente de sourcePool) ne contenant
     * pas de joueur du même club que {@code toMove}.
     * Préfère la poule voisine dans l'ordre du serpent.
     */
    private int findTargetPool(Participant toMove, List<List<Participant>> pools,
            int sourcePool, List<Participant> ranked) {
        // On cherche dans l'ordre : poule suivante, poule précédente, etc.
        for (int delta = 1; delta < pools.size(); delta++) {
            int candidate = (sourcePool + delta) % pools.size();
            if (!containsClubOf(toMove, pools.get(candidate))) {
                return candidate;
            }
            int candidate2 = ((sourcePool - delta) + pools.size()) % pools.size();
            if (candidate2 != candidate && !containsClubOf(toMove, pools.get(candidate2))) {
                return candidate2;
            }
        }
        return -1; // impossible
    }

    /**
     * Échange {@code toMove} de sourcePool vers targetPool.
     * On échange avec le joueur de targetPool le plus proche en rang
     * (pour maintenir l'équilibre des poules).
     */
    private void swapIntoPool(List<List<Participant>> pools,
            int sourcePool, int targetPool,
            Participant toMove, List<Participant> ranked) {

        List<Participant> src = pools.get(sourcePool);
        List<Participant> tgt = pools.get(targetPool);

        // Trouver dans la cible le joueur de rang le plus proche de toMove
        // qui ne crée pas de nouveau conflit dans la poule source
        int toMoveRank = ranked.indexOf(toMove);

        Participant bestSwap = null;
        int bestRankDiff = Integer.MAX_VALUE;

        for (Participant candidate : tgt) {
            // Ce candidat ne doit pas créer de conflit dans la source
            if (wouldCreateConflict(candidate, src, toMove))
                continue;
            int diff = Math.abs(ranked.indexOf(candidate) - toMoveRank);
            if (diff < bestRankDiff) {
                bestRankDiff = diff;
                bestSwap = candidate;
            }
        }

        if (bestSwap == null)
            return; // pas d'échange possible

        // Effectuer l'échange
        src.remove(toMove);
        tgt.remove(bestSwap);
        src.add(bestSwap);
        tgt.add(toMove);
    }

    /**
     * Vérifie si placer {@code candidate} dans {@code pool} (après avoir retiré
     * {@code excluded})
     * créerait un conflit de club.
     */
    private boolean wouldCreateConflict(Participant candidate,
            List<Participant> pool,
            Participant excluded) {
        for (Participant p : pool) {
            if (p.equals(excluded))
                continue;
            if (sameClub(p, candidate))
                return true;
        }
        return false;
    }

    private boolean containsClubOf(Participant p, List<Participant> pool) {
        return pool.stream().anyMatch(other -> sameClub(p, other));
    }

    /**
     * Détermine si deux participants appartiennent au même club.
     * Seuls les FfttParticipant ont un numéro de club ; les invités/étrangers
     * sont considérés comme n'ayant pas de club (pas de contrainte entre eux).
     */
    private boolean sameClub(Participant a, Participant b) {
        if (!(a instanceof FfttParticipant fa))
            return false;
        if (!(b instanceof FfttParticipant fb))
            return false;
        String clubA = fa.player().getClub().getNumber();
        String clubB = fb.player().getClub().getNumber();
        return clubA != null && clubA.equals(clubB);
    }
}