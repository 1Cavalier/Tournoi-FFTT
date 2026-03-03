package fr.Brunoy.gestion_tournois_FFTT.domain.model.competition.service;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Registration;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tournament;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.GenderPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.RegistrationStatus;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration.RegistrationDraft;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration.RegistrationSummary;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.service.MultiTableauRegistrationService;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.service.RegistrationCheckoutService;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.service.TournamentLevelEligibilityPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.model.Player;
import fr.Brunoy.gestion_tournois_FFTT.domain.model.competition.TestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationCheckoutServiceTest {

    private final TournamentLevelEligibilityPolicy levelPolicy = new TournamentLevelEligibilityPolicy();
    private final MultiTableauRegistrationService validator = new MultiTableauRegistrationService(levelPolicy);
    private final RegistrationCheckoutService checkout = new RegistrationCheckoutService(validator);

    @Test
    void reserveOnline_createsReservedRegistrations_withReservedUntilPlus1Hour() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy3PerDay(day);

        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("B", day, GenderPolicy.MIXTE, 16));

        Player p = TestFixtures.playerMale(500, 500);

        RegistrationDraft draft = new RegistrationDraft(p);
        draft.addTableau("A");
        draft.addTableau("B");

        Instant now = Instant.parse("2026-02-10T10:00:00Z");

        RegistrationCheckoutService.ReservationReceipt receipt = checkout.reserveOnline(t, draft, now);

        assertNotNull(receipt);
        assertNotNull(receipt.batchId());
        assertEquals(now.plusSeconds(3600), receipt.reservedUntil());

        List<Registration> regsA = t.registrationsFor("A");
        List<Registration> regsB = t.registrationsFor("B");

        assertEquals(1, regsA.size());
        assertEquals(1, regsB.size());

        assertEquals(RegistrationStatus.RESERVED, regsA.get(0).status());
        assertEquals(RegistrationStatus.RESERVED, regsB.get(0).status());

        assertEquals(receipt.batchId(), regsA.get(0).batchId());
        assertEquals(receipt.batchId(), regsB.get(0).batchId());
    }

    @Test
    void confirmOnlinePayment_beforeExpiration_confirmsAllRegistrationsInBatch() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy3PerDay(day);

        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("B", day, GenderPolicy.MIXTE, 16));

        Player p = TestFixtures.playerMale(500, 500);

        RegistrationDraft draft = new RegistrationDraft(p);
        draft.addTableau("A");
        draft.addTableau("B");

        Instant now = Instant.parse("2026-02-10T10:00:00Z");
        RegistrationCheckoutService.ReservationReceipt receipt = checkout.reserveOnline(t, draft, now);

        Instant payTime = now.plusSeconds(30 * 60);

        checkout.confirmOnlinePayment(t, receipt.batchId(), payTime);

        Registration a = t.registrationsFor("A").get(0);
        Registration b = t.registrationsFor("B").get(0);

        assertEquals(RegistrationStatus.CONFIRMED, a.status());
        assertEquals(RegistrationStatus.CONFIRMED, b.status());
        assertNull(a.reservedUntil());
        assertNull(b.reservedUntil());
    }

    @Test
    void confirmOnlinePayment_afterExpiration_cancelsAllAndThrowsExpired() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy3PerDay(day);

        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 16));
        t.addTableau(TestFixtures.tableauAllSeries("B", day, GenderPolicy.MIXTE, 16));

        Player p = TestFixtures.playerMale(500, 500);

        RegistrationDraft draft = new RegistrationDraft(p);
        draft.addTableau("A");
        draft.addTableau("B");

        Instant now = Instant.parse("2026-02-10T10:00:00Z");
        RegistrationCheckoutService.ReservationReceipt receipt = checkout.reserveOnline(t, draft, now);

        Instant late = now.plusSeconds(3600 + 1);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> checkout.confirmOnlinePayment(t, receipt.batchId(), late));

        assertEquals(ErrorCode.REGISTRATION_RESERVATION_EXPIRED, ex.getCode());

        Registration a = t.registrationsFor("A").get(0);
        Registration b = t.registrationsFor("B").get(0);

        assertEquals(RegistrationStatus.CANCELLED, a.status());
        assertEquals(RegistrationStatus.CANCELLED, b.status());
    }

    @Test
    void cancelExpiredOnlineReservations_cancelsOnlyExpiredReserved() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy3PerDay(day);

        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 16));

        Player p = TestFixtures.playerMale(500, 500);

        RegistrationDraft draft = new RegistrationDraft(p);
        draft.addTableau("A");

        Instant now = Instant.parse("2026-02-10T10:00:00Z");

        checkout.reserveOnline(t, draft, now);

        int cancelled0 = checkout.cancelExpiredOnlineReservations(t, now.plusSeconds(3599));
        assertEquals(0, cancelled0);
        assertEquals(RegistrationStatus.RESERVED, t.registrationsFor("A").get(0).status());
    }

    @Test
    void reservedOnline_blocksCapacity_untilExpired_thenFreesSlot() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy3PerDay(day);

        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 1));

        Player p1 = TestFixtures.playerMale(500, 500);
        Player p2 = TestFixtures.playerFemale(500, 500);

        RegistrationDraft draft1 = new RegistrationDraft(p1);
        draft1.addTableau("A");

        Instant now = Instant.parse("2026-02-10T10:00:00Z");
        checkout.reserveOnline(t, draft1, now);

        RegistrationDraft draft2 = new RegistrationDraft(p2);
        draft2.addTableau("A");

        RegistrationSummary s = validator.validate(t, draft2);
        assertFalse(s.isValid());
        assertTrue(s.violations().stream().anyMatch(v -> v.getCode() == ErrorCode.TABLEAU_FULL),
                "Le validator devrait contenir une violation TABLEAU_FULL");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> checkout.reserveOnline(t, draft2, now.plusSeconds(10)));

        assertEquals(ErrorCode.REGISTRATION_INVALID, ex.getCode());

        checkout.cancelExpiredOnlineReservations(t, now.plusSeconds(3600 + 1));

        RegistrationCheckoutService.ReservationReceipt receipt2 = checkout.reserveOnline(t, draft2,
                now.plusSeconds(3600 + 2));

        assertNotNull(receipt2.batchId());

        assertEquals(1, t.registrationsFor("A").size());
        assertEquals(RegistrationStatus.RESERVED, t.registrationsFor("A").get(0).status());
    }
}