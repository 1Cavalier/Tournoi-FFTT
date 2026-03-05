package fr.Brunoy.gestion_tournois_FFTT.domain.refdata;

/**
 * Grades de juge-arbitre FFTT.
 *
 * JA1 et JA2 sont indépendants.
 * JA3 couvre JA1 et JA2.
 * JAN couvre JA3.
 * JAI couvre JAN.
 */
public enum JudgeRefereeGrade {

    JA1(1),
    JA2(1),
    JA3(2),
    JAN(3),
    JAI(4);

    private final int level;

    JudgeRefereeGrade(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    /**
     * Vérifie si ce grade permet d'assurer un tournoi nécessitant au minimum
     * le grade requis.
     */
    public boolean qualifiesFor(JudgeRefereeGrade required) {
        if (required == null)
            return true;

        // Cas particulier : JA1 et JA2 sont parallèles
        if (required == JA1) {
            return this == JA1 || this.level >= JA3.level;
        }
        if (required == JA2) {
            return this == JA2 || this.level >= JA3.level;
        }

        // Pour JA3, JAN, JAI : ordre simple
        return this.level >= required.level;
    }
}