package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.bracket;

import fr.Brunoy.gestion_tournois_FFTT.testutil.TestDataFactory;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.KoBracket;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.KoMatch;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolMatchScore;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KoBracketTest {

    // -------------------------------------------------------------------------
    // FIXTURES
    // -------------------------------------------------------------------------

    private static Participant p1() {
        return TestDataFactory.participantFrom(TestDataFactory.maleSeniorVersailles());
    }

    private static Participant p2() {
        return TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy());
    }

    private static Participant p3() {
        return TestDataFactory.participantFrom(TestDataFactory.maleSeniorCaen());
    }

    private static Participant p4() {
        return TestDataFactory.participantFrom(TestDataFactory.femaleSeniorVersailles());
    }

    private static PoolMatchScore win3_0() {
        return PoolMatchScore.ofSetsOnly(3, 0);
    }

    /**
     * Construit un KoBracket de taille 4 (2 matchs en round 1, 1 finale)
     * avec les 4 joueurs déjà assignés.
     *
     * Round 1 :
     * Match pos1 : p1 vs p2
     * Match pos2 : p3 vs p4
     * Round 2 (finale) :
     * Match pos1 : vainqueur(R1P1) vs vainqueur(R1P2)
     */
    private static KoBracket bracket4() {
        List<KoMatch> matches = new ArrayList<>();

        // Round 1
        KoMatch r1p1 = new KoMatch(1, 1);
        KoMatch r1p2 = new KoMatch(1, 2);
        r1p1.assignPlayers(p1(), p2());
        r1p2.assignPlayers(p3(), p4());

        // Round 2 (finale)
        KoMatch r2p1 = new KoMatch(2, 1);

        matches.add(r1p1);
        matches.add(r1p2);
        matches.add(r2p1);

        return new KoBracket("TBL_A", 4, matches);
    }

    // =========================================================================
    // CONSTRUCTION
    // =========================================================================

    @Test
    void construction_shouldComputeCorrectRounds() {
        KoBracket b = bracket4();
        assertEquals(2, b.totalRounds()); // log2(4) = 2
    }

    @Test
    void construction_shouldHaveCorrectBracketSize() {
        assertEquals(4, bracket4().bracketSize());
    }

    @Test
    void construction_nonPowerOf2_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new KoBracket("TBL_A", 3, List.of()));
    }

    @Test
    void matchesForRound_shouldReturnCorrectMatches() {
        KoBracket b = bracket4();
        List<KoMatch> round1 = b.matchesForRound(1);
        assertEquals(2, round1.size());
        assertEquals(1, round1.get(0).position());
        assertEquals(2, round1.get(1).position());
    }

    @Test
    void allMatches_shouldBeSortedByRoundThenPosition() {
        KoBracket b = bracket4();
        List<KoMatch> all = b.allMatches();
        assertEquals(3, all.size());
        assertEquals(1, all.get(0).round());
        assertEquals(1, all.get(0).position());
        assertEquals(1, all.get(1).round());
        assertEquals(2, all.get(1).position());
        assertEquals(2, all.get(2).round());
    }

    // =========================================================================
    // SAISIE DES SCORES ET PROPAGATION
    // =========================================================================

    @Test
    void recordScore_shouldPropagateWinnerToNextRound() {
        KoBracket b = bracket4();

        // p1 gagne le match R1P1
        b.recordScore(1, 1, win3_0());

        // Le vainqueur doit être propagé en R2P1 comme player1
        KoMatch finale = b.finalMatch();
        assertEquals(p1(), finale.player1());
    }

    @Test
    void recordScore_bothMatches_shouldPopulateFinal() {
        KoBracket b = bracket4();

        b.recordScore(1, 1, win3_0()); // p1 gagne
        b.recordScore(1, 2, win3_0()); // p3 gagne

        KoMatch finale = b.finalMatch();
        assertEquals(p1(), finale.player1());
        assertEquals(p3(), finale.player2());
    }

    @Test
    void recordScore_final_shouldDetermineChampion() {
        KoBracket b = bracket4();

        b.recordScore(1, 1, win3_0()); // p1 gagne
        b.recordScore(1, 2, win3_0()); // p3 gagne
        b.recordScore(2, 1, win3_0()); // p1 gagne la finale

        assertEquals(p1(), b.champion());
    }

    @Test
    void champion_beforeFinalPlayed_shouldBeNull() {
        KoBracket b = bracket4();
        assertNull(b.champion());
    }

    @Test
    void recordScore_alreadyFinished_shouldThrow() {
        KoBracket b = bracket4();
        b.recordScore(1, 1, win3_0());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> b.recordScore(1, 1, win3_0()));
        assertEquals(ErrorCode.BRACKET_MATCH_ALREADY_FINISHED, ex.getCode());
    }

    @Test
    void recordScore_unknownMatch_shouldThrow() {
        KoBracket b = bracket4();
        BusinessException ex = assertThrows(BusinessException.class,
                () -> b.recordScore(99, 99, win3_0()));
        assertEquals(ErrorCode.BRACKET_MATCH_NOT_FOUND, ex.getCode());
    }

    // =========================================================================
    // WALKOVER
    // =========================================================================

    @Test
    void walkover_shouldPropagateOpponentToNextRound() {
        KoBracket b = bracket4();

        // p2 déclare forfait dans le match R1P1 (p1 vs p2)
        b.declareWalkover(1, 1, p2());

        // p1 doit être propagé en finale
        assertEquals(p1(), b.finalMatch().player1());
    }

    @Test
    void walkover_shouldDetermineChampion() {
        KoBracket b = bracket4();

        b.declareWalkover(1, 1, p2()); // p1 gagne R1P1
        b.declareWalkover(1, 2, p4()); // p3 gagne R1P2
        b.recordScore(2, 1, PoolMatchScore.ofSetsOnly(0, 3)); // p3 gagne la finale

        assertEquals(p3(), b.champion());
    }

    // =========================================================================
    // BYE
    // =========================================================================

    @Test
    void byeMatch_shouldAutoAdvancePlayer() {
        // Créer un match BYE : player1 existe, player2 = null
        KoMatch byeMatch = new KoMatch(1, 1);
        byeMatch.assignPlayers(p1(), null); // null = BYE

        assertEquals(KoMatch.Status.BYE, byeMatch.status());
        assertEquals(p1(), byeMatch.winner());
        assertNull(byeMatch.loser());
    }

    @Test
    void byeMatch_bothNull_shouldStayPending() {
        // Les deux null = match pas encore peuplé = PENDING
        // BYE est déclenché explicitement via markAsBye() par le BracketBuilder
        KoMatch match = new KoMatch(1, 1);
        match.assignPlayers(null, null);
        assertEquals(KoMatch.Status.PENDING, match.status());
    }

    // =========================================================================
    // ÉTAT DU TABLEAU
    // =========================================================================

    @Test
    void isComplete_shouldReturnFalseWhenMatchesRemain() {
        KoBracket b = bracket4();
        assertFalse(b.isComplete());
    }

    @Test
    void isComplete_shouldReturnTrueWhenAllMatchesDone() {
        KoBracket b = bracket4();
        b.recordScore(1, 1, win3_0());
        b.recordScore(1, 2, win3_0());
        b.recordScore(2, 1, win3_0());
        assertTrue(b.isComplete());
    }

    @Test
    void startMatch_shouldSetInProgress() {
        KoBracket b = bracket4();
        b.startMatch(1, 1);
        assertEquals(KoMatch.Status.IN_PROGRESS,
                b.matchesForRound(1).get(0).status());
    }

    // =========================================================================
    // TABLEAU DE 8 (test de propagation sur 3 tours)
    // =========================================================================

    @Test
    void bracket8_fullTournament_shouldPropagateCorrectly() {
        // Tableau de 8 : 3 tours (QF, SF, Finale)
        // Round 1 : 4 matchs
        // Round 2 : 2 matchs
        // Round 3 : finale

        List<KoMatch> matches = new ArrayList<>();
        Participant[] players = {
                null, p1(), p2(), p3(), p4(),
                TestDataFactory.participantFrom(TestDataFactory.femaleSeniorVersailles()),
                TestDataFactory.participantFrom(TestDataFactory.maleBenjaminBrunoy()),
                TestDataFactory.participantFrom(TestDataFactory.femaleVeteran45Caen()),
                TestDataFactory.participantFrom(TestDataFactory.maleVeteran80Caen())
        };

        // Round 1 : 4 matchs
        for (int pos = 1; pos <= 4; pos++) {
            KoMatch m = new KoMatch(1, pos);
            m.assignPlayers(players[pos * 2 - 1], players[pos * 2]);
            matches.add(m);
        }
        // Round 2 : 2 matchs
        matches.add(new KoMatch(2, 1));
        matches.add(new KoMatch(2, 2));
        // Round 3 : finale
        matches.add(new KoMatch(3, 1));

        KoBracket b = new KoBracket("TBL_B", 8, matches);
        assertEquals(3, b.totalRounds());

        // Jouer tous les matchs : le player1 gagne à chaque fois
        b.recordScore(1, 1, win3_0()); // p1 gagne
        b.recordScore(1, 2, win3_0()); // p3 gagne
        b.recordScore(1, 3, win3_0()); // players[5] gagne
        b.recordScore(1, 4, win3_0()); // players[7] gagne

        // Vérifier que les demi-finales sont bien peuplées
        assertEquals(p1(), b.matchesForRound(2).get(0).player1());
        assertEquals(p3(), b.matchesForRound(2).get(0).player2());

        b.recordScore(2, 1, win3_0()); // p1 gagne la SF1
        b.recordScore(2, 2, win3_0()); // players[5] gagne la SF2

        // Finale
        assertEquals(p1(), b.finalMatch().player1());
        b.recordScore(3, 1, win3_0()); // p1 champion

        assertEquals(p1(), b.champion());
        assertTrue(b.isComplete());
    }
}