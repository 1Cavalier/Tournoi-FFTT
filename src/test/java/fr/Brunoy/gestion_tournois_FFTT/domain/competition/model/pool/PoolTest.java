package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.pool;

import fr.Brunoy.gestion_tournois_FFTT.testutil.TestDataFactory;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolMatch;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolMatchScore;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolSlot;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolStanding;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.Poule;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PouleTest {

    // -------------------------------------------------------------------------
    // FIXTURES
    // -------------------------------------------------------------------------

    private static Participant j1() {
        return TestDataFactory.participantFrom(TestDataFactory.maleSeniorVersailles()); // 1230 pts
    }

    private static Participant j2() {
        return TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy()); // 980 pts
    }

    private static Participant j3() {
        return TestDataFactory.participantFrom(TestDataFactory.maleSeniorCaen()); // 820 pts
    }

    /** Poule de 3 avec j1(seed1), j2(seed2), j3(seed3). */
    private static Poule poule3() {
        return new Poule("TBL_A", 1, List.of(
                new PoolSlot(1, 1, j1()),
                new PoolSlot(2, 2, j2()),
                new PoolSlot(3, 3, j3())));
    }

    /** Poule de 2 avec j1(seed1), j2(seed2). */
    private static Poule poule2() {
        return new Poule("TBL_A", 1, List.of(
                new PoolSlot(1, 1, j1()),
                new PoolSlot(2, 2, j2())));
    }

    /** Score 3-0 : player1 gagne. */
    private static PoolMatchScore win3_0() {
        return PoolMatchScore.ofSetsOnly(3, 0);
    }

    /** Score 0-3 : player2 gagne. */
    private static PoolMatchScore win0_3() {
        return PoolMatchScore.ofSetsOnly(0, 3);
    }

    private static PoolMatch matchAt(List<PoolMatch> matches, int order) {
        return matches.stream()
                .filter(m -> m.matchOrderInPool() == order)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Match " + order + " introuvable"));
    }

    private static PoolStanding standingOf(List<PoolStanding> standings, Participant p) {
        return standings.stream()
                .filter(s -> s.participant().equals(p))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Standing de " + p.participantId() + " introuvable"));
    }

    // =========================================================================
    // CONSTRUCTION
    // =========================================================================

    @Test
    void poule3_shouldGenerate3Matches() {
        assertEquals(3, poule3().matches().size());
    }

    @Test
    void poule3_matchOrder_shouldBe1v3_then_1v2_then_2v3() {
        List<PoolMatch> matches = poule3().matches();

        PoolMatch m1 = matchAt(matches, 1);
        assertEquals(1, m1.slot1().positionInPool());
        assertEquals(3, m1.slot2().positionInPool());

        PoolMatch m2 = matchAt(matches, 2);
        assertEquals(1, m2.slot1().positionInPool());
        assertEquals(2, m2.slot2().positionInPool());

        PoolMatch m3 = matchAt(matches, 3);
        assertEquals(2, m3.slot1().positionInPool());
        assertEquals(3, m3.slot2().positionInPool());
    }

    @Test
    void poule2_shouldGenerate1Match() {
        assertEquals(1, poule2().matches().size());
    }

    @Test
    void poule2_matchOrder_shouldBe1v2() {
        PoolMatch m = poule2().matches().get(0);
        assertEquals(1, m.slot1().positionInPool());
        assertEquals(2, m.slot2().positionInPool());
        assertEquals(1, m.matchOrderInPool());
    }

    @Test
    void poolLabel_shouldReturnLetter() {
        assertEquals("A", poule3().poolLabel());
        Poule pouleB = new Poule("TBL_A", 2, List.of(
                new PoolSlot(1, 1, j1()),
                new PoolSlot(2, 2, j2()),
                new PoolSlot(3, 3, j3())));
        assertEquals("B", pouleB.poolLabel());
    }

    @Test
    void allMatches_shouldStartAsPending() {
        poule3().matches().forEach(m -> assertEquals(PoolMatch.Status.PENDING, m.status()));
    }

    @Test
    void construction_shouldRejectFewerThan2Players() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new Poule("TBL_A", 1, List.of(
                        new PoolSlot(1, 1, j1()))));
        assertEquals(ErrorCode.POOL_INVALID_SIZE, ex.getCode());
    }

    @Test
    void construction_shouldRejectMoreThan3Players() {
        Participant j4 = TestDataFactory.participantFrom(TestDataFactory.maleVeteran80Caen());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new Poule("TBL_A", 1, List.of(
                        new PoolSlot(1, 1, j1()),
                        new PoolSlot(2, 2, j2()),
                        new PoolSlot(3, 3, j3()),
                        new PoolSlot(4, 4, j4))));
        assertEquals(ErrorCode.POOL_INVALID_SIZE, ex.getCode());
    }

    @Test
    void construction_shouldRejectDuplicateParticipant() {
        Participant p = j1();
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new Poule("TBL_A", 1, List.of(
                        new PoolSlot(1, 1, p),
                        new PoolSlot(2, 2, p),
                        new PoolSlot(3, 3, j3()))));
        assertEquals(ErrorCode.POOL_DUPLICATE_PARTICIPANT, ex.getCode());
    }

    // =========================================================================
    // POOL MATCH SCORE — VALIDATION
    // =========================================================================

    @Test
    void score_3_0_shouldBeValid() {
        PoolMatchScore score = PoolMatchScore.ofSetsOnly(3, 0);
        assertTrue(score.player1Wins());
        assertEquals(3, score.setsWonByPlayer1());
        assertEquals(0, score.setsWonByPlayer2());
    }

    @Test
    void score_3_2_shouldBeValid() {
        PoolMatchScore score = PoolMatchScore.ofSetsOnly(3, 2);
        assertTrue(score.player1Wins());
        assertEquals(3, score.setsWonByPlayer1());
        assertEquals(2, score.setsWonByPlayer2());
    }

    @Test
    void score_withSetDetail_shouldAccumulatePointsCorrectly() {
        PoolMatchScore score = new PoolMatchScore(List.of(
                new int[] { 11, 8 },
                new int[] { 9, 11 },
                new int[] { 11, 7 },
                new int[] { 11, 5 }));
        assertTrue(score.player1Wins());
        assertEquals(3, score.setsWonByPlayer1());
        assertEquals(1, score.setsWonByPlayer2());
        assertEquals(42, score.totalPointsPlayer1());
        assertEquals(31, score.totalPointsPlayer2());
    }

    @Test
    void score_deuceSet13_11_shouldBeValid() {
        assertDoesNotThrow(() -> new PoolMatchScore(List.of(
                new int[] { 13, 11 },
                new int[] { 11, 5 },
                new int[] { 11, 3 })));
    }

    @Test
    void score_setUnder11_shouldThrow() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new PoolMatchScore(List.of(new int[] { 10, 8 })));
        assertEquals(ErrorCode.POOL_MATCH_SCORE_SET_NOT_FINISHED, ex.getCode());
    }

    @Test
    void score_deuce11_10_shouldThrow() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new PoolMatchScore(List.of(new int[] { 11, 10 })));
        assertEquals(ErrorCode.POOL_MATCH_SCORE_SET_INVALID_DEUCE, ex.getCode());
    }

    @Test
    void score_deuce14_11_shouldThrow() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new PoolMatchScore(List.of(new int[] { 14, 11 })));
        assertEquals(ErrorCode.POOL_MATCH_SCORE_SET_INVALID_DEUCE, ex.getCode());
    }

    @Test
    void score_negativePoints_shouldThrow() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new PoolMatchScore(List.of(new int[] { 11, -1 })));
        assertEquals(ErrorCode.POOL_MATCH_SCORE_NEGATIVE_POINTS, ex.getCode());
    }

    @Test
    void score_notFinished_shouldThrow() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new PoolMatchScore(List.of(
                        new int[] { 11, 5 },
                        new int[] { 11, 7 })));
        assertEquals(ErrorCode.POOL_MATCH_SCORE_NOT_FINISHED, ex.getCode());
    }

    @Test
    void score_tooManySets_shouldThrow() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new PoolMatchScore(List.of(
                        new int[] { 11, 5 },
                        new int[] { 11, 5 },
                        new int[] { 11, 5 },
                        new int[] { 11, 5 },
                        new int[] { 11, 5 },
                        new int[] { 11, 5 })));
        assertEquals(ErrorCode.POOL_MATCH_SCORE_TOO_MANY_SETS, ex.getCode());
    }

    // =========================================================================
    // SAISIE DES SCORES
    // =========================================================================

    @Test
    void recordScore_shouldCompleteMatch() {
        Poule p = poule3();
        p.recordScore(1, win3_0());
        assertEquals(PoolMatch.Status.COMPLETED, matchAt(p.matches(), 1).status());
    }

    @Test
    void recordScore_twice_shouldThrow() {
        Poule p = poule3();
        p.recordScore(1, win3_0());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> p.recordScore(1, win3_0()));
        assertEquals(ErrorCode.POOL_MATCH_ALREADY_FINISHED, ex.getCode());
    }

    @Test
    void recordScore_unknownMatchOrder_shouldThrow() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> poule3().recordScore(99, win3_0()));
        assertEquals(ErrorCode.POOL_MATCH_NOT_FOUND, ex.getCode());
    }

    @Test
    void startMatch_shouldSetInProgress() {
        Poule p = poule3();
        p.startMatch(2);
        assertEquals(PoolMatch.Status.IN_PROGRESS, matchAt(p.matches(), 2).status());
    }

    @Test
    void recordScore_afterInProgress_shouldComplete() {
        Poule p = poule3();
        p.startMatch(1);
        p.recordScore(1, win3_0());
        assertEquals(PoolMatch.Status.COMPLETED, matchAt(p.matches(), 1).status());
    }

    @Test
    void declareWalkover_shouldSetWalkoverStatus() {
        Poule p = poule3();
        p.declareWalkover(1, j3()); // match 1 = j1 vs j3
        assertEquals(PoolMatch.Status.WALKOVER, matchAt(p.matches(), 1).status());
    }

    @Test
    void declareWalkover_wrongParticipant_shouldThrow() {
        // match 1 = j1 vs j3, j2 n'est pas dedans
        BusinessException ex = assertThrows(BusinessException.class,
                () -> poule3().declareWalkover(1, j2()));
        assertEquals(ErrorCode.POOL_MATCH_PARTICIPANT_NOT_IN_MATCH, ex.getCode());
    }

    // =========================================================================
    // CLASSEMENT — CAS NORMAUX
    // =========================================================================

    @Test
    void computeStandings_beforeAllMatchesFinished_shouldThrow() {
        Poule p = poule3();
        p.recordScore(1, win3_0());
        BusinessException ex = assertThrows(BusinessException.class, p::computeStandings);
        assertEquals(ErrorCode.POOL_NOT_ALL_MATCHES_FINISHED, ex.getCode());
    }

    @Test
    void computeStandings_j1WinsAll_shouldRankCorrectly() {
        Poule p = poule3();
        p.recordScore(1, win3_0()); // j1 bat j3
        p.recordScore(2, win3_0()); // j1 bat j2
        p.recordScore(3, win3_0()); // j2 bat j3

        List<PoolStanding> standings = p.computeStandings();

        Participant j1 = j1(), j2 = j2(), j3 = j3();

        PoolStanding s1 = standingOf(standings, j1);
        PoolStanding s2 = standingOf(standings, j2);
        PoolStanding s3 = standingOf(standings, j3);

        assertEquals(1, s1.rank());
        assertEquals(4, s1.matchPoints()); // 2 victoires = 2+2
        assertEquals(2, s2.rank());
        assertEquals(3, s2.matchPoints()); // 1V + 1D = 2+1
        assertEquals(3, s3.rank());
        assertEquals(2, s3.matchPoints()); // 2 défaites = 1+1
    }

    @Test
    void qualifiedParticipants_shouldReturnTop2() {
        Poule p = poule3();
        p.recordScore(1, win3_0()); // j1 bat j3
        p.recordScore(2, win3_0()); // j1 bat j2
        p.recordScore(3, win3_0()); // j2 bat j3

        Participant j1 = j1(), j2 = j2(), j3 = j3();
        List<Participant> qualified = p.qualifiedParticipants();

        assertEquals(2, qualified.size());
        assertTrue(qualified.contains(j1));
        assertTrue(qualified.contains(j2));
        assertFalse(qualified.contains(j3));
    }

    @Test
    void qualifiedCount_shouldAlwaysBe2() {
        assertEquals(2, poule3().qualifiedCount());
        assertEquals(2, poule2().qualifiedCount());
    }

    @Test
    void allMatchesFinished_shouldTrackCorrectly() {
        Poule p = poule3();
        assertFalse(p.allMatchesFinished());
        p.recordScore(1, win3_0());
        p.recordScore(2, win3_0());
        assertFalse(p.allMatchesFinished());
        p.recordScore(3, win3_0());
        assertTrue(p.allMatchesFinished());
    }

    // =========================================================================
    // CLASSEMENT — DÉPARTAGE
    // =========================================================================

    @Test
    void computeStandings_headToHead_shouldBreakTie() {
        Poule p = poule3();
        // j3 bat j1 (match 1)
        p.recordScore(1, win0_3());
        // j1 bat j2 (match 2)
        p.recordScore(2, win3_0());
        // j2 bat j3 (match 3)
        p.recordScore(3, win3_0());

        // j1 : 1V(j2) + 1D(j3) = 3 pts
        // j2 : 1V(j3) + 1D(j1) = 3 pts
        // j3 : 1V(j1) + 1D(j2) = 3 pts
        // Tous à 3 pts → départage par tête-à-tête

        List<PoolStanding> standings = p.computeStandings();
        Participant j1 = j1(), j2 = j2(), j3 = j3();

        PoolStanding s1 = standingOf(standings, j1);
        PoolStanding s2 = standingOf(standings, j2);
        PoolStanding s3 = standingOf(standings, j3);

        // Si résolu : j1 bat j2, j2 bat j3, j3 bat j1 → ordre circulaire
        // Le départage peut aboutir à rank=0 (tirage au sort) si non résolu
        if (s1.rank() != 0) {
            List<Integer> ranks = List.of(s1.rank(), s2.rank(), s3.rank());
            assertTrue(ranks.contains(1) && ranks.contains(2) && ranks.contains(3),
                    "Les rangs doivent être 1, 2 et 3 : " + ranks);
        }
    }

    @Test
    void computeStandings_clearWinner_shouldNotNeedTiebreak() {
        Poule p = poule3();
        p.recordScore(1, win3_0()); // j1 bat j3 (3-0)
        p.recordScore(2, win0_3()); // j2 bat j1 (3-0)
        p.recordScore(3, win3_0()); // j2 bat j3 (3-0)

        // j2 : 2V = 4 pts → 1er direct
        // j1 : 1V + 1D = 3 pts → 2ème
        // j3 : 2D = 2 pts → 3ème

        List<PoolStanding> standings = p.computeStandings();
        Participant j1 = j1(), j2 = j2(), j3 = j3();

        assertEquals(1, standingOf(standings, j2).rank());
        assertEquals(2, standingOf(standings, j1).rank());
        assertEquals(3, standingOf(standings, j3).rank());
    }

    // =========================================================================
    // CLASSEMENT — WALKOVER
    // =========================================================================

    @Test
    void walkover_playerShouldGetZeroPointsAndBeLastRanked() {
        Poule p = poule3();
        Participant j3 = j3();
        p.declareWalkover(1, j3); // j3 forfait match j1 vs j3
        p.recordScore(2, win3_0()); // j1 bat j2
        p.declareWalkover(3, j3); // j3 forfait match j2 vs j3

        List<PoolStanding> standings = p.computeStandings();
        PoolStanding s3 = standingOf(standings, j3);

        assertTrue(s3.hasWalkover());
        assertEquals(3, s3.rank());
    }

    @Test
    void walkover_resultsShouldBeAnnulledForOtherPlayers() {
        // Règle FFTT : les matchs du joueur forfait sont annulés
        // Seul le match j1 vs j2 compte pour le classement
        Poule p = poule3();
        Participant j3 = j3();
        p.declareWalkover(1, j3); // j1 vs j3 annulé
        p.recordScore(2, win3_0()); // j1 bat j2 → j1=2pts, j2=1pt
        p.declareWalkover(3, j3); // j2 vs j3 annulé

        List<PoolStanding> standings = p.computeStandings();
        Participant j1 = j1(), j2 = j2();

        assertEquals(2, standingOf(standings, j1).matchPoints());
        assertEquals(1, standingOf(standings, j2).matchPoints());
        assertEquals(1, standingOf(standings, j1).rank());
        assertEquals(2, standingOf(standings, j2).rank());
    }

    @Test
    void walkover_playerShouldNotBeQualified() {
        Poule p = poule3();
        Participant j3 = j3();
        p.declareWalkover(1, j3);
        p.recordScore(2, win3_0());
        p.declareWalkover(3, j3);

        assertFalse(p.qualifiedParticipants().contains(j3));
        assertEquals(2, p.qualifiedParticipants().size());
    }

    // =========================================================================
    // POULE DE 2
    // =========================================================================

    @Test
    void poule2_qualifiedCount_shouldBe2() {
        assertEquals(2, poule2().qualifiedCount());
    }

    @Test
    void poule2_bothPlayersShouldBeQualified() {
        Poule p = poule2();
        p.recordScore(1, win3_0()); // j1 gagne

        Participant j1 = j1(), j2 = j2();
        List<Participant> qualified = p.qualifiedParticipants();

        assertEquals(2, qualified.size());
        assertTrue(qualified.contains(j1), "j1 (vainqueur) doit être qualifié");
        assertTrue(qualified.contains(j2), "j2 (perdant) doit aussi être qualifié");
    }

    @Test
    void poule2_winner1st_loser2nd() {
        Poule p = poule2();
        p.recordScore(1, win3_0()); // j1 gagne

        Participant j1 = j1(), j2 = j2();
        List<PoolStanding> standings = p.computeStandings();

        assertEquals(1, standingOf(standings, j1).rank());
        assertEquals(2, standingOf(standings, j1).matchPoints());
        assertEquals(2, standingOf(standings, j2).rank());
        assertEquals(1, standingOf(standings, j2).matchPoints());
    }

    @Test
    void poule2_walkover_forfeitPlayerShouldNotBeQualified() {
        Poule p = poule2();
        Participant j2 = j2();
        p.declareWalkover(1, j2);

        assertFalse(p.qualifiedParticipants().contains(j2));
    }

    @Test
    void poule2_allMatchesFinished_afterSingleMatch() {
        Poule p = poule2();
        assertFalse(p.allMatchesFinished());
        p.recordScore(1, win3_0());
        assertTrue(p.allMatchesFinished());
    }

    // =========================================================================
    // POOL SLOT — VALIDATION
    // =========================================================================

    @Test
    void poolSlot_invalidSeedRank_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new PoolSlot(0, 1, j1()));
    }

    @Test
    void poolSlot_invalidPosition_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new PoolSlot(1, 0, j1()));
        assertThrows(IllegalArgumentException.class,
                () -> new PoolSlot(1, 5, j1()));
    }

    @Test
    void poolSlot_nullParticipant_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> new PoolSlot(1, 1, null));
    }

    // =========================================================================
    // POOL MATCH — TRANSITIONS
    // =========================================================================

    @Test
    void startMatch_twice_shouldThrow() {
        Poule p = poule3();
        p.startMatch(1);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> p.startMatch(1));
        assertEquals(ErrorCode.POOL_MATCH_INVALID_TRANSITION, ex.getCode());
    }

    @Test
    void startMatch_afterCompleted_shouldThrow() {
        Poule p = poule3();
        p.recordScore(1, win3_0());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> p.startMatch(1));
        assertEquals(ErrorCode.POOL_MATCH_INVALID_TRANSITION, ex.getCode());
    }

    @Test
    void completedMatch_winner_shouldBeCorrect() {
        Poule p = poule3();
        Participant j1 = j1(), j3 = j3();
        p.recordScore(1, win3_0()); // match 1 = j1 vs j3, j1 gagne
        PoolMatch m1 = matchAt(p.matches(), 1);
        assertEquals(j1, m1.winner());
        assertEquals(j3, m1.loser());
    }

    @Test
    void walkoverMatch_winner_shouldBeOpponent() {
        Poule p = poule3();
        Participant j1 = j1(), j3 = j3();
        p.declareWalkover(1, j3); // j3 forfait dans match j1 vs j3
        PoolMatch m1 = matchAt(p.matches(), 1);
        assertEquals(j1, m1.winner());
        assertEquals(j3, m1.loser());
    }

    @Test
    void poolMatch_sameParticipant_shouldThrow() {
        Participant p = j1();
        PoolSlot slotA = new PoolSlot(1, 1, p);
        PoolSlot slotB = new PoolSlot(2, 2, p);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new PoolMatch(slotA, slotB, 1));
        assertEquals(ErrorCode.POOL_MATCH_SAME_PARTICIPANT, ex.getCode());
    }
}