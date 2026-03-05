package fr.Brunoy.gestion_tournois_FFTT.domain.refdata;

public enum Gender {
    MALE,
    FEMALE;

    public boolean isFemale() {
        return this == FEMALE;
    }
}