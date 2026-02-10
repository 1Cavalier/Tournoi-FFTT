package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums;

public enum GenderPolicy {
    MIXTE,
    FEMININ_ONLY;

    public boolean isFemaleOnly() {
        return this == FEMININ_ONLY;
    }
}
