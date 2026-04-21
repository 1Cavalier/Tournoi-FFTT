package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.Poule;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolStanding;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service domaine responsable de la construction du tableau KO
 * à partir des résultats des poules.
 *
 * <h2>Règles de placement FFTT (article I.305.2 et I.305.4)</h2>
 *
 * <p>
 * Avec 2 qualifiés par poule :
 * </p>
 * <ol>
 * <li>Les 1ers de poule sont placés dans le tableau dans l'ordre des poules
 * (poule 1 → position 1, poule 2 → position 3, poule 3 → position 5, etc.)
 * en alternant les demi-tableaux (haut / bas).</li>
 * <li>Les 2èmes de poule sont placés dans le <strong>demi-tableau
 * opposé</strong>
 * à leur 1er respectif, par tirage au sort entre eux.</li>
 * <li>Les BYE (exemptions) sont ajoutés si le nombre de qualifiés
 * n'est pas une puissance de 2. Les BYE sont attribués aux positions
 * correspondant aux mieux classés (rangs serpent les plus faibles).</li>
 * </ol>
 *
 * <h2>Exemple : 6 poules de 3 → 12 qualifiés → tableau de 16</h2>
 *
 * <pre>
 *   Taille du tableau : 16 (prochaine puissance de 2 ≥ 12)
 *   BYE : 4 (16 - 12)
 *
 *   Positions 1..8 = demi-tableau HAUT
 *   Positions 9..16 = demi-tableau BAS
 *
 *   1ers de poule (6) placés : pos 1, 5, 9, 13, 3, 7 (alternance H/B)
 *   2èmes de poule (6) placés dans le demi-tableau opposé de leur 1er.
 *   Les 4 BYE vont aux positions restantes, attribuées aux mieux classés.
 * </pre>
 */
public final class BracketBuilder {

    /**
     * Construit le tableau KO à partir de la liste des poules finalisées.
     *
     * @param tableauCode code du tableau
     * @param poules      liste des poules (toutes les parties doivent être
     *                    terminées)
     * @return le KoBracket prêt à être joué
     */
    public KoBracket build(String tableauCode, List<Poule> poules) {
        Objects.requireNonNull(tableauCode, "tableauCode");
        Objects.requireNonNull(poules, "poules");

        if (poules.isEmpty()) {
            throw new BusinessException(ErrorCode.BRACKET_NO_QUALIFIED_PLAYERS);
        }

        // ---- 1. Récupérer les standings par poule ----
        List<Poule> sortedPoules = poules.stream()
                .sorted(Comparator.comparingInt(Poule::poolNumber))
                .collect(Collectors.toList());

        List<Participant> firstPlaces = new ArrayList<>();
        List<Participant> secondPlaces = new ArrayList<>();

        for (Poule poule : sortedPoules) {
            List<PoolStanding> standings = poule.computeStandings();
            int qCount = poule.qualifiedCount(); // toujours 2

            // PoolStanding::isQualified prend un int → on ne peut pas utiliser
            // une method reference, on passe par une lambda
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

        // ---- 2. Taille du tableau (prochaine puissance de 2) ----
        int bracketSize = nextPowerOfTwo(totalQualified);
        int totalRounds = (int) (Math.log(bracketSize) / Math.log(2));
        int nbByes = bracketSize - totalQualified;

        // ---- 3. Créer tous les matchs du tableau ----
        List<KoMatch> allMatches = new ArrayList<>();
        for (int round = 1; round <= totalRounds; round++) {
            int nbMatchesInRound = bracketSize / (int) Math.pow(2, round);
            for (int pos = 1; pos <= nbMatchesInRound; pos++) {
                allMatches.add(new KoMatch(round, pos));
            }
        }

        // Indexer les matchs du premier tour par position
        Map<Integer, KoMatch> round1Matches = new LinkedHashMap<>();
        for (KoMatch m : allMatches) {
            if (m.round() == 1)
                round1Matches.put(m.position(), m);
        }

        // ---- 4. Placer les joueurs au 1er tour ----
        int halfSize = bracketSize / 2;
        int quarterSize = bracketSize / 4;

        Participant[] slots = new Participant[bracketSize + 1]; // 1-indexed

        // Placer les 1ers de poule en alternant haut / bas
        int highSlot = 1;
        int lowSlot = halfSize + 1;

        for (int i = 0; i < firstPlaces.size(); i++) {
            if (i % 2 == 0) {
                slots[highSlot] = firstPlaces.get(i);
                highSlot += 2;
            } else {
                slots[lowSlot] = firstPlaces.get(i);
                lowSlot += 2;
            }
        }

        // Placer les 2èmes dans le demi-tableau opposé de leur 1er
        List<Integer> freeSlotsHigh = findFreeSlots(slots, 1, halfSize);
        List<Integer> freeSlotsLow = findFreeSlots(slots, halfSize + 1, bracketSize);

        List<Participant> secondsForHigh = new ArrayList<>();
        List<Participant> secondsForLow = new ArrayList<>();

        for (int i = 0; i < secondPlaces.size(); i++) {
            if (i % 2 == 0) {
                secondsForLow.add(secondPlaces.get(i)); // 1er était en haut → 2ème en bas
            } else {
                secondsForHigh.add(secondPlaces.get(i)); // 1er était en bas → 2ème en haut
            }
        }

        // Tirage au sort des 2èmes (règle FFTT)
        Collections.shuffle(secondsForHigh);
        Collections.shuffle(secondsForLow);

        assignToFreeSlots(slots, freeSlotsHigh, secondsForHigh);
        assignToFreeSlots(slots, freeSlotsLow, secondsForLow);

        // ---- 5. Assigner les joueurs aux matchs du 1er tour ----
        for (int pos = 1; pos <= halfSize; pos++) {
            KoMatch match = round1Matches.get(pos);
            if (match == null)
                continue;

            int slotIndex1 = (pos - 1) * 2 + 1;
            int slotIndex2 = (pos - 1) * 2 + 2;

            Participant p1 = slotIndex1 <= bracketSize ? slots[slotIndex1] : null;
            Participant p2 = slotIndex2 <= bracketSize ? slots[slotIndex2] : null;

            match.assignPlayers(p1, p2);
            if (match.status() == KoMatch.Status.BYE) {
                propagateBye(match, allMatches);
            }
        }

        return new KoBracket(tableauCode, bracketSize, allMatches);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private void propagateBye(KoMatch byeMatch, List<KoMatch> allMatches) {
        Participant winner = byeMatch.winner();
        int nextRound = byeMatch.round() + 1;
        int nextPosition = (int) Math.ceil(byeMatch.position() / 2.0);
        boolean isPlayer1 = (byeMatch.position() % 2 == 1);

        allMatches.stream()
                .filter(m -> m.round() == nextRound && m.position() == nextPosition)
                .findFirst()
                .ifPresent(nextMatch -> {
                    if (isPlayer1)
                        nextMatch.assignPlayers(winner, nextMatch.player2());
                    else
                        nextMatch.assignPlayers(nextMatch.player1(), winner);
                });
    }

    private List<Integer> findFreeSlots(Participant[] slots, int from, int to) {
        List<Integer> free = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            if (slots[i] == null)
                free.add(i);
        }
        return free;
    }

    private void assignToFreeSlots(Participant[] slots,
            List<Integer> freeSlots,
            List<Participant> participants) {
        int limit = Math.min(freeSlots.size(), participants.size());
        for (int i = 0; i < limit; i++) {
            slots[freeSlots.get(i)] = participants.get(i);
        }
    }

    private static int nextPowerOfTwo(int n) {
        if (n <= 1)
            return 2;
        int power = 1;
        while (power < n)
            power <<= 1;
        return power;
    }
}