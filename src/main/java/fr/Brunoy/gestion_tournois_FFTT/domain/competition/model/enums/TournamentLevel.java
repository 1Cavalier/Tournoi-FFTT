package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums;

import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.JudgeRefereeGrade;

public enum TournamentLevel {
    DEPARTEMENTAL,
    REGIONAL,
    NATIONAL_B,
    NATIONAL_A,
    INTERNATIONAL;

    public JudgeRefereeGrade requiredJudgeRefereeGrade() {
        return switch (this) {
            case DEPARTEMENTAL, REGIONAL, NATIONAL_B -> JudgeRefereeGrade.JA3;
            case NATIONAL_A, INTERNATIONAL -> JudgeRefereeGrade.JAN;
        };
    }
}
