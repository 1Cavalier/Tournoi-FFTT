package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums;

/**
 * Politique de genre d'un tableau de tournoi FFTT.
 *
 * MIXTE : ouvert à tous les joueurs sans restriction de genre.
 * FEMININ : réservé aux joueuses uniquement.
 * MASCULIN : réservé aux joueurs uniquement.
 * (certains tournois proposent des tableaux masculins séparés)
 */
public enum GenderPolicy {

    MIXTE,
    FEMININ,
    MASCULIN;

    public boolean isFemaleOnly() {
        return this == FEMININ;
    }

    public boolean isMaleOnly() {
        return this == MASCULIN;
    }

    /**
     * Libellé officiel affiché dans l'interface.
     */
    public String label() {
        return switch (this) {
            case MIXTE -> "Mixte";
            case FEMININ -> "Féminin uniquement";
            case MASCULIN -> "Masculin uniquement";
        };
    }
}