package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums;

/**
 * Statut d'une inscription à un tableau.
 *
 * WAITLISTED : inscrit en file d'attente (ne bloque pas de place).
 * RESERVED : place temporairement bloquée (paiement en attente).
 * CONFIRMED : inscription validée (place prise).
 * CANCELLED : inscription annulée.
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

    /** Indique si l'inscription est "vivante" (active) au sens large. */
    public boolean isAlive() {
        return this != CANCELLED;
    }

    /** Indique si l'inscription est définitivement terminée. */
    public boolean isFinal() {
        return this == CANCELLED;
    }
}