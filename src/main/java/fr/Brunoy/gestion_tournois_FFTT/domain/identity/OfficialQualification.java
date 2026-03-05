package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.*;

import java.util.Objects;

public final class OfficialQualification {

    private final OfficialRoleType roleType;

    private final RefereeGrade refereeGrade;
    private final JudgeRefereeGrade judgeRefereeGrade;
    private final TechnicalGrade technicalGrade;

    private OfficialQualification(
            OfficialRoleType roleType,
            RefereeGrade refereeGrade,
            JudgeRefereeGrade judgeRefereeGrade,
            TechnicalGrade technicalGrade) {

        if (roleType == null) {
            throw new BusinessException(ErrorCode.OFFICIAL_ROLE_REQUIRED);
        }
        this.roleType = roleType;
        this.refereeGrade = refereeGrade;
        this.judgeRefereeGrade = judgeRefereeGrade;
        this.technicalGrade = technicalGrade;

        validateInvariants();
    }

    private void validateInvariants() {
        switch (roleType) {
            case ARBITRE -> {
                if (refereeGrade == null || judgeRefereeGrade != null || technicalGrade != null) {
                    throw new BusinessException(ErrorCode.OFFICIAL_INCONSISTENT_GRADE);
                }
            }
            case JUGE_ARBITRE -> {
                if (judgeRefereeGrade == null || refereeGrade != null || technicalGrade != null) {
                    throw new BusinessException(ErrorCode.OFFICIAL_INCONSISTENT_GRADE);
                }
            }
            case TECHNIQUE -> {
                if (technicalGrade == null || refereeGrade != null || judgeRefereeGrade != null) {
                    throw new BusinessException(ErrorCode.OFFICIAL_INCONSISTENT_GRADE);
                }
            }
        }
    }

    // ---------- Factories ----------

    public static OfficialQualification referee(RefereeGrade grade) {
        if (grade == null)
            throw new BusinessException(ErrorCode.OFFICIAL_GRADE_REQUIRED);
        return new OfficialQualification(OfficialRoleType.ARBITRE, grade, null, null);
    }

    public static OfficialQualification judgeReferee(JudgeRefereeGrade grade) {
        if (grade == null)
            throw new BusinessException(ErrorCode.OFFICIAL_GRADE_REQUIRED);
        return new OfficialQualification(OfficialRoleType.JUGE_ARBITRE, null, grade, null);
    }

    public static OfficialQualification technical(TechnicalGrade grade) {
        if (grade == null)
            throw new BusinessException(ErrorCode.OFFICIAL_GRADE_REQUIRED);
        return new OfficialQualification(OfficialRoleType.TECHNIQUE, null, null, grade);
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