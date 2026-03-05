package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfficialQualificationTest {

    @Test
    void factoriesShouldSetRoleAndGrade() {
        OfficialQualification q1 = OfficialQualification.referee(RefereeGrade.REGIONAL);
        assertEquals(OfficialRoleType.ARBITRE, q1.getRoleType());
        assertTrue(q1.isReferee());
        assertEquals(RefereeGrade.REGIONAL, q1.getRefereeGrade());

        OfficialQualification q2 = OfficialQualification.judgeReferee(JudgeRefereeGrade.JA3);
        assertEquals(OfficialRoleType.JUGE_ARBITRE, q2.getRoleType());
        assertTrue(q2.isJudgeReferee());
        assertEquals(JudgeRefereeGrade.JA3, q2.getJudgeRefereeGrade());

        OfficialQualification q3 = OfficialQualification.technical(TechnicalGrade.BPJEPS);
        assertEquals(OfficialRoleType.TECHNIQUE, q3.getRoleType());
        assertTrue(q3.isTechnical());
        assertEquals(TechnicalGrade.BPJEPS, q3.getTechnicalGrade());
    }

    @Test
    void nullGradeShouldThrow() {
        BusinessException ex = assertThrows(BusinessException.class, () -> OfficialQualification.judgeReferee(null));
        assertEquals(ErrorCode.OFFICIAL_GRADE_REQUIRED, ex.getCode());
    }

    @Test
    void equalsShouldBeValueBased() {
        OfficialQualification a = OfficialQualification.judgeReferee(JudgeRefereeGrade.JA2);
        OfficialQualification b = OfficialQualification.judgeReferee(JudgeRefereeGrade.JA2);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}