package fr.Brunoy.gestion_tournois_FFTT.domain.refdata;

/**
 * Type de licence FFTT.
 */
public enum LicenseType {

    COMPETITION,
    LOISIR,
    DIRIGEANT,
    DECOUVERTE,
    EVENEMENTIELLE,
    LIBERTE;

    /**
     * Indique si la licence permet de participer à une compétition FFTT.
     */
    public boolean allowsCompetition() {
        return this == COMPETITION;
    }
}