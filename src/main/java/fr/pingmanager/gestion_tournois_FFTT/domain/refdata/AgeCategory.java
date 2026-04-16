package fr.pingmanager.gestion_tournois_FFTT.domain.refdata;

/**
 * Catégories d'âge officielles FFTT.
 *
 * Source : Circulaire administrative et financière FFTT 2025/2026.
 *
 * Les libellés affichés via label() correspondent à la nomenclature officielle.
 * Les tranches d'années de naissance ne sont pas incluses car elles changent
 * chaque saison — seule la catégorie fait foi dans les règlements de tournoi.
 *
 * L'ordre de déclaration (ordinal) est utilisé dans AgeCategoryPolicy.range()
 * pour comparer les catégories : POUSSIN(0) < BENJAMIN_1(1) < ... <
 * VETERAN_90(22).
 */
public enum AgeCategory {

    // -----------------------------------------------------------------------
    // JEUNES
    // -----------------------------------------------------------------------

    POUSSIN,

    BENJAMIN_1,
    BENJAMIN_2,

    MINIME_1,
    MINIME_2,

    CADET_1,
    CADET_2,

    JUNIOR_1,
    JUNIOR_2,
    JUNIOR_3,
    JUNIOR_4,

    // -----------------------------------------------------------------------
    // ADULTES
    // -----------------------------------------------------------------------

    SENIOR,

    // -----------------------------------------------------------------------
    // VÉTÉRANS — tranches de 5 ans depuis 40 ans
    // -----------------------------------------------------------------------

    VETERAN_40,
    VETERAN_45,
    VETERAN_50,
    VETERAN_55,
    VETERAN_60,
    VETERAN_65,
    VETERAN_70,
    VETERAN_75,
    VETERAN_80,
    VETERAN_85,
    VETERAN_90;

    // -----------------------------------------------------------------------
    // HELPERS MÉTIER
    // -----------------------------------------------------------------------

    public boolean isVeteran() {
        return name().startsWith("VETERAN_");
    }

    public boolean isSenior() {
        return this == SENIOR;
    }

    public boolean isJunior() {
        return name().startsWith("JUNIOR_");
    }

    public boolean isCadet() {
        return name().startsWith("CADET_");
    }

    public boolean isMinime() {
        return name().startsWith("MINIME_");
    }

    public boolean isBenjamin() {
        return name().startsWith("BENJAMIN_");
    }

    public boolean isPoussin() {
        return this == POUSSIN;
    }

    /**
     * Libellé officiel FFTT affiché dans l'interface.
     */
    public String label() {
        return switch (this) {
            case POUSSIN -> "Poussin (- 9 ans)";
            case BENJAMIN_1 -> "Benjamin 1";
            case BENJAMIN_2 -> "Benjamin 2";
            case MINIME_1 -> "Minime 1";
            case MINIME_2 -> "Minime 2";
            case CADET_1 -> "Cadet 1";
            case CADET_2 -> "Cadet 2";
            case JUNIOR_1 -> "Junior 1";
            case JUNIOR_2 -> "Junior 2";
            case JUNIOR_3 -> "Junior 3";
            case JUNIOR_4 -> "Junior 4";
            case SENIOR -> "Senior";
            case VETERAN_40 -> "Vétéran 40";
            case VETERAN_45 -> "Vétéran 45";
            case VETERAN_50 -> "Vétéran 50";
            case VETERAN_55 -> "Vétéran 55";
            case VETERAN_60 -> "Vétéran 60";
            case VETERAN_65 -> "Vétéran 65";
            case VETERAN_70 -> "Vétéran 70";
            case VETERAN_75 -> "Vétéran 75";
            case VETERAN_80 -> "Vétéran 80";
            case VETERAN_85 -> "Vétéran 85";
            case VETERAN_90 -> "Vétéran 90";
        };
    }
}