package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool;

import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.Objects;

/**
 * Value object calculé représentant le classement d'un joueur dans sa poule.
 *
 * Calculé par Poule#computeStandings() à partir des matchs terminés.
 *
 * Système de points FFTT (article I.305 des Règlements Sportifs) :
 * Victoire → 2 points-parties
 * Défaite → 1 point-partie
 * Walkover → 0 point-partie (et ses résultats sont annulés pour les autres)
 *
 * Départage (en cas d'égalité de points-parties entre N joueurs) :
 * 1. Résultats des matchs entre les joueurs ex-aequo seulement
 * 2. Quotient manches gagnées / manches perdues (entre les ex-aequo)
 * 3. Quotient points-jeu gagnés / points-jeu perdus (entre les ex-aequo)
 * 4. Tirage au sort (non géré ici : laisser au JA)
 *
 * Immuable.
 */
public final class PoolStanding implements Comparable<PoolStanding> {

    private final Participant participant;

    /** Points-parties accumulés (victoires × 2 + défaites × 1, walkover = 0). */
    private final int matchPoints;

    /** Nombre de matchs gagnés. */
    private final int matchesWon;

    /** Nombre de matchs perdus. */
    private final int matchesLost;

    /**
     * Vrai si le joueur a déclaré au moins un forfait → ses résultats sont annulés.
     */
    private final boolean hasWalkover;

    /** Manches gagnées (toutes parties jouées). */
    private final int setsWon;

    /** Manches perdues (toutes parties jouées). */
    private final int setsLost;

    /** Points-jeu marqués (toutes parties jouées). */
    private final int pointsWon;

    /** Points-jeu concédés (toutes parties jouées). */
    private final int pointsLost;

    /**
     * Rang final dans la poule (1 = meilleur).
     * Positionné à 0 tant que le classement n'est pas finalisé.
     */
    private final int rank;

    // -------------------------------------------------------------------------
    // CONSTRUCTEUR
    // -------------------------------------------------------------------------

    public PoolStanding(
            Participant participant,
            int matchPoints,
            int matchesWon,
            int matchesLost,
            boolean hasWalkover,
            int setsWon,
            int setsLost,
            int pointsWon,
            int pointsLost,
            int rank) {

        this.participant = Objects.requireNonNull(participant, "participant");
        this.matchPoints = matchPoints;
        this.matchesWon = matchesWon;
        this.matchesLost = matchesLost;
        this.hasWalkover = hasWalkover;
        this.setsWon = setsWon;
        this.setsLost = setsLost;
        this.pointsWon = pointsWon;
        this.pointsLost = pointsLost;
        this.rank = rank;
    }

    // -------------------------------------------------------------------------
    // QUERIES UTILES
    // -------------------------------------------------------------------------

    /**
     * Quotient manches gagnées / perdues.
     * Utilisé au 2e critère de départage.
     * Retourne Double.MAX_VALUE si setsLost == 0 (protection contre division par
     * 0).
     */
    public double setsQuotient() {
        if (setsLost == 0)
            return Double.MAX_VALUE;
        return (double) setsWon / setsLost;
    }

    /**
     * Quotient points-jeu gagnés / perdus.
     * Utilisé au 3e critère de départage.
     * Retourne Double.MAX_VALUE si pointsLost == 0.
     */
    public double pointsQuotient() {
        if (pointsLost == 0)
            return Double.MAX_VALUE;
        return (double) pointsWon / pointsLost;
    }

    /**
     * Indique si le joueur est qualifié pour le tableau KO.
     *
     * @param qualifiedCount nombre de qualifiés de la poule
     *                       (1 pour une poule de 2, 2 pour une poule de 3)
     */
    public boolean isQualified(int qualifiedCount) {
        return !hasWalkover && rank >= 1 && rank <= qualifiedCount;
    }

    // -------------------------------------------------------------------------
    // COMPARABLE (tri naturel : du meilleur au moins bon)
    // -------------------------------------------------------------------------

    /**
     * Comparaison par rang (1 avant 2 avant 3).
     * Utilisé pour trier la liste des standings d'une poule.
     */
    @Override
    public int compareTo(PoolStanding other) {
        return Integer.compare(this.rank, other.rank);
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public Participant participant() {
        return participant;
    }

    public int matchPoints() {
        return matchPoints;
    }

    public int matchesWon() {
        return matchesWon;
    }

    public int matchesLost() {
        return matchesLost;
    }

    public boolean hasWalkover() {
        return hasWalkover;
    }

    public int setsWon() {
        return setsWon;
    }

    public int setsLost() {
        return setsLost;
    }

    public int pointsWon() {
        return pointsWon;
    }

    public int pointsLost() {
        return pointsLost;
    }

    public int rank() {
        return rank;
    }

    // -------------------------------------------------------------------------
    // EQUALS / HASHCODE (identité = participant)
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PoolStanding other))
            return false;
        return participant.equals(other.participant);
    }

    @Override
    public int hashCode() {
        return participant.hashCode();
    }

    @Override
    public String toString() {
        return "PoolStanding{rank=%d, pts=%d, %s}".formatted(
                rank, matchPoints, participant.participantId());
    }
}