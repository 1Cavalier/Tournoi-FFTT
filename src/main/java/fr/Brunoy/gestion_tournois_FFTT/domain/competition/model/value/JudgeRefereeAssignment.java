package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.Player;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.JudgeRefereeGrade;

import java.util.Objects;

/**
 * Affectation du Juge-Arbitre (JA) au tournoi.
 * Value Object : immutable.
 */
public final class JudgeRefereeAssignment {

    private final Player judgeReferee; // identité
    private final JudgeRefereeGrade grade; // grade JA déclaré

    public JudgeRefereeAssignment(Player judgeReferee, JudgeRefereeGrade grade) {
        if (judgeReferee == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_JA_REQUIRED);
        if (grade == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_JA_GRADE_REQUIRED);

        this.judgeReferee = judgeReferee;
        this.grade = grade;
    }

    public Player judgeReferee() {
        return judgeReferee;
    }

    public JudgeRefereeGrade grade() {
        return grade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof JudgeRefereeAssignment that))
            return false;
        return judgeReferee.equals(that.judgeReferee) && grade == that.grade;
    }

    @Override
    public int hashCode() {
        return Objects.hash(judgeReferee, grade);
    }

    @Override
    public String toString() {
        return "JA=" + judgeReferee.getFullName() + " (" + grade + ")";
    }
}