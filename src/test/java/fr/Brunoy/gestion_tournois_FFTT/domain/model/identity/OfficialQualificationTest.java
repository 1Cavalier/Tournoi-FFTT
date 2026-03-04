package fr.Brunoy.gestion_tournois_FFTT.domain.model.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.OfficialQualification;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.model.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.JudgeRefereeGrade;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.OfficialRoleType;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.RefereeGrade;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.TechnicalGrade;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.enums.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfficialQualificationTest {

    private void verifierErreurMetier(BusinessException ex, ErrorCode codeAttendu) {
        assertEquals(codeAttendu, ex.getCode());
    }

    // ---------- Créations OK ----------

    @Test
    void creationArbitre_quandGradeValide() {
        OfficialQualification q = OfficialQualification.referee(RefereeGrade.REGIONAL);

        assertEquals(OfficialRoleType.ARBITRE, q.getRoleType());
        assertEquals(RefereeGrade.REGIONAL, q.getRefereeGrade());
        assertNull(q.getJudgeRefereeGrade());
        assertNull(q.getTechnicalGrade());

        assertTrue(q.isReferee());
        assertFalse(q.isJudgeReferee());
        assertFalse(q.isTechnical());
    }

    @Test
    void creationJugeArbitre_quandGradeValide() {
        OfficialQualification q = OfficialQualification.judgeReferee(JudgeRefereeGrade.JA2);

        assertEquals(OfficialRoleType.JUGE_ARBITRE, q.getRoleType());
        assertEquals(JudgeRefereeGrade.JA2, q.getJudgeRefereeGrade());
        assertNull(q.getRefereeGrade());
        assertNull(q.getTechnicalGrade());

        assertFalse(q.isReferee());
        assertTrue(q.isJudgeReferee());
        assertFalse(q.isTechnical());
    }

    @Test
    void creationTechnique_quandGradeValide() {
        OfficialQualification q = OfficialQualification.technical(TechnicalGrade.BPJEPS);

        assertEquals(OfficialRoleType.TECHNIQUE, q.getRoleType());
        assertEquals(TechnicalGrade.BPJEPS, q.getTechnicalGrade());
        assertNull(q.getRefereeGrade());
        assertNull(q.getJudgeRefereeGrade());

        assertFalse(q.isReferee());
        assertFalse(q.isJudgeReferee());
        assertTrue(q.isTechnical());
    }

    // ---------- Erreurs (grade manquant) ----------

    @Test
    void erreur_siCreationArbitreAvecGradeNull() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> OfficialQualification.referee(null));

        verifierErreurMetier(ex, ErrorCode.OFFICIAL_GRADE_REQUIRED);
    }

    @Test
    void erreur_siCreationJugeArbitreAvecGradeNull() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> OfficialQualification.judgeReferee(null));

        verifierErreurMetier(ex, ErrorCode.OFFICIAL_GRADE_REQUIRED);
    }

    @Test
    void erreur_siCreationTechniqueAvecGradeNull() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> OfficialQualification.technical(null));

        verifierErreurMetier(ex, ErrorCode.OFFICIAL_GRADE_REQUIRED);
    }

    // ---------- equals / hashCode ----------

    @Test
    void deuxQualificationsIdentiques_sontEgales() {
        OfficialQualification q1 = OfficialQualification.referee(RefereeGrade.NATIONAL);
        OfficialQualification q2 = OfficialQualification.referee(RefereeGrade.NATIONAL);

        assertEquals(q1, q2);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void deuxQualificationsDifferentes_neSontPasEgales() {
        OfficialQualification q1 = OfficialQualification.referee(RefereeGrade.NATIONAL);
        OfficialQualification q2 = OfficialQualification.referee(RefereeGrade.REGIONAL);

        assertNotEquals(q1, q2);
    }

    @Test
    void qualificationsDeRolesDifferents_neSontPasEgales() {
        OfficialQualification q1 = OfficialQualification.referee(RefereeGrade.CLUB);
        OfficialQualification q2 = OfficialQualification.judgeReferee(JudgeRefereeGrade.JA1);

        assertNotEquals(q1, q2);
    }

    // ---------- toString (facultatif mais pratique) ----------

    @Test
    void toString_contientLeRoleEtLeGrade() {
        OfficialQualification q = OfficialQualification.judgeReferee(JudgeRefereeGrade.JAN);
        String s = q.toString();

        assertTrue(s.contains("Juge-arbitre") || s.contains("Juge"));
        assertTrue(s.contains("JAN"));
    }
}
