package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value object représentant le score complet d'un match de poule.
 *
 * Un match FFTT se joue au meilleur des 5 manches.
 * Chaque manche se joue en 11 points (avec 2 points d'écart minimum).
 *
 * Exemples de scores valides :
 * 3-0 : victoire nette
 * 3-2 : victoire au bout
 * 0-3 : défaite nette
 *
 * Le score stocke la liste des manches : chaque manche est un int[2]
 * où [0] = points joueur1, [1] = points joueur2.
 *
 * Immuable.
 */
public final class PoolMatchScore {

    /** Nombre de manches gagnantes dans un match FFTT standard (best of 5). */
    private static final int WINNING_SETS = 3;

    /**
     * Liste des manches jouées.
     * Chaque manche : int[2] = { pointsJ1, pointsJ2 }
     */
    private final List<int[]> sets;

    /** Manches gagnées par le joueur 1. */
    private final int setsWonByPlayer1;

    /** Manches gagnées par le joueur 2. */
    private final int setsWonByPlayer2;

    // -------------------------------------------------------------------------
    // CONSTRUCTEUR
    // -------------------------------------------------------------------------

    /**
     * @param sets liste des manches. Chaque élément est int[2] = {ptsJ1, ptsJ2}.
     *             La liste doit représenter un match complet et valide.
     */
    public PoolMatchScore(List<int[]> sets) {
        Objects.requireNonNull(sets, "sets");
        if (sets.isEmpty()) {
            throw new BusinessException(ErrorCode.POOL_MATCH_SCORE_EMPTY);
        }

        int wonBy1 = 0;
        int wonBy2 = 0;

        for (int[] set : sets) {
            if (set == null || set.length != 2) {
                throw new BusinessException(ErrorCode.POOL_MATCH_SCORE_INVALID_SET);
            }
            if (set[0] < 0 || set[1] < 0) {
                throw new BusinessException(ErrorCode.POOL_MATCH_SCORE_NEGATIVE_POINTS);
            }
            validateSet(set[0], set[1]);

            if (set[0] > set[1])
                wonBy1++;
            else
                wonBy2++;
        }

        validateMatchCompletion(wonBy1, wonBy2, sets.size());

        // copie défensive
        List<int[]> copy = new ArrayList<>();
        for (int[] set : sets) {
            copy.add(new int[] { set[0], set[1] });
        }
        this.sets = Collections.unmodifiableList(copy);
        this.setsWonByPlayer1 = wonBy1;
        this.setsWonByPlayer2 = wonBy2;
    }

    // -------------------------------------------------------------------------
    // FACTORY (score rapide pour les tests)
    // -------------------------------------------------------------------------

    /**
     * Crée un score simplifié sans le détail des points par manche.
     * Utile pour l'import ou les tests : on connaît juste le nombre de manches.
     *
     * @param setsJ1 manches gagnées par le joueur 1
     * @param setsJ2 manches gagnées par le joueur 2
     */
    public static PoolMatchScore ofSetsOnly(int setsJ1, int setsJ2) {
        List<int[]> sets = new ArrayList<>();
        // on génère des manches "fictives" valides (11-0 ou 0-11)
        for (int i = 0; i < setsJ1; i++)
            sets.add(new int[] { 11, 0 });
        for (int i = 0; i < setsJ2; i++)
            sets.add(new int[] { 0, 11 });
        return new PoolMatchScore(sets);
    }

    // -------------------------------------------------------------------------
    // VALIDATIONS
    // -------------------------------------------------------------------------

    /**
     * Valide qu'une manche est légale selon les règles FFTT/ITTF.
     * Une manche se gagne à 11 points avec au moins 2 points d'écart.
     * En cas de 10-10 (déuce), on continue jusqu'à 2 points d'écart.
     */
    private static void validateSet(int p1, int p2) {
        int max = Math.max(p1, p2);
        int min = Math.min(p1, p2);
        int diff = max - min;

        if (max < 11) {
            throw new BusinessException(ErrorCode.POOL_MATCH_SCORE_SET_NOT_FINISHED);
        }
        if (max == 11 && min > 9) {
            // 11-10 est invalide, il faut au moins 2 d'écart
            throw new BusinessException(ErrorCode.POOL_MATCH_SCORE_SET_INVALID_DEUCE);
        }
        if (max > 11 && diff != 2) {
            // en déuce, on joue jusqu'à 2 d'écart exactement
            throw new BusinessException(ErrorCode.POOL_MATCH_SCORE_SET_INVALID_DEUCE);
        }
        if (max > 11 && min < 10) {
            // ex: 15-7 est impossible (la manche aurait dû s'arrêter à 11)
            throw new BusinessException(ErrorCode.POOL_MATCH_SCORE_SET_INVALID_DEUCE);
        }
    }

    /**
     * Valide que le match est bien terminé :
     * - un des deux joueurs a atteint WINNING_SETS (3)
     * - le nombre total de manches est cohérent
     */
    private static void validateMatchCompletion(int wonBy1, int wonBy2, int totalSets) {
        int maxSets = WINNING_SETS * 2 - 1; // 5 au maximum
        if (totalSets > maxSets) {
            throw new BusinessException(ErrorCode.POOL_MATCH_SCORE_TOO_MANY_SETS);
        }
        if (wonBy1 != WINNING_SETS && wonBy2 != WINNING_SETS) {
            throw new BusinessException(ErrorCode.POOL_MATCH_SCORE_NOT_FINISHED);
        }
        if (wonBy1 == WINNING_SETS && wonBy2 == WINNING_SETS) {
            throw new BusinessException(ErrorCode.POOL_MATCH_SCORE_INVALID);
        }
    }

    // -------------------------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------------------------

    public boolean player1Wins() {
        return setsWonByPlayer1 == WINNING_SETS;
    }

    public boolean player2Wins() {
        return setsWonByPlayer2 == WINNING_SETS;
    }

    public int setsWonByPlayer1() {
        return setsWonByPlayer1;
    }

    public int setsWonByPlayer2() {
        return setsWonByPlayer2;
    }

    /** Total des points marqués par le joueur 1 sur toutes les manches. */
    public int totalPointsPlayer1() {
        return sets.stream().mapToInt(s -> s[0]).sum();
    }

    /** Total des points marqués par le joueur 2 sur toutes les manches. */
    public int totalPointsPlayer2() {
        return sets.stream().mapToInt(s -> s[1]).sum();
    }

    /** Nombre de manches jouées. */
    public int totalSets() {
        return sets.size();
    }

    /** Copie défensive de la liste des manches. */
    public List<int[]> sets() {
        List<int[]> copy = new ArrayList<>();
        for (int[] s : sets)
            copy.add(new int[] { s[0], s[1] });
        return copy;
    }

    // -------------------------------------------------------------------------
    // EQUALS / HASHCODE
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PoolMatchScore other))
            return false;
        if (sets.size() != other.sets.size())
            return false;
        for (int i = 0; i < sets.size(); i++) {
            if (sets.get(i)[0] != other.sets.get(i)[0])
                return false;
            if (sets.get(i)[1] != other.sets.get(i)[1])
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 1;
        for (int[] s : sets)
            h = 31 * h + (s[0] * 100 + s[1]);
        return h;
    }

    @Override
    public String toString() {
        return "%d-%d".formatted(setsWonByPlayer1, setsWonByPlayer2);
    }
}