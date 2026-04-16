package fr.pingmanager.gestion_tournois_FFTT.domain.refdata;

/**
 * Phase de comptage des points FFTT.
 *
 * PHASE_1 : octobre → fin décembre (points de début de saison)
 * PHASE_2 : janvier → fin juillet (points officiels de saison)
 *
 * La phase détermine quels points de classement sont utilisés
 * pour l'éligibilité des joueurs aux tableaux.
 */
public enum RankingPhase {

    PHASE_1,
    PHASE_2;

    public boolean isPhase1() {
        return this == PHASE_1;
    }

    public boolean isPhase2() {
        return this == PHASE_2;
    }

    /**
     * Libellé officiel affiché dans l'interface.
     */
    public String label() {
        return switch (this) {
            case PHASE_1 -> "Phase 1 (octobre → décembre)";
            case PHASE_2 -> "Phase 2 (janvier → juillet)";
        };
    }
}