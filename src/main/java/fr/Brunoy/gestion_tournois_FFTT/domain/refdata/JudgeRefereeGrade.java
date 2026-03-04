package fr.Brunoy.gestion_tournois_FFTT.domain.refdata;

/**
 * Grades de juge-arbitre FFTT.
 *
 * JA1 et JA2 sont indépendants.
 * JA3 est supérieur aux deux.
 * JAN est supérieur à JA3.
 * JAI est supérieur à JAN.
 */
public enum JudgeRefereeGrade {

    JA1,
    JA2,
    JA3,
    JAN,
    JAI;

    /**
     * Vérifie si ce grade permet d'assurer un tournoi
     * nécessitant au minimum le grade requis.
     */
    public boolean qualifiesFor(JudgeRefereeGrade required) {

        if (required == null)
            return true;

        return switch (required) {

            case JA1 -> this == JA1 || this == JA3 || this == JAN || this == JAI;

            case JA2 -> this == JA2 || this == JA3 || this == JAN || this == JAI;

            case JA3 -> this == JA3 || this == JAN || this == JAI;

            case JAN -> this == JAN || this == JAI;

            case JAI -> this == JAI;
        };
    }

}