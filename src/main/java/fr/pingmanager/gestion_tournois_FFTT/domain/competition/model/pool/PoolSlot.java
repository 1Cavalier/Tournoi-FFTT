package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool;

import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.Objects;

/**
 * Value object représentant la place d'un joueur dans une poule.
 *
 * Contient :
 * - le participant (FFTT / guest / foreign)
 * - son rang global dans le tirage serpent (1 = meilleur classé de tout le
 * tableau)
 * - sa position dans la poule (1, 2 ou 3) qui détermine l'ordre des matchs
 *
 * Immuable : une fois la poule tirée, les slots ne changent pas.
 */
public final class PoolSlot {

    /** Rang global dans le tirage (1 = tête de série n°1 du tableau). */
    private final int seedRank;

    /** Position dans la poule : 1, 2 ou 3. Détermine l'ordre des matchs. */
    private final int positionInPool;

    private final Participant participant;

    // -------------------------------------------------------------------------
    // CONSTRUCTEUR
    // -------------------------------------------------------------------------

    public PoolSlot(int seedRank, int positionInPool, Participant participant) {
        if (seedRank < 1) {
            throw new IllegalArgumentException("seedRank must be >= 1, got: " + seedRank);
        }
        if (positionInPool < 1 || positionInPool > 4) {
            // on tolère 4 pour les poules de 4 éventuelles plus tard
            throw new IllegalArgumentException("positionInPool must be 1-4, got: " + positionInPool);
        }
        this.seedRank = seedRank;
        this.positionInPool = positionInPool;
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public int seedRank() {
        return seedRank;
    }

    public int positionInPool() {
        return positionInPool;
    }

    public Participant participant() {
        return participant;
    }

    // -------------------------------------------------------------------------
    // EQUALS / HASHCODE (identité = participant)
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PoolSlot other))
            return false;
        return participant.equals(other.participant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participant);
    }

    @Override
    public String toString() {
        return "PoolSlot{seed=%d, pos=%d, participant=%s}".formatted(
                seedRank, positionInPool, participant.participantId());
    }
}