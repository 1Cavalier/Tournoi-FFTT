package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.classification;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolMatchScore;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity représentant un match de classement.
 *
 * Un match de classement oppose deux joueurs éliminés au même tour du tableau
 * KO
 * pour déterminer leur rang final (ex : match pour la 3ème place).
 *
 * Contrairement aux matchs KO, il n'y a pas de BYE possible : les deux joueurs
 * sont toujours connus avant que le match ne commence.
 *
 * Cycle de vie : PENDING → IN_PROGRESS → COMPLETED
 * → WALKOVER
 */
public final class ClassificationMatch {

    // -------------------------------------------------------------------------
    // STATUTS
    // -------------------------------------------------------------------------

    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        WALKOVER
    }

    // -------------------------------------------------------------------------
    // CHAMPS
    // -------------------------------------------------------------------------

    private final String id;

    /**
     * Rang attribué au vainqueur de ce match.
     * Ex : 3 pour le match pour la 3ème place.
     */
    private final int winnerRank;

    /**
     * Rang attribué au perdant de ce match.
     * Ex : 4 pour le match pour la 3ème place.
     */
    private final int loserRank;

    private final Participant player1;
    private final Participant player2;

    private Status status;
    private PoolMatchScore score;
    private String walkoverId;

    // -------------------------------------------------------------------------
    // CONSTRUCTEUR
    // -------------------------------------------------------------------------

    public ClassificationMatch(int winnerRank, int loserRank,
            Participant player1, Participant player2) {
        if (winnerRank < 1) {
            throw new IllegalArgumentException("winnerRank must be >= 1");
        }
        if (loserRank <= winnerRank) {
            throw new IllegalArgumentException("loserRank must be > winnerRank");
        }
        this.id = UUID.randomUUID().toString();
        this.winnerRank = winnerRank;
        this.loserRank = loserRank;
        this.player1 = Objects.requireNonNull(player1, "player1");
        this.player2 = Objects.requireNonNull(player2, "player2");
        this.status = Status.PENDING;

        if (player1.equals(player2)) {
            throw new BusinessException(ErrorCode.CLASSIFICATION_MATCH_SAME_PARTICIPANT);
        }
    }

    /** Constructeur de reconstruction depuis la base. */
    public ClassificationMatch(String id, int winnerRank, int loserRank,
            Participant player1, Participant player2,
            Status status, PoolMatchScore score, String walkoverId) {
        this.id = Objects.requireNonNull(id);
        this.winnerRank = winnerRank;
        this.loserRank = loserRank;
        this.player1 = Objects.requireNonNull(player1);
        this.player2 = Objects.requireNonNull(player2);
        this.status = Objects.requireNonNull(status);
        this.score = score;
        this.walkoverId = walkoverId;
    }

    // -------------------------------------------------------------------------
    // ACTIONS MÉTIER
    // -------------------------------------------------------------------------

    public void start() {
        if (status != Status.PENDING) {
            throw new BusinessException(ErrorCode.CLASSIFICATION_MATCH_INVALID_TRANSITION);
        }
        this.status = Status.IN_PROGRESS;
    }

    public void recordScore(PoolMatchScore score) {
        if (isFinished()) {
            throw new BusinessException(ErrorCode.CLASSIFICATION_MATCH_ALREADY_FINISHED);
        }
        this.score = Objects.requireNonNull(score, "score");
        this.status = Status.COMPLETED;
    }

    public void declareWalkover(Participant participant) {
        Objects.requireNonNull(participant, "participant");
        if (isFinished()) {
            throw new BusinessException(ErrorCode.CLASSIFICATION_MATCH_ALREADY_FINISHED);
        }
        String pid = participant.participantId();
        if (!pid.equals(player1.participantId()) && !pid.equals(player2.participantId())) {
            throw new BusinessException(ErrorCode.CLASSIFICATION_MATCH_PARTICIPANT_NOT_IN_MATCH);
        }
        this.walkoverId = pid;
        this.status = Status.WALKOVER;
    }

    // -------------------------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------------------------

    public boolean isFinished() {
        return status == Status.COMPLETED || status == Status.WALKOVER;
    }

    public Participant winner() {
        return switch (status) {
            case COMPLETED -> score.player1Wins() ? player1 : player2;
            case WALKOVER -> walkoverId.equals(player1.participantId()) ? player2 : player1;
            default -> null;
        };
    }

    public Participant loser() {
        return switch (status) {
            case COMPLETED -> score.player1Wins() ? player2 : player1;
            case WALKOVER -> walkoverId.equals(player1.participantId()) ? player1 : player2;
            default -> null;
        };
    }

    /**
     * Retourne le rang final du vainqueur (null si match pas terminé).
     */
    public Integer resolvedWinnerRank() {
        return isFinished() ? winnerRank : null;
    }

    /**
     * Retourne le rang final du perdant (null si match pas terminé).
     */
    public Integer resolvedLoserRank() {
        return isFinished() ? loserRank : null;
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public String id() {
        return id;
    }

    public int winnerRank() {
        return winnerRank;
    }

    public int loserRank() {
        return loserRank;
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
    // EQUALS / HASHCODE
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ClassificationMatch other))
            return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ClassificationMatch{rank=%d/%d, %s vs %s, status=%s}"
                .formatted(winnerRank, loserRank,
                        player1.participantId(), player2.participantId(), status);
    }
}