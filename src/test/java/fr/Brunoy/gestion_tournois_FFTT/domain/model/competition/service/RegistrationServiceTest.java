package fr.Brunoy.gestion_tournois_FFTT.domain.model.competition.service;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tournament;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.GenderPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.service.RegistrationService;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.service.TournamentLevelEligibilityPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.Player;
import fr.Brunoy.gestion_tournois_FFTT.domain.model.competition.TestFixtures;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationServiceTest {

    private final TournamentLevelEligibilityPolicy levelPolicy = new TournamentLevelEligibilityPolicy();
    private final RegistrationService service = new RegistrationService(levelPolicy);

    @Test
    void register_ok_addsRegistration() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy(day);

        Tableau tabA = TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 16);
        t.addTableau(tabA);

        Player p = TestFixtures.playerMale(500, 500);

        service.register(t, p, "A");

        assertEquals(1, t.registrationsFor("A").size());
        assertEquals(p, t.registrationsFor("A").get(0).player());
    }

    @Test
    void register_tableauNotFound_throwsBusinessException() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy(day);

        Player p = TestFixtures.playerMale(500, 500);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(t, p, "NOPE"));

        assertEquals(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND, ex.getCode());
    }

    @Test
    void register_alreadyRegistered_throwsBusinessException() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy(day);

        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 16));

        Player p = TestFixtures.playerMale(500, 500);

        service.register(t, p, "A");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(t, p, "A"));

        assertEquals(ErrorCode.REGISTRATION_ALREADY_REGISTERED, ex.getCode());
    }

    @Test
    void register_tableauFull_throwsBusinessException() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy(day);

        // maxPlayers = 1
        t.addTableau(TestFixtures.tableauAllSeries("A", day, GenderPolicy.MIXTE, 1));

        Player p1 = TestFixtures.playerMale(500, 500);
        Player p2 = TestFixtures.playerFemale(500, 500);

        service.register(t, p1, "A");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(t, p2, "A"));

        assertEquals(ErrorCode.TABLEAU_FULL, ex.getCode());
    }

    @Test
    void register_notEligible_gender_femaleOnly_throwsForMale() {
        LocalDate day = LocalDate.of(2026, 2, 10);
        Tournament t = TestFixtures.tournament1DayWithPolicy(day);

        t.addTableau(TestFixtures.tableauAllSeries("F", day, GenderPolicy.FEMININ_ONLY, 16));

        Player male = TestFixtures.playerMale(500, 500);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(t, male, "F"));

        assertEquals(ErrorCode.REGISTRATION_NOT_ELIGIBLE, ex.getCode());
    }
}