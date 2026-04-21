package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity représentant un match entre deux joueurs dans une poule.
 *
 * Cycle de vie du statut :
 * PENDING → IN_PROGRESS → COMPLETED
 * → WALKOVER (abandon / forfait déclaré par le JA)
 *
 * L'ordre des matchs dans une poule de 3 est fixé par la FFTT :
 * match 1 : position 1 vs position 3
 * match 2 : position 1 vs position 2
 * match 3 : position 2 vs position 3
 *
 * La position dans la poule est capturée via les PoolSlot.
 */
public final class PoolMatch {

    // -------------------------------------------------------------------------
    // STATUTS
    // -------------------------------------------------------------------------

    public enum Status {
        /** Match pas encore commencé. */
        PENDING,
        /** Match en cours (optionnel, utile pour l'UI temps réel). */
        IN_PROGRESS,
        /** Match terminé normalement, score saisi. */
        COMPLETED,
        /**
         * Forfait / abandon : le joueur concerné marque 0 point-partie.
         * Son adversaire marque 2 points-partie (victoire).
         * Les résultats de ce joueur dans la poule sont annulés (règle FFTT).
         */
        WALKOVER
    }

    // -------------------------------------------------------------------------
    // CHAMPS
    // -------------------------------------------------------------------------

    private final String id;

    /** Slot du joueur 1 (position dans la poule). */
    private final PoolSlot slot1;

    /** Slot du joueur 2 (position dans la poule). */
    private final PoolSlot slot2;

    /**
     * Numéro d'ordre du match dans la poule (1, 2, 3).
     * Déterminé à la création selon les règles FFTT.
     */
    private final int matchOrderInPool;

    private Status status;

    /**
     * Score du match. Null tant que le match n'est pas COMPLETED.
     * En cas de WALKOVER, null également (on utilise walkoverId).
     */
    private PoolMatchScore score;

    /**
     * Id du participant qui a déclaré forfait/abandon.
     * Non null uniquement si status == WALKOVER.
     */
    private String walkoverId;

    // -------------------------------------------------------------------------
    // CONSTRUCTEUR
    // -------------------------------------------------------------------------

    public PoolMatch(PoolSlot slot1, PoolSlot slot2, int matchOrderInPool) {
        this.id = UUID.randomUUID().toString();
        this.slot1 = Objects.requireNonNull(slot1, "slot1");
        this.slot2 = Objects.requireNonNull(slot2, "slot2");

        if (slot1.equals(slot2)) {
            throw new BusinessException(ErrorCode.POOL_MATCH_SAME_PARTICIPANT);
        }
        if (matchOrderInPool < 1 || matchOrderInPool > 3) {
            throw new IllegalArgumentException("matchOrderInPool must be 1-3");
        }
        this.matchOrderInPool = matchOrderInPool;
        this.status = Status.PENDING;
    }

    /** Constructeur de reconstruction depuis la base (avec id). */
    public PoolMatch(String id, PoolSlot slot1, PoolSlot slot2,
            int matchOrderInPool, Status status,
            PoolMatchScore score, String walkoverId) {

        this.id = Objects.requireNonNull(id, "id");
        this.slot1 = Objects.requireNonNull(slot1, "slot1");
        this.slot2 = Objects.requireNonNull(slot2, "slot2");
        this.matchOrderInPool = matchOrderInPool;
        this.status = Objects.requireNonNull(status, "status");
        this.score = score;
        this.walkoverId = walkoverId;
    }

    // -------------------------------------------------------------------------
    // TRANSITIONS
    // -------------------------------------------------------------------------

    /**
     * Démarre le match (optionnel : permet l'affichage "en cours" sur l'UI).
     */
    public void start() {
        if (status != Status.PENDING) {
            throw new BusinessException(ErrorCode.POOL_MATCH_INVALID_TRANSITION);
        }
        this.status = Status.IN_PROGRESS;
    }

    /**
     * Enregistre le score du match et le passe à COMPLETED.
     */
    public void recordScore(PoolMatchScore score) {
        if (status == Status.COMPLETED || status == Status.WALKOVER) {
            throw new BusinessException(ErrorCode.POOL_MATCH_ALREADY_FINISHED);
        }
        this.score = Objects.requireNonNull(score, "score");
        this.status = Status.COMPLETED;
    }

    /**
     * Déclare un forfait / abandon pour le joueur donné.
     * Règle FFTT : le joueur forfait marque 0 point-partie,
     * ses résultats dans la poule sont annulés (géré dans PoolStanding).
     *
     * @param participant le joueur qui déclare forfait
     */
    public void declareWalkover(Participant participant) {
        Objects.requireNonNull(participant, "participant");
        if (status == Status.COMPLETED || status == Status.WALKOVER) {
            throw new BusinessException(ErrorCode.POOL_MATCH_ALREADY_FINISHED);
        }
        if (!slot1.participant().equals(participant)
                && !slot2.participant().equals(participant)) {
            throw new BusinessException(ErrorCode.POOL_MATCH_PARTICIPANT_NOT_IN_MATCH);
        }
        this.walkoverId = participant.participantId();
        this.status = Status.WALKOVER;
    }

    // -------------------------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------------------------

    public boolean isFinished() {
        return status == Status.COMPLETED || status == Status.WALKOVER;
    }

    /**
     * Retourne le vainqueur du match.
     * Null si le match n'est pas terminé.
     */
    public Participant winner() {
        return switch (status) {
            case COMPLETED -> score.player1Wins()
                    ? slot1.participant()
                    : slot2.participant();
            case WALKOVER -> walkoverId.equals(slot1.participant().participantId())
                    ? slot2.participant()
                    : slot1.participant();
            default -> null;
        };
    }

    /**
     * Retourne le perdant du match.
     * Null si le match n'est pas terminé.
     */
    public Participant loser() {
        return switch (status) {
            case COMPLETED -> score.player1Wins()
                    ? slot2.participant()
                    : slot1.participant();
            case WALKOVER -> walkoverId.equals(slot1.participant().participantId())
                    ? slot1.participant()
                    : slot2.participant();
            default -> null;
        };
    }

    /** Vrai si le participant donné a déclaré forfait dans ce match. */
    public boolean isWalkoverFor(Participant p) {
        return status == Status.WALKOVER && walkoverId.equals(p.participantId());
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public String id() {
        return id;
    }

    public PoolSlot slot1() {
        return slot1;
    }

    public PoolSlot slot2() {
        return slot2;
    }

    public int matchOrderInPool() {
        return matchOrderInPool;
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
    // EQUALS (identité métier = id)
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PoolMatch other))
            return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "PoolMatch{%s vs %s, order=%d, status=%s}".formatted(
                slot1.participant().participantId(), slot2.participant().participantId(),
                matchOrderInPool, status);
    }
}