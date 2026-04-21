package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolMatchScore;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity représentant un match dans le tableau à élimination directe (TED).
 *
 * Structure d'un match KO :
 * - round : le tour (1 = 1er tour, 2 = quart, 3 = demi, 4 = finale...)
 * - position : la position dans l'arbre du tableau (numérotation 1..N)
 *
 * Un match KO peut avoir un BYE (exemption) quand le tableau n'est pas
 * une puissance de 2 exacte. Dans ce cas, le joueur présent passe directement
 * au tour suivant.
 *
 * Cycle de vie :
 * PENDING → IN_PROGRESS → COMPLETED
 * → WALKOVER
 */
public final class KoMatch {

    // -------------------------------------------------------------------------
    // STATUTS
    // -------------------------------------------------------------------------

    public enum Status {
        /** Match en attente (joueurs pas encore connus ou match pas commencé). */
        PENDING,
        /** Match en cours. */
        IN_PROGRESS,
        /** Match terminé avec un score. */
        COMPLETED,
        /** Forfait : le joueur concerné marque 0, l'adversaire avance. */
        WALKOVER,
        /** Exemption (BYE) : un seul joueur, passe automatiquement. */
        BYE
    }

    // -------------------------------------------------------------------------
    // CHAMPS
    // -------------------------------------------------------------------------

    private final String id;

    /** Tour du tableau (1 = premier tour, 2 = deuxième tour, etc.). */
    private final int round;

    /**
     * Position dans le tableau (1 = haut, 2 = bas du même quart, etc.).
     * Permet de reconstruire l'arbre complet.
     */
    private final int position;

    /**
     * Joueur en haut (slot "gauche" du match). Peut être null si pas encore
     * qualifié.
     */
    private Participant player1;

    /**
     * Joueur en bas (slot "droit" du match). Peut être null si BYE ou pas encore
     * qualifié.
     */
    private Participant player2;

    private Status status;
    private PoolMatchScore score;

    /** Id du joueur forfait (uniquement si WALKOVER). */
    private String walkoverId;

    // -------------------------------------------------------------------------
    // CONSTRUCTEUR
    // -------------------------------------------------------------------------

    public KoMatch(int round, int position) {
        this.id = UUID.randomUUID().toString();
        this.round = round;
        this.position = position;
        this.status = Status.PENDING;
    }

    /** Constructeur de reconstruction depuis la base. */
    public KoMatch(String id, int round, int position,
            Participant player1, Participant player2,
            Status status, PoolMatchScore score, String walkoverId) {
        this.id = Objects.requireNonNull(id, "id");
        this.round = round;
        this.position = position;
        this.player1 = player1;
        this.player2 = player2;
        this.status = Objects.requireNonNull(status, "status");
        this.score = score;
        this.walkoverId = walkoverId;
    }

    // -------------------------------------------------------------------------
    // ACTIONS MÉTIER
    // -------------------------------------------------------------------------

    /**
     * Assigne les joueurs (appelé par BracketBuilder lors du placement initial).
     */
    public void assignPlayers(Participant p1, Participant p2) {
        this.player1 = p1;
        this.player2 = p2;
        // Si l'un des deux est null → c'est un BYE
        if (p1 == null || p2 == null) {
            this.status = Status.BYE;
        }
    }

    /** Démarre le match. */
    public void start() {
        if (status != Status.PENDING) {
            throw new BusinessException(ErrorCode.BRACKET_MATCH_INVALID_TRANSITION);
        }
        this.status = Status.IN_PROGRESS;
    }

    /** Enregistre le score et clôt le match. */
    public void recordScore(PoolMatchScore score) {
        if (isFinished()) {
            throw new BusinessException(ErrorCode.BRACKET_MATCH_ALREADY_FINISHED);
        }
        Objects.requireNonNull(score, "score");
        this.score = score;
        this.status = Status.COMPLETED;
    }

    /** Déclare un forfait. */
    public void declareWalkover(Participant participant) {
        Objects.requireNonNull(participant, "participant");
        if (isFinished()) {
            throw new BusinessException(ErrorCode.BRACKET_MATCH_ALREADY_FINISHED);
        }
        String pid = participant.participantId();
        if (!pid.equals(player1Id()) && !pid.equals(player2Id())) {
            throw new BusinessException(ErrorCode.BRACKET_MATCH_PARTICIPANT_NOT_IN_MATCH);
        }
        this.walkoverId = pid;
        this.status = Status.WALKOVER;
    }

    // -------------------------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------------------------

    public boolean isFinished() {
        return status == Status.COMPLETED
                || status == Status.WALKOVER
                || status == Status.BYE;
    }

    /**
     * Retourne le vainqueur du match.
     * Pour un BYE : retourne le joueur présent (player1 ou player2, l'autre étant
     * null).
     */
    public Participant winner() {
        return switch (status) {
            case COMPLETED -> score.player1Wins() ? player1 : player2;
            case WALKOVER -> walkoverId.equals(player1Id()) ? player2 : player1;
            case BYE -> player1 != null ? player1 : player2;
            default -> null;
        };
    }

    /**
     * Retourne le perdant (null pour un BYE).
     */
    public Participant loser() {
        return switch (status) {
            case COMPLETED -> score.player1Wins() ? player2 : player1;
            case WALKOVER -> walkoverId.equals(player1Id()) ? player1 : player2;
            default -> null;
        };
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public String id() {
        return id;
    }

    public int round() {
        return round;
    }

    public int position() {
        return position;
    }

    public Participant player1() {
        return player1;
    }

    public Participant player2() {
        return player2;
    }

    public Status status() {
        return status;
    }

    public PoolMatchScore score() {
        return score;
    }

    public String walkoverId() {
        return walkoverId;
    }

    // -------------------------------------------------------------------------
    // HELPERS PRIVÉS
    // -------------------------------------------------------------------------

    private String player1Id() {
        return player1 == null ? null : player1.participantId();
    }

    private String player2Id() {
        return player2 == null ? null : player2.participantId();
    }

    // -------------------------------------------------------------------------
    // EQUALS / HASHCODE
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof KoMatch other))
            return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "KoMatch{round=%d, pos=%d, status=%s}".formatted(round, position, status);
    }
}