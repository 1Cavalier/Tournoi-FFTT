package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums;

/**
 * Balles de tennis de table 3 étoiles homologuées FFTT pour les tournois
 * officiels.
 *
 * Source : liste indicative des balles agréées ITTF / FFTT couramment utilisées
 * en compétition départementale, régionale et nationale.
 *
 * La valeur AUTRE permet de saisir librement une balle non listée ici,
 * par exemple une nouvelle homologation ou une balle spécifique à un niveau
 * international.
 *
 * Règle FFTT : toute balle utilisée en tournoi homologué doit être approuvée
 * par l'ITTF. La liste ci-dessous couvre les références les plus répandues
 * en France au moment de l'écriture de cette version.
 */
public enum ApprovedBall {

    // -------------------------------------------------------------------------
    // BUTTERFLY
    // -------------------------------------------------------------------------
    BUTTERFLY_R40_PLUS_WHITE("Butterfly R40+", "Blanche", "Butterfly"),
    BUTTERFLY_R40_PLUS_ORANGE("Butterfly R40+", "Orange", "Butterfly"),
    BUTTERFLY_G40_PLUS_WHITE("Butterfly G40+", "Blanche", "Butterfly"),
    BUTTERFLY_G40_PLUS_ORANGE("Butterfly G40+", "Orange", "Butterfly"),

    // -------------------------------------------------------------------------
    // NITTAKU
    // -------------------------------------------------------------------------
    NITTAKU_PREMIUM_3STAR_WHITE("Nittaku Premium 3 étoiles", "Blanche", "Nittaku"),
    NITTAKU_PREMIUM_3STAR_ORANGE("Nittaku Premium 3 étoiles", "Orange", "Nittaku"),

    // -------------------------------------------------------------------------
    // DHS
    // -------------------------------------------------------------------------
    DHS_D40_PLUS_WHITE("DHS D40+", "Blanche", "DHS"),
    DHS_D40_PLUS_ORANGE("DHS D40+", "Orange", "DHS"),

    // -------------------------------------------------------------------------
    // STIGA
    // -------------------------------------------------------------------------
    STIGA_OPTIMUM_40_PLUS_WHITE("Stiga Optimum 40+", "Blanche", "Stiga"),
    STIGA_OPTIMUM_40_PLUS_ORANGE("Stiga Optimum 40+", "Orange", "Stiga"),

    // -------------------------------------------------------------------------
    // DONIC
    // -------------------------------------------------------------------------
    DONIC_P40_PLUS_WHITE("Donic P40+", "Blanche", "Donic"),
    DONIC_P40_PLUS_ORANGE("Donic P40+", "Orange", "Donic"),

    // -------------------------------------------------------------------------
    // XIOM
    // -------------------------------------------------------------------------
    XIOM_V40_PLUS_WHITE("Xiom V40+", "Blanche", "Xiom"),
    XIOM_V40_PLUS_ORANGE("Xiom V40+", "Orange", "Xiom"),

    // -------------------------------------------------------------------------
    // TIBHAR
    // -------------------------------------------------------------------------
    TIBHAR_SYNTT_NG_WHITE("Tibhar Syntt NG", "Blanche", "Tibhar"),
    TIBHAR_SYNTT_NG_ORANGE("Tibhar Syntt NG", "Orange", "Tibhar"),

    // -------------------------------------------------------------------------
    // AUTRE — saisie libre
    // -------------------------------------------------------------------------
    AUTRE("Autre", "", "");

    // -------------------------------------------------------------------------

    private final String modelName;
    private final String color;
    private final String brand;

    ApprovedBall(String modelName, String color, String brand) {
        this.modelName = modelName;
        this.color = color;
        this.brand = brand;
    }

    public String modelName() {
        return modelName;
    }

    public String color() {
        return color;
    }

    public String brand() {
        return brand;
    }

    /**
     * Libellé affiché dans l'interface.
     * Ex : "Butterfly R40+ — Blanche"
     * Pour AUTRE : "Autre (saisie libre)"
     */
    public String label() {
        if (this == AUTRE)
            return "Autre (saisie libre)";
        if (color.isBlank())
            return modelName;
        return modelName + " — " + color;
    }

    /**
     * Retourne true si ce choix nécessite une saisie libre de la part de
     * l'utilisateur.
     */
    public boolean requiresCustomInput() {
        return this == AUTRE;
    }
}