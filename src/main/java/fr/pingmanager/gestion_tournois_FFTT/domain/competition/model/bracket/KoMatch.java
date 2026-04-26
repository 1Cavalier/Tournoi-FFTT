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
 * Cycle de vie :
 * PENDING → IN_PROGRESS → COMPLETED
 * → WALKOVER
 * PENDING → BYE (un seul joueur, passe automatiquement)
 *
 * Un match passe en BYE si et seulement si assignPlayers() est appelé
 * avec exactement un joueur null (XOR).
 * Deux joueurs null = PENDING (match pas encore peuplé).
 * Deux joueurs non-null = PENDING (match normal, attend le start/recordScore).
 */
public final class KoMatch {

    // -------------------------------------------------------------------------
    // STATUTS
    // -------------------------------------------------------------------------

    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        WALKOVER,
        BYE
    }

    // -------------------------------------------------------------------------
    // CHAMPS
    // -------------------------------------------------------------------------

    private final String id;
    private final int round;
    private final int position;

    private Participant player1;
    private Participant player2;

    private Status status;
    private PoolMatchScore score;
    private String walkoverId;

    // -------------------------------------------------------------------------
    // CONSTRUCTEURS
    // -------------------------------------------------------------------------

    public KoMatch(int round, int position) {
        this.id = UUID.randomUUID().toString();
        this.round = round;
        this.position = position;
        this.status = Status.PENDING;
    }

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
     * Assigne les joueurs au match.
     *
     * Règles de statut :
     * - Exactement un null (XOR) → BYE automatique
     * - Les deux null → PENDING (match pas encore peuplé)
     * - Les deux non-null → PENDING (match normal, attend recordScore)
     *
     * Cette méthode peut être appelée plusieurs fois lors de la propagation
     * (d'abord player1, puis player2). On recalcule le statut à chaque appel.
     */
    public void assignPlayers(Participant p1, Participant p2) {
        this.player1 = p1;
        this.player2 = p2;

        boolean p1Present = p1 != null;
        boolean p2Present = p2 != null;

        if (p1Present ^ p2Present) {
            // XOR : exactement un joueur → BYE
            this.status = Status.BYE;
        } else {
            // Les deux présents ou les deux absents → PENDING
            // (sauf si le match est déjà terminé via recordScore/walkover)
            if (this.status == Status.BYE) {
                // On était BYE mais maintenant les deux sont présents → PENDING
                this.status = Status.PENDING;
            }
            // Si COMPLETED/WALKOVER/IN_PROGRESS : on ne change pas le statut
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

    public Participant winner() {
        return switch (status) {
            case COMPLETED -> score.player1Wins() ? player1 : player2;
            case WALKOVER -> walkoverId.equals(player1Id()) ? player2 : player1;
            case BYE -> player1 != null ? player1 : player2;
            default -> null;
        };
    }

    public Participant loser() {
        return switch (status) {
            case COMPLETED -> score.player1Wins() ? player2 : player1;
            case WALKOVER -> walkoverId.equals(player1Id()) ? player1 : player2;
            default -> null;
        };
    }

    public boolean isWalkoverFor(Participant p) {
        return status == Status.WALKOVER && walkoverId.equals(p.participantId());
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