package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.JudgeRefereeGrade;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.OfficialRoleType;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.RefereeGrade;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.TechnicalGrade;

import java.util.Objects;

public class OfficialQualification {

    private final OfficialRoleType roleType;

    private final RefereeGrade refereeGrade;
    private final JudgeRefereeGrade judgeRefereeGrade;
    private final TechnicalGrade technicalGrade;

    private OfficialQualification(
            OfficialRoleType roleType,
            RefereeGrade refereeGrade,
            JudgeRefereeGrade judgeRefereeGrade,
            TechnicalGrade technicalGrade) {
        this.roleType = roleType;
        this.refereeGrade = refereeGrade;
        this.judgeRefereeGrade = judgeRefereeGrade;
        this.technicalGrade = technicalGrade;
    }

    // ---------- Factories ----------

    public static OfficialQualification referee(RefereeGrade grade) {
        if (grade == null)
            throw new BusinessException(ErrorCode.OFFICIAL_GRADE_REQUIRED);

        return new OfficialQualification(
                OfficialRoleType.ARBITRE,
                grade,
                null,
                null);
    }

    public static OfficialQualification judgeReferee(JudgeRefereeGrade grade) {
        if (grade == null)
            throw new BusinessException(ErrorCode.OFFICIAL_GRADE_REQUIRED);

        return new OfficialQualification(
                OfficialRoleType.JUGE_ARBITRE,
                null,
                grade,
                null);
    }

    public static OfficialQualification technical(TechnicalGrade grade) {
        if (grade == null)
            throw new BusinessException(ErrorCode.OFFICIAL_GRADE_REQUIRED);

        return new OfficialQualification(
                OfficialRoleType.TECHNIQUE,
                null,
                null,
                grade);
    }

    // ---------- Getters ----------

    public OfficialRoleType getRoleType() {
        return roleType;
    }

    public RefereeGrade getRefereeGrade() {
        return refereeGrade;
    }

    public JudgeRefereeGrade getJudgeRefereeGrade() {
        return judgeRefereeGrade;
    }

    public TechnicalGrade getTechnicalGrade() {
        return technicalGrade;
    }

    public boolean isReferee() {
        return roleType == OfficialRoleType.ARBITRE;
    }

    public boolean isJudgeReferee() {
        return roleType == OfficialRoleType.JUGE_ARBITRE;
    }

    public boolean isTechnical() {
        return roleType == OfficialRoleType.TECHNIQUE;
    }

    @Override
    public String toString() {
        return switch (roleType) {
            case ARBITRE -> "Arbitre (" + refereeGrade + ")";
            case JUGE_ARBITRE -> "Juge-arbitre (" + judgeRefereeGrade + ")";
            case TECHNIQUE -> "Technique (" + technicalGrade + ")";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OfficialQualification q))
            return false;
        return roleType == q.roleType
                && refereeGrade == q.refereeGrade
                && judgeRefereeGrade == q.judgeRefereeGrade
                && technicalGrade == q.technicalGrade;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleType, refereeGrade, judgeRefereeGrade, technicalGrade);
    }
}
