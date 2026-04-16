package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.value;

import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Player;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.JudgeRefereeGrade;

/**
 * Affectation du Juge-Arbitre (JA) au tournoi.
 * Value Object : immutable.
 * Identité métier : numéro de licence (Player).
 */
public final class JudgeRefereeAssignment {

    private final Player judgeReferee;
    private final JudgeRefereeGrade grade;

    public JudgeRefereeAssignment(Player judgeReferee, JudgeRefereeGrade grade) {
        if (judgeReferee == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_JA_REQUIRED);
        }
        if (isBlank(judgeReferee.getLicenseNumber())) {
            throw new BusinessException(ErrorCode.PLAYER_LICENSE_REQUIRED);
        }
        if (grade == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_JA_GRADE_REQUIRED);
        }

        this.judgeReferee = judgeReferee;
        this.grade = grade;
    }

    public Player judgeReferee() {
        return judgeReferee;
    }

    public JudgeRefereeGrade grade() {
        return grade;
    }

    private String licenseKey() {
        return judgeReferee.getLicenseNumber().trim().toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof JudgeRefereeAssignment that))
            return false;
        return this.licenseKey().equals(that.licenseKey())
                && grade == that.grade;
    }

    @Override
    public int hashCode() {
        return Objects.hash(licenseKey(), grade);
    }

    @Override
    public String toString() {
        String name = judgeReferee.getFullName();
        if (isBlank(name)) {
            name = "Licence " + judgeReferee.getLicenseNumber();
        }
        return "JA=" + name + " (" + grade + ")";
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}