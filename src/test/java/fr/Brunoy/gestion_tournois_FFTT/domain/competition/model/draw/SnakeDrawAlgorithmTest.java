package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.draw;

import fr.Brunoy.gestion_tournois_FFTT.testutil.TestDataFactory;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.draw.DrawAlgorithmType;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.draw.SnakeDrawAlgorithm;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolSlot;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.FfttParticipant;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.AgeCategory;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.Gender;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.MedicalCertificateStatus;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SnakeDrawAlgorithmTest {

    // -------------------------------------------------------------------------
    // FIXTURES
    // -------------------------------------------------------------------------

    /**
     * Retourne une liste de N participants TOUS DISTINCTS.
     * On crée des GuestParticipant avec des IDs uniques pour éviter
     * le problème de indexOf() qui retourne la première occurrence.
     */
    private static List<Participant> ranked(int count) {
        List<Participant> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(TestDataFactory.guest(
                    "PLAYER-" + i,
                    "Joueur " + i,
                    Gender.MALE,
                    "FR",
                    AgeCategory.SENIOR,
                    MedicalCertificateStatus.VALIDE));
        }
        return list;
    }

    private static SnakeDrawAlgorithm algo() {
        return new SnakeDrawAlgorithm();
    }

    /** Retourne les seed ranks d'une poule triés par position. */
    private static List<Integer> seedRanks(List<PoolSlot> pool) {
        return pool.stream()
                .sorted((a, b) -> Integer.compare(a.positionInPool(), b.positionInPool()))
                .map(PoolSlot::seedRank)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // TYPE
    // =========================================================================

    @Test
    void type_shouldBeSnake() {
        assertEquals(DrawAlgorithmType.SNAKE, algo().type());
    }

    // =========================================================================
    // SERPENT — PLACEMENT DE BASE
    // =========================================================================

    @Test
    void draw_9players_3pools_shouldFollowSnakePattern() {
        // P1={J1,J6,J7}, P2={J2,J5,J8}, P3={J3,J4,J9}
        List<Participant> players = ranked(9);
        List<List<PoolSlot>> pools = algo().draw(players, 3);

        assertEquals(3, pools.size());
        assertEquals(List.of(1, 6, 7), seedRanks(pools.get(0)));
        assertEquals(List.of(2, 5, 8), seedRanks(pools.get(1)));
        assertEquals(List.of(3, 4, 9), seedRanks(pools.get(2)));
    }

    @Test
    void draw_6players_2pools_shouldFollowSnakePattern() {
        // P1={J1,J4,J5}, P2={J2,J3,J6}
        List<Participant> players = ranked(6);
        List<List<PoolSlot>> pools = algo().draw(players, 3);

        assertEquals(2, pools.size());
        assertEquals(List.of(1, 4, 5), seedRanks(pools.get(0)));
        assertEquals(List.of(2, 3, 6), seedRanks(pools.get(1)));
    }

    @Test
    void draw_12players_4pools_shouldFollowSnakePattern() {
        // P1={1,8,9}, P2={2,7,10}, P3={3,6,11}, P4={4,5,12}
        List<Participant> players = ranked(12);
        List<List<PoolSlot>> pools = algo().draw(players, 3);

        assertEquals(4, pools.size());
        assertEquals(List.of(1, 8, 9), seedRanks(pools.get(0)));
        assertEquals(List.of(2, 7, 10), seedRanks(pools.get(1)));
        assertEquals(List.of(3, 6, 11), seedRanks(pools.get(2)));
        assertEquals(List.of(4, 5, 12), seedRanks(pools.get(3)));
    }

    @Test
    void draw_4players_poolSize2_shouldGive2PoolsOf2() {
        // P1={J1,J4}, P2={J2,J3}
        List<Participant> players = ranked(4);
        List<List<PoolSlot>> pools = algo().draw(players, 2);

        assertEquals(2, pools.size());
        assertEquals(List.of(1, 4), seedRanks(pools.get(0)));
        assertEquals(List.of(2, 3), seedRanks(pools.get(1)));
    }

    @Test
    void draw_10players_poolSize3_shouldMake4Pools() {
        // ceil(10/3) = 4 poules, total = 10 joueurs
        List<Participant> players = ranked(10);
        List<List<PoolSlot>> pools = algo().draw(players, 3);

        assertEquals(4, pools.size());
        assertEquals(10, pools.stream().mapToLong(List::size).sum());
    }

    @Test
    void draw_positionInPool_shouldBeConsecutiveFrom1() {
        List<Participant> players = ranked(6);
        List<List<PoolSlot>> pools = algo().draw(players, 3);

        for (List<PoolSlot> pool : pools) {
            List<Integer> positions = pool.stream()
                    .map(PoolSlot::positionInPool)
                    .sorted()
                    .collect(Collectors.toList());
            for (int i = 0; i < positions.size(); i++) {
                assertEquals(i + 1, positions.get(i));
            }
        }
    }

    @Test
    void draw_seedRank_shouldMatchGlobalRank() {
        List<Participant> players = ranked(6);
        List<List<PoolSlot>> pools = algo().draw(players, 3);

        for (List<PoolSlot> pool : pools) {
            for (PoolSlot slot : pool) {
                int expectedSeed = players.indexOf(slot.participant()) + 1;
                assertEquals(expectedSeed, slot.seedRank());
            }
        }
    }

    @Test
    void draw_allPlayersShouldBePlacedExactlyOnce() {
        List<Participant> players = ranked(9);
        List<List<PoolSlot>> pools = algo().draw(players, 3);

        List<Participant> placed = pools.stream()
                .flatMap(pool -> pool.stream().map(PoolSlot::participant))
                .collect(Collectors.toList());

        assertEquals(9, placed.size());
        for (Participant p : players) {
            assertEquals(1, placed.stream().filter(x -> x.equals(p)).count(),
                    "Chaque joueur doit être placé exactement une fois");
        }
    }

    // =========================================================================
    // VALIDATION
    // =========================================================================

    @Test
    void draw_notEnoughPlayers_shouldThrow() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> algo().draw(ranked(2), 3));
        assertEquals(ErrorCode.DRAW_NOT_ENOUGH_PLAYERS, ex.getCode());
    }

    @Test
    void draw_nullList_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> algo().draw(null, 3));
    }

    @Test
    void draw_resultShouldBeImmutable() {
        List<List<PoolSlot>> pools = algo().draw(ranked(6), 3);
        assertThrows(UnsupportedOperationException.class,
                () -> pools.add(List.of()));
        assertThrows(UnsupportedOperationException.class,
                () -> pools.get(0).add(new PoolSlot(99, 1,
                        TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy()))));
    }

    // =========================================================================
    // CONTRAINTE DE CLUB
    // =========================================================================

    @Test
    void draw_sameClubPlayers_shouldNotBeInSamePool() {
        // J1 (Brunoy), J2 (Versailles), J3 (Caen), J4 (Brunoy) ← conflit serpent
        // Sans contrainte : J1 et J4 iraient en P1 (serpent : 1→P1, 4→P1)
        // Avec contrainte : J4 doit être déplacé
        List<Participant> players = new ArrayList<>();
        players.add(TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy())); // seed 1, Brunoy
        players.add(TestDataFactory.participantFrom(TestDataFactory.maleSeniorVersailles())); // seed 2, Versailles
        players.add(TestDataFactory.participantFrom(TestDataFactory.maleSeniorCaen())); // seed 3, Caen
        players.add(TestDataFactory.participantFrom(TestDataFactory.femaleJuniorBrunoy())); // seed 4, Brunoy
        players.add(TestDataFactory.participantFrom(TestDataFactory.femaleSeniorVersailles()));// seed 5
        players.add(TestDataFactory.participantFrom(TestDataFactory.femaleVeteran45Caen())); // seed 6

        List<List<PoolSlot>> pools = algo().draw(players, 3);

        for (List<PoolSlot> pool : pools) {
            long brunoyCount = pool.stream()
                    .map(PoolSlot::participant)
                    .filter(p -> p instanceof FfttParticipant fp
                            && "08911132".equals(fp.player().getClub().getNumber()))
                    .count();
            assertTrue(brunoyCount <= 1,
                    "Une poule ne doit pas contenir plus d'un joueur de Brunoy");
        }
    }

    @Test
    void draw_clubConstraint_brunoyPlayersShouldBeInDifferentPools() {
        List<Participant> players = new ArrayList<>();
        players.add(TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy())); // seed 1, Brunoy
        players.add(TestDataFactory.participantFrom(TestDataFactory.maleSeniorVersailles())); // seed 2
        players.add(TestDataFactory.participantFrom(TestDataFactory.maleSeniorCaen())); // seed 3
        players.add(TestDataFactory.participantFrom(TestDataFactory.femaleJuniorBrunoy())); // seed 4, Brunoy
        players.add(TestDataFactory.participantFrom(TestDataFactory.femaleSeniorVersailles()));// seed 5
        players.add(TestDataFactory.participantFrom(TestDataFactory.femaleVeteran45Caen())); // seed 6

        List<List<PoolSlot>> pools = algo().draw(players, 3);

        List<Integer> brunoyPoolIndexes = new ArrayList<>();
        for (int i = 0; i < pools.size(); i++) {
            for (PoolSlot slot : pools.get(i)) {
                if (slot.participant() instanceof FfttParticipant fp
                        && "08911132".equals(fp.player().getClub().getNumber())) {
                    brunoyPoolIndexes.add(i);
                }
            }
        }

        assertEquals(2, brunoyPoolIndexes.size());
        assertNotEquals(brunoyPoolIndexes.get(0), brunoyPoolIndexes.get(1),
                "Les deux joueurs de Brunoy doivent être dans des poules différentes");
    }

    @Test
    void draw_allSameClub_shouldNotThrow() {
        // Insoluble → l'algo ne doit pas planter
        List<Participant> players = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            players.add(TestDataFactory.participantFrom(TestDataFactory.maleSeniorBrunoy()));
        }
        assertDoesNotThrow(() -> {
            List<List<PoolSlot>> pools = algo().draw(players, 3);
            assertEquals(2, pools.size());
        });
    }

    @Test
    void draw_guestParticipants_shouldHaveNoClubConstraint() {
        // Les GuestParticipant n'ont pas de club → pas de contrainte entre eux
        List<Participant> players = ranked(6); // tous des GuestParticipant
        assertDoesNotThrow(() -> algo().draw(players, 3));
    }
}