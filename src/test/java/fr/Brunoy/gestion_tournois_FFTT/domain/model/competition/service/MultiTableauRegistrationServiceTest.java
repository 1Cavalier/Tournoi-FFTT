package fr.Brunoy.gestion_tournois_FFTT.domain.model.competition.service;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.service.MultiTableauRegistrationService;
import fr.Brunoy.gestion_tournois_FFTT.domain.model.competition.TestFixtures;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tournament;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.GenderPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration.RegistrationDraft;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration.RegistrationSummary;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.model.Player;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MultiTableauRegistrationServiceTest {

    private final MultiTableauRegistrationService service = new MultiTableauRegistrationService();

    @Test
    void validate_ok_whenThreeTableauxSameDay_andPolicyIs3PerDay() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy3PerDay(day);

        // 3 tableaux le même jour
        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("B", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("C", day, GenderPolicy.MIXTE, 16));

        Player p = TestFixtures.playerMale(500, 500);

        RegistrationDraft draft = new RegistrationDraft(p);
        draft.addTableau("A");
        draft.addTableau("B");
        draft.addTableau("C");

        RegistrationSummary summary = service.validate(t, draft);

        assertTrue(summary.isValid(), "3 tableaux/jour doit être valide avec une policy à 3/jour");
        assertEquals(0, summary.violations().size());
    }

    @Test
    void validate_fails_whenFourTableauxSameDay_andPolicyIs3PerDay() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy3PerDay(day);

        // 4 tableaux le même jour
        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("B", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("C", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("D", day, GenderPolicy.MIXTE, 16));

        Player p = TestFixtures.playerMale(500, 500);

        RegistrationDraft draft = new RegistrationDraft(p);
        draft.addTableau("A");
        draft.addTableau("B");
        draft.addTableau("C");
        draft.addTableau("D");

        RegistrationSummary summary = service.validate(t, draft);

        assertFalse(summary.isValid(), "4 tableaux/jour doit être invalide avec une policy à 3/jour");
    }

    // (optionnel) test register() : 3 OK → ajoute 3 inscriptions
    @Test
    void register_ok_whenThreeTableauxSameDay() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy3PerDay(day);

        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("B", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("C", day, GenderPolicy.MIXTE, 16));

        Player p = TestFixtures.playerMale(500, 500);

        RegistrationDraft draft = new RegistrationDraft(p);
        draft.addTableau("A");
        draft.addTableau("B");
        draft.addTableau("C");

        service.register(t, draft);

        assertEquals(1, t.registrationsFor("A").size());
        assertEquals(1, t.registrationsFor("B").size());
        assertEquals(1, t.registrationsFor("C").size());
    }

    @Test
    void femaleExtra_NONE_femaleCannotDo4thTableau() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1Day3PerDay_FemaleNone(day);

        // 4 tableaux le même jour
        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("B", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("C", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("D", day, GenderPolicy.MIXTE, 16));

        Player female = TestFixtures.playerFemale(500, 500);

        RegistrationDraft draft = new RegistrationDraft(female);
        draft.addTableau("A");
        draft.addTableau("B");
        draft.addTableau("C");
        draft.addTableau("D");

        RegistrationSummary summary = service.validate(t, draft);

        assertFalse(summary.isValid());
        assertTrue(summary.violations().stream()
                .anyMatch(v -> v.getCode() == ErrorCode.REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED));
    }

    @Test
    void femaleExtra_ANY_TABLEAU_femaleCanDo4thTableau_butMaleCannot() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1Day3PerDay_FemaleAny(day);

        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("B", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("C", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("D", day, GenderPolicy.MIXTE, 16));

        // Femme : 4 doit passer
        Player female = TestFixtures.playerFemale(500, 500);
        RegistrationDraft draftF = new RegistrationDraft(female);
        draftF.addTableau("A");
        draftF.addTableau("B");
        draftF.addTableau("C");
        draftF.addTableau("D");

        RegistrationSummary sumF = service.validate(t, draftF);
        assertTrue(sumF.isValid(), "Avec ANY_TABLEAU, une féminine doit pouvoir faire +1 (4 au total)");

        // Homme : 4 doit échouer
        Player male = TestFixtures.playerMale(500, 500);
        RegistrationDraft draftM = new RegistrationDraft(male);
        draftM.addTableau("A");
        draftM.addTableau("B");
        draftM.addTableau("C");
        draftM.addTableau("D");

        RegistrationSummary sumM = service.validate(t, draftM);
        assertFalse(sumM.isValid(), "Un homme reste limité à 3/jour");
        assertTrue(sumM.violations().stream()
                .anyMatch(v -> v.getCode() == ErrorCode.REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED));
    }

    @Test
    void femaleExtra_SPECIFIC_F_onlyGivesExtraIfFSelected() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1Day3PerDay_FemaleSpecificF(day);

        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("B", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("C", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("D", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("F", day, GenderPolicy.FEMININ_ONLY, 16));

        Player female = TestFixtures.playerFemale(500, 500);

        // Cas 1: A,B,C,D (sans F) => KO
        RegistrationDraft noF = new RegistrationDraft(female);
        noF.addTableau("A");
        noF.addTableau("B");
        noF.addTableau("C");
        noF.addTableau("D");

        RegistrationSummary sumNoF = service.validate(t, noF);
        assertFalse(sumNoF.isValid(), "Sans le tableau spécifique F, pas de +1");
        assertTrue(sumNoF.violations().stream()
                .anyMatch(v -> v.getCode() == ErrorCode.REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED));

        // Cas 2: A,B,C,F => OK (+1 uniquement via F)
        RegistrationDraft withF = new RegistrationDraft(female);
        withF.addTableau("A");
        withF.addTableau("B");
        withF.addTableau("C");
        withF.addTableau("F");

        RegistrationSummary sumWithF = service.validate(t, withF);
        assertTrue(sumWithF.isValid(), "Avec F sélectionné, la féminine a +1 => 4 autorisés");

        // Cas 3: A,B,C,F,D => KO (5 tableaux)
        RegistrationDraft tooMany = new RegistrationDraft(female);
        tooMany.addTableau("A");
        tooMany.addTableau("B");
        tooMany.addTableau("C");
        tooMany.addTableau("F");
        tooMany.addTableau("D");

        RegistrationSummary sumTooMany = service.validate(t, tooMany);
        assertFalse(sumTooMany.isValid(), "Même avec +1, 5 reste interdit");
        assertTrue(sumTooMany.violations().stream()
                .anyMatch(v -> v.getCode() == ErrorCode.REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED));
    }
}
