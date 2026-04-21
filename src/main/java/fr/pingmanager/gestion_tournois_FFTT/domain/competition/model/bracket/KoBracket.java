package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolMatchScore;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * Aggregate représentant le tableau à élimination directe (TED) d'un tableau de
 * tournoi.
 *
 * Structure :
 * - Le tableau est une puissance de 2 (4, 8, 16, 32...) ou inclut des BYE
 * pour les cas où le nombre de qualifiés n'est pas une puissance de 2 exacte.
 * - Les matchs sont identifiés par (round, position).
 * - Round 1 = premier tour, round max = finale.
 *
 * Règles de placement FFTT (article I.305) :
 * - Les 1ers de poule sont dans des demi-tableaux différents de leurs 2èmes.
 * - Les BYE (exemptions) sont attribués aux mieux classés (rangs serpent les
 * plus bas).
 *
 * L'aggregate délègue la construction initiale à {@link BracketBuilder}.
 */
public final class KoBracket {

    // -------------------------------------------------------------------------
    // CHAMPS
    // -------------------------------------------------------------------------

    private final String id;
    private final String tableauCode;

    /**
     * Taille du tableau (puissance de 2 supérieure ou égale au nb de qualifiés).
     * Ex : 10 qualifiés → taille 16.
     */
    private final int bracketSize;

    /**
     * Nombre de tours (log2 de bracketSize).
     * Ex : bracketSize=16 → 4 tours (1/8, QF, SF, F).
     */
    private final int totalRounds;

    /**
     * Tous les matchs du tableau, ordonnés par (round, position).
     * Clé : "round-position", ex "1-1", "1-2", "2-1"...
     */
    private final Map<String, KoMatch> matchesByKey;

    // -------------------------------------------------------------------------
    // CONSTRUCTEUR
    // -------------------------------------------------------------------------

    /** Constructeur principal (nouvelle instance — id généré). */
    KoBracket(String tableauCode, int bracketSize, List<KoMatch> matches) {
        this(UUID.randomUUID().toString(), tableauCode, bracketSize, matches);
    }

    /**
     * Constructeur complet : utilisé à la création ET pour la reconstruction depuis
     * la base.
     */
    public KoBracket(String id, String tableauCode, int bracketSize, List<KoMatch> matches) {
        this.id = Objects.requireNonNull(id, "id");
        this.tableauCode = Objects.requireNonNull(tableauCode, "tableauCode")
                .trim().toUpperCase();
        if (bracketSize < 2 || (bracketSize & (bracketSize - 1)) != 0) {
            throw new IllegalArgumentException(
                    "bracketSize must be a power of 2, got: " + bracketSize);
        }
        this.bracketSize = bracketSize;
        this.totalRounds = (int) (Math.log(bracketSize) / Math.log(2));

        this.matchesByKey = new LinkedHashMap<>();
        for (KoMatch m : matches) {
            matchesByKey.put(key(m.round(), m.position()), m);
        }
    }

    // -------------------------------------------------------------------------
    // ACTIONS MÉTIER
    // -------------------------------------------------------------------------

    /**
     * Enregistre le score d'un match KO et propage le vainqueur au tour suivant.
     */
    public void recordScore(int round, int position, PoolMatchScore score) {
        KoMatch match = findMatch(round, position);
        match.recordScore(score);
        propagateWinner(match);
    }

    /** Démarre un match KO. */
    public void startMatch(int round, int position) {
        findMatch(round, position).start();
    }

    /** Déclare un forfait dans un match KO. */
    public void declareWalkover(int round, int position, Participant participant) {
        KoMatch match = findMatch(round, position);
        match.declareWalkover(participant);
        propagateWinner(match);
    }

    // -------------------------------------------------------------------------
    // PROPAGATION DU VAINQUEUR
    // -------------------------------------------------------------------------

    /**
     * Après qu'un match est terminé, place le vainqueur dans le match du tour
     * suivant.
     *
     * Convention de numérotation des positions :
     * Au tour T, positions 1..N.
     * Le vainqueur de la position P au tour T joue à la position ceil(P/2) au tour
     * T+1.
     * Il est player1 si P est impair, player2 si P est pair.
     */
    private void propagateWinner(KoMatch finishedMatch) {
        Participant winner = finishedMatch.winner();
        if (winner == null)
            return;

        int nextRound = finishedMatch.round() + 1;
        int nextPosition = (int) Math.ceil(finishedMatch.position() / 2.0);

        KoMatch nextMatch = matchesByKey.get(key(nextRound, nextPosition));
        if (nextMatch == null)
            return; // finale : pas de match suivant

        boolean isPlayer1 = (finishedMatch.position() % 2 == 1);
        if (isPlayer1) {
            nextMatch.assignPlayers(winner, nextMatch.player2());
        } else {
            nextMatch.assignPlayers(nextMatch.player1(), winner);
        }
    }

    // -------------------------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------------------------

    /**
     * Retourne le vainqueur final du tableau (null si la finale n'est pas jouée).
     */
    public Participant champion() {
        KoMatch finale = finalMatch();
        return finale != null ? finale.winner() : null;
    }

    /** Retourne le match de finale (dernier round, position 1). */
    public KoMatch finalMatch() {
        return matchesByKey.get(key(totalRounds, 1));
    }

    /** Retourne tous les matchs d'un tour donné. */
    public List<KoMatch> matchesForRound(int round) {
        return matchesByKey.values().stream()
                .filter(m -> m.round() == round)
                .sorted(Comparator.comparingInt(KoMatch::position))
                .collect(Collectors.toList());
    }

    /** Retourne tous les matchs du tableau, triés par (round, position). */
    public List<KoMatch> allMatches() {
        return matchesByKey.values().stream()
                .sorted(Comparator.comparingInt(KoMatch::round)
                        .thenComparingInt(KoMatch::position))
                .collect(Collectors.toList());
    }

    /** Vrai si tous les matchs sont terminés. */
    public boolean isComplete() {
        return matchesByKey.values().stream().allMatch(KoMatch::isFinished);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private KoMatch findMatch(int round, int position) {
        KoMatch m = matchesByKey.get(key(round, position));
        if (m == null)
            throw new BusinessException(ErrorCode.BRACKET_MATCH_NOT_FOUND);
        return m;
    }

    private static String key(int round, int position) {
        return round + "-" + position;
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public String id() {
        return id;
    }

    public String tableauCode() {
        return tableauCode;
    }

    public int bracketSize() {
        return bracketSize;
    }

    public int totalRounds() {
        return totalRounds;
    }
}