package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.bracket;

import fr.Brunoy.gestion_tournois_FFTT.testutil.TestDataFactory;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.BracketBuilder;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.KoBracket;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.KoMatch;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolMatchScore;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolSlot;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.Poule;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.AgeCategory;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.Gender;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.MedicalCertificateStatus;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BracketBuilderTest {

    // -------------------------------------------------------------------------
    // FIXTURES
    // -------------------------------------------------------------------------

    /** Compteur global pour générer des IDs uniques. */
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    /**
     * Retourne un participant FFTT distinct selon n (1-indexed, cycle sur 8
     * fixtures).
     * Utilisé pour les 1ers et 2èmes de poule (qualifiés).
     */
    private static Participant px(int n) {
        return switch (((n - 1) % 8)) {
            case 0 -> TestDataFactory.participantFrom(TestDataFactory.maleSeniorVersailles());
            case 1 -> TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy());
            case 2 -> TestDataFactory.participantFrom(TestDataFactory.maleSeniorCaen());
            case 3 -> TestDataFactory.participantFrom(TestDataFactory.femaleSeniorVersailles());
            case 4 -> TestDataFactory.participantFrom(TestDataFactory.femaleJuniorBrunoy());
            case 5 -> TestDataFactory.participantFrom(TestDataFactory.femaleVeteran45Caen());
            case 6 -> TestDataFactory.participantFrom(TestDataFactory.maleBenjaminBrunoy());
            default -> TestDataFactory.participantFrom(TestDataFactory.maleVeteran80Caen());
        };
    }

    /**
     * Retourne un GuestParticipant avec un ID toujours unique.
     * Utilisé pour les 3èmes de poule (éliminés, jamais qualifiés).
     */
    private static Participant uniqueGuest() {
        int id = COUNTER.incrementAndGet();
        return TestDataFactory.guest(
                "ELIM-" + id, "Eliminé " + id,
                Gender.MALE, "FR",
                AgeCategory.SENIOR,
                MedicalCertificateStatus.VALIDE);
    }

    private static PoolMatchScore win3_0() {
        return PoolMatchScore.ofSetsOnly(3, 0);
    }

    /**
     * Crée une poule de 3 terminée. seed1 et seed2 sont les qualifiés (FFTT
     * distincts).
     * Le 3ème est un GuestParticipant unique → jamais de doublon.
     * Le joueur seed1 (pos1) gagne tout.
     */
    private static Poule finishedPool3(int seed1, int seed2) {
        Participant j1 = px(seed1);
        Participant j2 = px(seed2);
        Participant j3 = uniqueGuest(); // toujours unique → jamais de POOL_DUPLICATE

        Poule p = new Poule("TBL_A", seed1, List.of(
                new PoolSlot(seed1, 1, j1),
                new PoolSlot(seed2, 2, j2),
                new PoolSlot(seed1 * 100 + seed2, 3, j3))); // seedRank unique
        p.recordScore(1, win3_0()); // j1 bat j3
        p.recordScore(2, win3_0()); // j1 bat j2
        p.recordScore(3, win3_0()); // j2 bat j3
        return p;
    }

    /**
     * Crée une poule de 2 terminée. Le joueur seed1 gagne.
     */
    private static Poule finishedPool2(int seed1, int seed2) {
        Participant j1 = px(seed1);
        Participant j2 = px(seed2);

        Poule p = new Poule("TBL_A", seed1, List.of(
                new PoolSlot(seed1, 1, j1),
                new PoolSlot(seed2, 2, j2)));
        p.recordScore(1, win3_0());
        return p;
    }

    private static BracketBuilder builder() {
        return new BracketBuilder();
    }

    // =========================================================================
    // VALIDATION
    // =========================================================================

    @Test
    void build_nullTableauCode_shouldThrow() {
        Poule pool = finishedPool3(1, 2);
        assertThrows(NullPointerException.class,
                () -> builder().build(null, List.of(pool)));
    }

    @Test
    void build_emptyPools_shouldThrow() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> builder().build("TBL_A", List.of()));
        assertEquals(ErrorCode.BRACKET_NO_QUALIFIED_PLAYERS, ex.getCode());
    }

    // =========================================================================
    // TAILLE DU TABLEAU
    // =========================================================================

    @Test
    void build_4pools_shouldMakeBracket8() {
        // 4 poules de 3 → 8 qualifiés → tableau de 8
        List<Poule> pools = List.of(
                finishedPool3(1, 2),
                finishedPool3(3, 4),
                finishedPool3(5, 6),
                finishedPool3(7, 8));
        KoBracket b = builder().build("TBL_A", pools);
        assertEquals(8, b.bracketSize());
        assertEquals(3, b.totalRounds());
    }

    @Test
    void build_2pools_shouldMakeBracket4() {
        // 2 poules de 3 → 4 qualifiés → tableau de 4
        List<Poule> pools = List.of(
                finishedPool3(1, 2),
                finishedPool3(3, 4));
        KoBracket b = builder().build("TBL_A", pools);
        assertEquals(4, b.bracketSize());
        assertEquals(2, b.totalRounds());
    }

    @Test
    void build_3pools_shouldMakeBracket8WithByes() {
        // 3 poules de 3 → 6 qualifiés → tableau de 8 (2 BYE)
        List<Poule> pools = List.of(
                finishedPool3(1, 2),
                finishedPool3(3, 4),
                finishedPool3(5, 6));
        KoBracket b = builder().build("TBL_A", pools);
        assertEquals(8, b.bracketSize());

        long byeCount = b.allMatches().stream()
                .filter(m -> m.status() == KoMatch.Status.BYE)
                .count();
        assertEquals(2, byeCount);
    }

    @Test
    void build_6pools_shouldMakeBracket16WithByes() {
        // 6 poules de 3 → 12 qualifiés → tableau de 16 (4 BYE)
        List<Poule> pools = List.of(
                finishedPool3(1, 2),
                finishedPool3(3, 4),
                finishedPool3(5, 6),
                finishedPool3(7, 8),
                finishedPool2(1, 3),
                finishedPool2(5, 7));
        KoBracket b = builder().build("TBL_A", pools);
        assertEquals(16, b.bracketSize());

        long byeCount = b.allMatches().stream()
                .filter(m -> m.status() == KoMatch.Status.BYE)
                .count();
        assertEquals(4, byeCount);
    }

    // =========================================================================
    // QUALIFIÉS PLACÉS CORRECTEMENT
    // =========================================================================

    @Test
    void build_allQualifiedShouldBePlaced() {
        // 4 poules → 8 qualifiés distincts → tous dans un match
        List<Poule> pools = List.of(
                finishedPool3(1, 2),
                finishedPool3(3, 4),
                finishedPool3(5, 6),
                finishedPool3(7, 8));

        List<Participant> expectedQualified = new ArrayList<>();
        for (Poule p : pools) {
            expectedQualified.addAll(p.qualifiedParticipants());
        }
        assertEquals(8, expectedQualified.size());

        KoBracket b = builder().build("TBL_A", pools);

        List<Participant> placedPlayers = b.allMatches().stream()
                .flatMap(m -> {
                    List<Participant> ps = new ArrayList<>();
                    if (m.player1() != null)
                        ps.add(m.player1());
                    if (m.player2() != null)
                        ps.add(m.player2());
                    return ps.stream();
                })
                .collect(Collectors.toList());

        for (Participant expected : expectedQualified) {
            assertTrue(placedPlayers.contains(expected),
                    "Joueur qualifié non placé : " + expected.participantId());
        }
    }

    @Test
    void build_samePoolPlayersShouldNotMeetBeforeFinal() {
        // 1er et 2ème de la même poule doivent être dans des demi-tableaux opposés
        List<Poule> pools = List.of(
                finishedPool3(1, 2),
                finishedPool3(3, 4),
                finishedPool3(5, 6),
                finishedPool3(7, 8));

        KoBracket b = builder().build("TBL_A", pools);

        for (Poule poule : pools) {
            List<Participant> qualified = poule.qualifiedParticipants();
            if (qualified.size() < 2)
                continue;

            Participant first = qualified.get(0);
            Participant second = qualified.get(1);

            int pos1 = findPositionInRound1(b, first);
            int pos2 = findPositionInRound1(b, second);

            // Si l'un a un BYE (pas en round 1), on skip
            if (pos1 == -1 || pos2 == -1)
                continue;

            // Tableau de 8 : quart haut = pos 1-2, quart bas = pos 3-4
            int quarter = b.bracketSize() / 4;
            boolean firstInHigh = pos1 <= quarter;
            boolean secondInHigh = pos2 <= quarter;

            assertNotEquals(firstInHigh, secondInHigh,
                    "Le 1er (pos " + pos1 + ") et le 2ème (pos " + pos2
                            + ") de la poule " + poule.poolLabel()
                            + " doivent être dans des demi-tableaux opposés");
        }
    }

    @Test
    void build_withPool2_shouldPlaceBothPlayersAsQualified() {
        // Poule de 2 → les 2 joueurs qualifiés → dans le tableau
        List<Poule> pools = List.of(
                finishedPool2(1, 2),
                finishedPool2(3, 4));

        KoBracket b = builder().build("TBL_A", pools);
        assertEquals(4, b.bracketSize());

        long placed = b.allMatches().stream()
                .flatMap(m -> {
                    List<Participant> ps = new ArrayList<>();
                    if (m.player1() != null)
                        ps.add(m.player1());
                    if (m.player2() != null)
                        ps.add(m.player2());
                    return ps.stream();
                })
                .count();
        assertEquals(4, placed);
    }

    @Test
    void build_sortedByPoolNumber() {
        // Les poules passées dans le désordre doivent fonctionner
        Poule p3 = finishedPool3(5, 6);
        Poule p1 = finishedPool3(1, 2);
        Poule p2 = finishedPool3(3, 4);

        assertDoesNotThrow(() -> builder().build("TBL_A", List.of(p3, p1, p2)));
    }

    // =========================================================================
    // HELPER PRIVÉ
    // =========================================================================

    private static int findPositionInRound1(KoBracket b, Participant p) {
        return b.matchesForRound(1).stream()
                .filter(m -> p.equals(m.player1()) || p.equals(m.player2()))
                .mapToInt(KoMatch::position)
                .findFirst()
                .orElse(-1);
    }
}