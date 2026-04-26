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
 * Principe (Manuel JA, section I-6.1) :
 * Les joueurs sont classés 1 à N par points décroissants.
 * Ils sont répartis en P poules selon un parcours en zigzag :
 *
 * Aller → : poule 1, 2, 3, ..., P
 * Retour ← : poule P, P-1, ..., 1
 * Aller → : poule 1, 2, 3, ..., P
 * ...
 *
 * Exemple : 9 joueurs en 3 poules de 3 (joueurs triés 1..9)
 * Tour aller : J1→P1, J2→P2, J3→P3
 * Tour retour : J4→P3, J5→P2, J6→P1
 * Tour aller : J7→P1, J8→P2, J9→P3
 *
 * P1 = {J1, J6, J7}
 * P2 = {J2, J5, J8}
 * P3 = {J3, J4, J9}
 *
 * Règle clé : quand on atteint le bout (poule 1 ou poule P), on inverse la
 * direction SANS avancer. Le prochain joueur est placé dans la même poule
 * limite, puis repart dans l'autre sens.
 *
 * Contrainte de club (article I.302 des Règlements Sportifs) :
 * On évite que deux joueurs du même club soient dans la même poule.
 * En cas de conflit, le moins bien classé est déplacé vers la poule
 * voisine disponible, en respectant l'ordre du serpent.
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
        int nbPools = (int) Math.ceil((double) nbPlayers / poolSize);

        // Initialiser les poules
        List<List<Participant>> pools = new ArrayList<>();
        for (int i = 0; i < nbPools; i++) {
            pools.add(new ArrayList<>());
        }

        // ---- Placement en serpent ----
        //
        // Règle FFTT : quand on atteint le bout, on inverse la direction
        // SANS avancer. Le joueur suivant reste dans la même poule limite
        // puis repart en sens inverse.
        //
        // Exemple 9 joueurs, 3 poules :
        // idx: 0 1 2 2 1 0 0 1 2 ← indices de poule
        // dir: → → → ← ← ← → → →
        // J: 1 2 3 4 5 6 7 8 9
        int poolIndex = 0;
        int direction = 1; // +1 = aller (vers droite), -1 = retour (vers gauche)

        for (Participant p : rankedParticipants) {
            pools.get(poolIndex).add(p);

            int next = poolIndex + direction;
            if (next < 0 || next >= nbPools) {
                // Bout atteint : on inverse, mais on NE BOUGE PAS
                direction = -direction;
                // poolIndex reste identique pour le prochain joueur
            } else {
                poolIndex = next;
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

    private void applyClubConstraint(List<List<Participant>> pools,
            List<Participant> rankedParticipants) {
        boolean changed = true;
        int maxIterations = pools.size() * rankedParticipants.size();

        while (changed && maxIterations-- > 0) {
            changed = false;

            for (int i = 0; i < pools.size(); i++) {
                List<Participant> conflict = findClubConflict(pools.get(i));
                if (conflict == null)
                    continue;

                Participant toMove = leastRanked(conflict, rankedParticipants);
                int targetPool = findTargetPool(toMove, pools, i);
                if (targetPool == -1)
                    continue; // insoluble (plus joueurs d'un club que de poules)

                swapIntoPool(pools, i, targetPool, toMove, rankedParticipants);
                changed = true;
                break; // recommencer depuis le début après chaque modification
            }
        }
    }

    /**
     * Retourne les deux premiers joueurs en conflit de club dans une poule,
     * ou null si aucun conflit.
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
     * Cherche la poule cible la plus proche (dans l'ordre du serpent)
     * ne contenant pas de joueur du même club que {@code toMove}.
     */
    private int findTargetPool(Participant toMove,
            List<List<Participant>> pools,
            int sourcePool) {
        for (int delta = 1; delta < pools.size(); delta++) {
            int candidate = (sourcePool + delta) % pools.size();
            if (!containsClubOf(toMove, pools.get(candidate))) {
                return candidate;
            }
            int candidate2 = ((sourcePool - delta) + pools.size()) % pools.size();
            if (candidate2 != candidate
                    && !containsClubOf(toMove, pools.get(candidate2))) {
                return candidate2;
            }
        }
        return -1;
    }

    /**
     * Échange {@code toMove} (de sourcePool) avec le joueur de rang le plus
     * proche dans targetPool, sans créer de nouveau conflit de club
     * dans aucune des deux poules.
     */
    private void swapIntoPool(List<List<Participant>> pools,
            int sourcePool, int targetPool,
            Participant toMove,
            List<Participant> ranked) {

        List<Participant> src = pools.get(sourcePool);
        List<Participant> tgt = pools.get(targetPool);

        int toMoveRank = ranked.indexOf(toMove);
        Participant bestSwap = null;
        int bestRankDiff = Integer.MAX_VALUE;

        for (Participant candidate : tgt) {
            // Vérifier que l'échange ne crée pas de conflit dans src ni dans tgt
            if (wouldCreateConflict(candidate, src, toMove))
                continue;
            if (wouldCreateConflict(toMove, tgt, candidate))
                continue;

            int diff = Math.abs(ranked.indexOf(candidate) - toMoveRank);
            if (diff < bestRankDiff) {
                bestRankDiff = diff;
                bestSwap = candidate;
            }
        }

        if (bestSwap == null)
            return;

        src.remove(toMove);
        tgt.remove(bestSwap);
        src.add(bestSwap);
        tgt.add(toMove);
    }

    /**
     * Vérifie si placer {@code candidate} dans {@code pool}
     * (après avoir retiré {@code excluded}) créerait un conflit de club.
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
     * Deux participants sont du même club si tous les deux sont FfttParticipant
     * et ont le même numéro de club.
     * Les invités/étrangers ne sont pas soumis à cette contrainte.
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