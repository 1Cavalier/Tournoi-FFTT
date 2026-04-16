package fr.pingmanager.gestion_tournois_FFTT.domain.refdata;

public enum RefereeGrade {

    CLUB(1),
    REGIONAL(2),
    NATIONAL(3),
    INTERNATIONAL(4),
    INTERNATIONAL_BLUE_BADGE(5);

    private final int level;

    RefereeGrade(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public boolean qualifiesFor(RefereeGrade required) {
        if (required == null)
            return true;
        return this.level >= required.level;
    }
}