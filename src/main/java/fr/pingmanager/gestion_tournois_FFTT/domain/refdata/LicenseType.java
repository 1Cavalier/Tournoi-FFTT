package fr.pingmanager.gestion_tournois_FFTT.domain.refdata;

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

    public boolean allowsCompetition() {
        return this == COMPETITION;
    }
}