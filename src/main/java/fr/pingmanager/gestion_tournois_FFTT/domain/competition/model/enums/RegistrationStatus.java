package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums;

/**
 * Statut d'une inscription à un tableau.
 *
 * - WAITLISTED : inscrit en file d'attente (ne bloque pas de place).
 * - RESERVED : place temporairement bloquée (paiement en attente, peut
 * expirer).
 * - CONFIRMED : inscription validée (place prise).
 * - CANCELLED : inscription annulée (final).
 *
 * NB : "Active" au sens métier est géré par Registration#isActiveAt(now)
 * (car RESERVED dépend du temps).
 */
public enum RegistrationStatus {

    WAITLISTED,
    RESERVED,
    CONFIRMED,
    CANCELLED;

    /** Indique si l'inscription bloque une place dans le tableau. */
    public boolean blocksSpot() {
        return this == RESERVED || this == CONFIRMED;
    }

    /** Indique si l'inscription est en file d'attente (ne bloque pas de place). */
    public boolean isWaitlist() {
        return this == WAITLISTED;
    }

    /** Indique si le statut correspond à une place "prise/bloquée". */
    public boolean isSpotStatus() {
        return this == RESERVED || this == CONFIRMED;
    }

    /**
     * Indique si l'inscription n'a pas été explicitement annulée.
     * Attention : RESERVED peut être "vivant" mais expiré => utiliser
     * Registration#isActiveAt.
     */
    public boolean isAlive() {
        return this != CANCELLED;
    }

    /** Indique si l'inscription est définitivement terminée. */
    public boolean isFinal() {
        return this == CANCELLED;
    }
}