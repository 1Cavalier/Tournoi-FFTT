package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums;

import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.JudgeRefereeGrade;

/**
 * Niveaux de tournois homologués FFTT.
 *
 * Source : Circulaire administrative et financière FFTT 2025/2026.
 */
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

    /**
     * Libellé officiel affiché dans l'interface.
     */
    public String label() {
        return switch (this) {
            case DEPARTEMENTAL -> "Départemental";
            case REGIONAL -> "Régional";
            case NATIONAL_B -> "National B";
            case NATIONAL_A -> "National A";
            case INTERNATIONAL -> "International";
        };
    }
}