package fr.Brunoy.gestion_tournois_FFTT.domain.refdata;

public enum RankingPhase {
    PHASE_1,
    PHASE_2;

    public boolean isPhase1() {
        return this == PHASE_1;
    }

    public boolean isPhase2() {
        return this == PHASE_2;
    }
}