package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.Participant;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.RankingPhase;
import fr.Brunoy.gestion_tournois_FFTT.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TournamentTest {

        // -------------------------------------------------------------------------
        // FIXTURES
        // -------------------------------------------------------------------------

        private static final Instant NOW = Instant.parse("2026-03-06T12:00:00Z");
        private static final LocalDate DAY1 = LocalDate.of(2026, 3, 7);
        private static final LocalDate DAY2 = LocalDate.of(2026, 3, 8);

        private static Tournament tournamentWithPolicy(TournamentRegistrationPolicy policy, Set<LocalDate> days) {
                return new Tournament(
                                "Tournoi Test",
                                TestDataFactory.clubBrunoy(),
                                TournamentLevel.DEPARTEMENTAL,
                                RankingPhase.PHASE_2,
                                days,
                                policy,
                                regulationDraft());
        }

        private static TournamentRegulationInfo regulationDraft() {
                // Draft permissif (aucune obligation "publication" ici)
                return TournamentRegulationInfo.draft(
                                null, // homologationNumber
                                null, // organizerContactName
                                null, // organizerEmail
                                null, // organizerPhone
                                null, // venueName
                                null, // venueStreet
                                null, // venueZip
                                null, // venueCity
                                0, // numberOfTables (>=0 OK en draft)
                                null, // playingArea
                                null, // ballBrandAndType
                                null, // ballProvisionPolicy
                                null, // registrationDeadline
                                null, // checkInDeadline
                                null, // firstMatchesStart
                                null // expectedEndTime
                );
        }

        private static Tableau tableauAllSeriesMixte(String code, LocalDate date, int maxPlayers, int waitCap) {
                return new Tableau(
                                code,
                                "Tableau " + code,
                                date,
                                GenderPolicy.MIXTE,
                                null, // AgeCategoryPolicy null => ANY
                                TableauPointsRuleType.TOUTES_SERIES,
                                null,
                                null,
                                maxPlayers,
                                waitCap,
                                new RegistrationFee(0, 0),
                                LocalTime.of(8, 30),
                                LocalTime.of(9, 0),
                                new PrizeDistribution(List.of(new PrizeTier(1, 1, 0))));
        }

        private static Tableau tableauFemaleOnly(String code, LocalDate date, int maxPlayers, int waitCap) {
                return new Tableau(
                                code,
                                "Tableau " + code,
                                date,
                                GenderPolicy.FEMININ,
                                null,
                                TableauPointsRuleType.TOUTES_SERIES,
                                null,
                                null,
                                maxPlayers,
                                waitCap,
                                new RegistrationFee(0, 0),
                                LocalTime.of(8, 30),
                                LocalTime.of(9, 0),
                                new PrizeDistribution(List.of(new PrizeTier(1, 1, 0))));
        }

        private static TournamentRegistrationPolicy policy(
                        int maxPerDay,
                        int maxTotal,
                        FemaleExtraRuleType femaleRule,
                        String femaleCode) {

                ParticipantEligibilityPolicy elig = TestDataFactory.policyAllAllowed();
                return new TournamentRegistrationPolicy(
                                maxPerDay,
                                maxTotal,
                                femaleRule,
                                femaleCode,
                                elig);
        }

        private static Registration regConfirmed(String tableauCode, Participant p) {
                return Registration.onSiteConfirmed(p, tableauCode, NOW);
        }

        private static Registration regWaitlisted(String tableauCode, Participant p) {
                return Registration.waitlisted(p, tableauCode, NOW, null);
        }

        // -------------------------------------------------------------------------
        // TABLEAUX
        // -------------------------------------------------------------------------

        @Test
        void addTableau_shouldRejectDuplicateCode() {
                Tournament t = tournamentWithPolicy(policy(2, 4, FemaleExtraRuleType.NONE, null), Set.of(DAY1));

                t.addTableau(tableauAllSeriesMixte("A", DAY1, 4, 0));

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> t.addTableau(tableauAllSeriesMixte(" a ", DAY1, 4, 0)));
                assertEquals(ErrorCode.TOURNAMENT_TABLEAU_CODE_DUPLICATE, ex.getCode());
        }

        @Test
        void addTableau_shouldRejectDateOutsideTournamentDays() {
                Tournament t = tournamentWithPolicy(policy(2, 4, FemaleExtraRuleType.NONE, null), Set.of(DAY1));

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> t.addTableau(tableauAllSeriesMixte("A", DAY2, 4, 0)));
                assertEquals(ErrorCode.TOURNAMENT_TABLEAU_DATE_NOT_IN_TOURNAMENT_DAYS, ex.getCode());
        }

        // -------------------------------------------------------------------------
        // REGISTRATION - BASIC
        // -------------------------------------------------------------------------

        @Test
        void addRegistration_shouldConfirmWhenSpotAvailable() {
                Tournament t = tournamentWithPolicy(policy(2, 4, FemaleExtraRuleType.NONE, null), Set.of(DAY1));
                t.addTableau(tableauAllSeriesMixte("A", DAY1, 2, 0));

                Participant p = TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy());

                t.addRegistration(regConfirmed("A", p), NOW);

                List<Registration> regs = t.registrationsFor("A");
                assertEquals(1, regs.size());
                assertEquals(RegistrationStatus.CONFIRMED, regs.get(0).status());
                assertEquals("A", regs.get(0).tableauCode());
                assertEquals(p.participantId(), regs.get(0).participant().participantId());
        }

        @Test
        void addRegistration_shouldRejectAlreadyRegisteredActive() {
                Tournament t = tournamentWithPolicy(policy(3, 5, FemaleExtraRuleType.NONE, null), Set.of(DAY1));
                t.addTableau(tableauAllSeriesMixte("A", DAY1, 10, 10));

                Participant p = TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy());

                t.addRegistration(regConfirmed("A", p), NOW);

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> t.addRegistration(regConfirmed("A", p), NOW));
                assertEquals(ErrorCode.REGISTRATION_ALREADY_REGISTERED, ex.getCode());
        }

        @Test
        void addRegistration_shouldRejectNotEligible() {
                Tournament t = tournamentWithPolicy(policy(2, 4, FemaleExtraRuleType.NONE, null), Set.of(DAY1));
                t.addTableau(tableauFemaleOnly("F", DAY1, 10, 0));

                Participant male = TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy());

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> t.addRegistration(regConfirmed("F", male), NOW));
                assertEquals(ErrorCode.REGISTRATION_NOT_ELIGIBLE, ex.getCode());
        }

        // -------------------------------------------------------------------------
        // CAPACITY + WAITLIST
        // -------------------------------------------------------------------------

        @Test
        void fullTableau_withNoWaitlist_shouldThrowTableauFull() {
                Tournament t = tournamentWithPolicy(policy(5, 10, FemaleExtraRuleType.NONE, null), Set.of(DAY1));
                t.addTableau(tableauAllSeriesMixte("A", DAY1, 1, 0));

                Participant p1 = TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy());
                Participant p2 = TestDataFactory.participantFrom(TestDataFactory.maleSeniorVersailles());

                t.addRegistration(regConfirmed("A", p1), NOW);

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> t.addRegistration(regConfirmed("A", p2), NOW));
                assertEquals(ErrorCode.TABLEAU_FULL, ex.getCode());
        }

        @Test
        void fullTableau_withWaitlist_shouldWaitlistWhenFull() {
                Tournament t = tournamentWithPolicy(policy(5, 10, FemaleExtraRuleType.NONE, null), Set.of(DAY1));
                t.addTableau(tableauAllSeriesMixte("A", DAY1, 1, 2));

                Participant p1 = TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy());
                Participant p2 = TestDataFactory.participantFrom(TestDataFactory.maleSeniorVersailles());

                t.addRegistration(regConfirmed("A", p1), NOW);

                // 2e => waitlist
                t.addRegistration(regWaitlisted("A", p2), NOW);

                List<Registration> regs = t.registrationsFor("A");
                assertEquals(2, regs.size());
                assertEquals(RegistrationStatus.CONFIRMED, regs.get(0).status());
                assertEquals(RegistrationStatus.WAITLISTED, regs.get(1).status());
        }

        @Test
        void waitlist_shouldPromoteFifoWhenCancelFreesSpot() {
                Tournament t = tournamentWithPolicy(policy(5, 10, FemaleExtraRuleType.NONE, null), Set.of(DAY1));
                t.addTableau(tableauAllSeriesMixte("A", DAY1, 1, 10));

                Participant p1 = TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy());
                Participant p2 = TestDataFactory.participantFrom(TestDataFactory.maleSeniorVersailles());
                Participant p3 = TestDataFactory.participantFrom(TestDataFactory.maleSeniorCaen());

                t.addRegistration(regConfirmed("A", p1), NOW);
                t.addRegistration(regWaitlisted("A", p2), NOW);
                t.addRegistration(regWaitlisted("A", p3), NOW);

                // Annule p1 => p2 doit être promu
                t.cancelRegistration("A", p1.participantId(), NOW);

                List<Registration> regs = t.registrationsFor("A");
                // p1 est purgé (CANCELLED) donc il reste p2,p3 actifs
                assertEquals(2, regs.size());
                assertEquals(p2.participantId(), regs.get(0).participant().participantId());
                assertEquals(RegistrationStatus.CONFIRMED, regs.get(0).status());
                assertEquals(p3.participantId(), regs.get(1).participant().participantId());
                assertEquals(RegistrationStatus.WAITLISTED, regs.get(1).status());
        }

        // -------------------------------------------------------------------------
        // LIMITS: MAX TOTAL / MAX PER DAY
        // -------------------------------------------------------------------------

        @Test
        void maxPerDay_shouldBeEnforced() {
                // max 1 par jour
                Tournament t = tournamentWithPolicy(policy(1, 10, FemaleExtraRuleType.NONE, null), Set.of(DAY1));
                t.addTableau(tableauAllSeriesMixte("A", DAY1, 50, 0));
                t.addTableau(tableauAllSeriesMixte("B", DAY1, 50, 0));

                Participant p = TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy());

                t.addRegistration(regConfirmed("A", p), NOW);

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> t.addRegistration(regConfirmed("B", p), NOW));
                assertEquals(ErrorCode.REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED, ex.getCode());
        }

        @Test
        void maxTotal_shouldBeEnforcedAcrossDays() {
                // max total 2 (sur 2 jours)
                Tournament t = tournamentWithPolicy(policy(2, 2, FemaleExtraRuleType.NONE, null), Set.of(DAY1, DAY2));
                t.addTableau(tableauAllSeriesMixte("A", DAY1, 50, 0));
                t.addTableau(tableauAllSeriesMixte("B", DAY2, 50, 0));
                t.addTableau(tableauAllSeriesMixte("C", DAY2, 50, 0));

                Participant p = TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy());

                t.addRegistration(regConfirmed("A", p), NOW);
                t.addRegistration(regConfirmed("B", p), NOW);

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> t.addRegistration(regConfirmed("C", p), NOW));
                assertEquals(ErrorCode.REGISTRATION_MAX_TOTAL_TABLEAUX_EXCEEDED, ex.getCode());
        }

        // -------------------------------------------------------------------------
        // FEMALE EXTRA - ONCE
        // -------------------------------------------------------------------------

        @Test
        void femaleExtra_once_shouldBeConsumedAndNotReusableEvenAfterCancel() {
                // Base: maxPerDay=1, maxTotal=1 + bonus féminin ONCE => permet 2 inscriptions
                TournamentRegistrationPolicy pol = policy(1, 1, FemaleExtraRuleType.EXTRA_ANY_ONCE, null);
                Tournament t = tournamentWithPolicy(pol, Set.of(DAY1, DAY2));

                t.addTableau(tableauAllSeriesMixte("A", DAY1, 50, 0));
                t.addTableau(tableauAllSeriesMixte("B", DAY1, 50, 0));
                t.addTableau(tableauAllSeriesMixte("C", DAY2, 50, 0));

                Participant f = TestDataFactory.participantFrom(TestDataFactory.femaleSeniorVersailles());

                // 1ère inscription (dans la base)
                t.addRegistration(regConfirmed("A", f), NOW);

                // 2e (le même jour) => dépasse max/jour=1, donc consomme le ONCE
                t.addRegistration(regConfirmed("B", f), NOW);

                // Annule B, mais le ONCE reste consommé (choix pro)
                t.cancelRegistration("B", f.participantId(), NOW);

                // Tentative de réutiliser le bonus ONCE sur un autre jour => doit échouer
                BusinessException ex = assertThrows(BusinessException.class,
                                () -> t.addRegistration(regConfirmed("C", f), NOW));
                // Selon la contrainte, ça peut bloquer soit par total soit par per-day,
                // ici total base=1 et ONCE déjà consommé => activeRegs=1, +1 > allowedTotal=1
                assertEquals(ErrorCode.REGISTRATION_MAX_TOTAL_TABLEAUX_EXCEEDED, ex.getCode());
        }

        // -------------------------------------------------------------------------
        // FEMALE EXTRA - SPECIFIC ONCE
        // -------------------------------------------------------------------------

        @Test
        void femaleSpecificOnce_shouldOnlyApplyOnSpecificTableau() {

                TournamentRegistrationPolicy pol = policy(1, 10, FemaleExtraRuleType.SPECIFIC_TABLEAU_ONCE, "FEM");

                Tournament t = tournamentWithPolicy(pol, Set.of(DAY1));

                t.addTableau(tableauAllSeriesMixte("A", DAY1, 50, 0));
                t.addTableau(tableauAllSeriesMixte("FEM", DAY1, 50, 0));
                t.addTableau(tableauAllSeriesMixte("B", DAY1, 50, 0));

                Participant f = TestDataFactory.participantFrom(TestDataFactory.femaleSeniorVersailles());

                // base
                t.addRegistration(regConfirmed("A", f), NOW);

                // B n'est pas le tableau spécifique -> bonus refusé
                BusinessException ex1 = assertThrows(
                                BusinessException.class,
                                () -> t.addRegistration(regConfirmed("B", f), NOW));

                assertEquals(ErrorCode.REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED, ex1.getCode());

                // le spécifique doit fonctionner
                assertDoesNotThrow(() -> t.addRegistration(regConfirmed("FEM", f), NOW));
        }

        // -------------------------------------------------------------------------
        // SAFETY
        // -------------------------------------------------------------------------

        @Test
        void registrationsFor_unknownTableau_shouldThrow() {
                Tournament t = tournamentWithPolicy(policy(2, 4, FemaleExtraRuleType.NONE, null), Set.of(DAY1));
                BusinessException ex = assertThrows(BusinessException.class, () -> t.registrationsFor("X"));
                assertEquals(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND, ex.getCode());
        }
}