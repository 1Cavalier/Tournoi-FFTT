package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.Poule;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolStanding;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Construit le tableau KO à partir des résultats des poules.
 *
 * Règles FFTT :
 * - Les nbByes mieux classés passent directement en round 2 (BYE).
 * - Les autres jouent le round 1.
 * - Les 1ers et 2èmes de la même poule sont dans des demi-tableaux opposés.
 *
 * Un match BYE = assignPlayers(joueur, null) → statut BYE automatique via XOR.
 */
public final class BracketBuilder {

    public KoBracket build(String tableauCode, List<Poule> poules) {
        Objects.requireNonNull(tableauCode, "tableauCode");
        Objects.requireNonNull(poules, "poules");

        if (poules.isEmpty()) {
            throw new BusinessException(ErrorCode.BRACKET_NO_QUALIFIED_PLAYERS);
        }

        // 1. Récupérer les qualifiés triés par numéro de poule
        List<Poule> sortedPoules = poules.stream()
                .sorted(Comparator.comparingInt(Poule::poolNumber))
                .collect(Collectors.toList());

        List<Participant> firstPlaces = new ArrayList<>();
        List<Participant> secondPlaces = new ArrayList<>();

        for (Poule poule : sortedPoules) {
            List<PoolStanding> standings = poule.computeStandings();
            int qCount = poule.qualifiedCount();
            List<Participant> qualified = standings.stream()
                    .filter(s -> s.isQualified(qCount))
                    .map(PoolStanding::participant)
                    .collect(Collectors.toList());
            if (qualified.size() >= 1)
                firstPlaces.add(qualified.get(0));
            if (qualified.size() >= 2)
                secondPlaces.add(qualified.get(1));
        }

        int totalQualified = firstPlaces.size() + secondPlaces.size();
        if (totalQualified == 0) {
            throw new BusinessException(ErrorCode.BRACKET_NO_QUALIFIED_PLAYERS);
        }

        // 2. Taille du tableau et BYE
        int bracketSize = nextPowerOfTwo(totalQualified);
        int totalRounds = (int) (Math.log(bracketSize) / Math.log(2));
        int nbByes = bracketSize - totalQualified;

        // 3. Répartir BYE et round1Players
        List<Participant> allRanked = new ArrayList<>();
        allRanked.addAll(firstPlaces);
        allRanked.addAll(secondPlaces);

        List<Participant> byePlayers = new ArrayList<>(allRanked.subList(0, nbByes));
        List<Participant> round1Players = new ArrayList<>(allRanked.subList(nbByes, totalQualified));

        // 4. Créer tous les matchs KO
        List<KoMatch> allMatches = new ArrayList<>();
        for (int round = 1; round <= totalRounds; round++) {
            int n = bracketSize / (int) Math.pow(2, round);
            for (int pos = 1; pos <= n; pos++) {
                allMatches.add(new KoMatch(round, pos));
            }
        }
        Map<String, KoMatch> matchIdx = new LinkedHashMap<>();
        for (KoMatch m : allMatches)
            matchIdx.put(m.round() + "-" + m.position(), m);

        // 5. Placer les joueurs de round 1 dans les matchs
        int nbR1Matches = round1Players.size() / 2;
        int halfR1 = Math.max(1, nbR1Matches / 2);

        List<Participant> r1Firsts = round1Players.stream()
                .filter(firstPlaces::contains).collect(Collectors.toList());
        List<Participant> r1Seconds = round1Players.stream()
                .filter(secondPlaces::contains).collect(Collectors.toList());

        int slotSize = nbR1Matches * 2;
        Participant[] slots = new Participant[slotSize + 1]; // 1-indexed

        // 1ers en slots impairs, alternance haut/bas
        int highOdd = 1;
        int lowOdd = halfR1 * 2 + 1;
        for (int i = 0; i < r1Firsts.size(); i++) {
            if (i % 2 == 0 && highOdd <= halfR1 * 2) {
                slots[highOdd] = r1Firsts.get(i);
                highOdd += 2;
            } else if (lowOdd <= slotSize) {
                slots[lowOdd] = r1Firsts.get(i);
                lowOdd += 2;
            }
        }

        // 2èmes dans les slots pairs libres, demi opposé de leur 1er
        List<Participant> secondsForLow = new ArrayList<>();
        List<Participant> secondsForHigh = new ArrayList<>();
        for (int i = 0; i < r1Seconds.size(); i++) {
            if (i % 2 == 0)
                secondsForLow.add(r1Seconds.get(i));
            else
                secondsForHigh.add(r1Seconds.get(i));
        }
        Collections.shuffle(secondsForHigh);
        Collections.shuffle(secondsForLow);

        List<Integer> fHigh = freePairSlots(slots, 1, halfR1 * 2);
        List<Integer> fLow = freePairSlots(slots, halfR1 * 2 + 1, slotSize);
        assignSlots(slots, fHigh, secondsForHigh);
        assignSlots(slots, fLow, secondsForLow);

        // Remplir les slots impairs restants avec les 2èmes non encore placés
        List<Participant> unplaced = round1Players.stream()
                .filter(p -> {
                    for (int i = 1; i <= slotSize; i++) {
                        if (p.equals(slots[i]))
                            return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        List<Integer> freeOdd = new ArrayList<>();
        for (int i = 1; i <= slotSize; i++) {
            if (i % 2 == 1 && slots[i] == null)
                freeOdd.add(i);
        }
        assignSlots(slots, freeOdd, unplaced);

        // 6. Assigner les joueurs aux matchs de round 1
        for (int pos = 1; pos <= nbR1Matches; pos++) {
            KoMatch match = matchIdx.get("1-" + pos);
            if (match == null)
                continue;
            Participant p1 = slots[2 * pos - 1];
            Participant p2 = slots[2 * pos];
            match.assignPlayers(p1, p2);
            // Si BYE (un slot null), propager immédiatement
            if (match.status() == KoMatch.Status.BYE) {
                propagate(match, matchIdx, totalRounds);
            }
        }

        // 7. Placer les joueurs BYE en round 2
        // assignPlayers(joueur, null) → statut BYE automatique via XOR dans KoMatch
        Collections.shuffle(byePlayers);
        int r2Total = bracketSize / 4;
        int r2Half = Math.max(1, r2Total / 2);
        int highBye = 1;
        int lowBye = r2Half + 1;

        for (int i = 0; i < byePlayers.size(); i++) {
            int pos = (i % 2 == 0) ? highBye++ : lowBye++;
            if (pos > r2Total)
                pos = highBye++;
            KoMatch match = matchIdx.get("2-" + pos);
            if (match == null)
                continue;
            match.assignPlayers(byePlayers.get(i), null); // → BYE automatique
            propagate(match, matchIdx, totalRounds);
        }

        return new KoBracket(tableauCode, bracketSize, allMatches);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private void propagate(KoMatch finished, Map<String, KoMatch> idx, int totalRounds) {
        Participant winner = finished.winner();
        if (winner == null)
            return;
        int nextRound = finished.round() + 1;
        int nextPosition = (int) Math.ceil(finished.position() / 2.0);
        if (nextRound > totalRounds)
            return;
        KoMatch next = idx.get(nextRound + "-" + nextPosition);
        if (next == null)
            return;
        if (finished.position() % 2 == 1)
            next.assignPlayers(winner, next.player2());
        else
            next.assignPlayers(next.player1(), winner);
    }

    private List<Integer> freePairSlots(Participant[] slots, int from, int to) {
        List<Integer> free = new ArrayList<>();
        int limit = Math.min(to, slots.length - 1);
        for (int i = from; i <= limit; i++) {
            if (i % 2 == 0 && slots[i] == null)
                free.add(i);
        }
        return free;
    }

    private void assignSlots(Participant[] slots, List<Integer> freeSlots,
            List<Participant> players) {
        int limit = Math.min(freeSlots.size(), players.size());
        for (int i = 0; i < limit; i++)
            slots[freeSlots.get(i)] = players.get(i);
    }

    private static int nextPowerOfTwo(int n) {
        if (n <= 1)
            return 2;
        int p = 1;
        while (p < n)
            p <<= 1;
        return p;
    }
}