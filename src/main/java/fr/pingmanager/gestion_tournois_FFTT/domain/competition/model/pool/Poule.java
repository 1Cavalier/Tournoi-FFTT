package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregate représentant une poule dans un tableau de tournoi.
 *
 * Responsabilités :
 * 1. Contenir les slots (joueurs tirés)
 * 2. Générer les matchs dans l'ordre FFTT selon la taille de la poule
 * 3. Recevoir les scores des matchs
 * 4. Calculer le classement final avec départage FFTT complet
 * 5. Exposer les qualifiés pour le tableau KO
 *
 * Tailles supportées :
 * - Poule de 2 : 1 match (pos1 vs pos2), 1 qualifié (le vainqueur)
 * - Poule de 3 : 3 matchs (1v3, 1v2, 2v3), 2 qualifiés (1er et 2ème)
 *
 * Invariants :
 * - Une poule contient 2 ou 3 slots et les matchs correspondants
 * - Un slot par participant (pas de doublon)
 * - Les matchs sont générés une seule fois à la création
 */
public final class Poule {

    // -------------------------------------------------------------------------
    // CONSTANTES
    // -------------------------------------------------------------------------

    public static final int MIN_POOL_SIZE = 2;
    public static final int MAX_POOL_SIZE = 3;

    /** Points-parties pour une victoire. */
    private static final int WIN_POINTS = 2;

    /** Points-parties pour une défaite. */
    private static final int LOSS_POINTS = 1;

    /** Points-parties pour un forfait / walkover. */
    private static final int WALKOVER_POINTS = 0;

    // -------------------------------------------------------------------------
    // CHAMPS
    // -------------------------------------------------------------------------

    private final String id;

    /** Identifiant du tableau auquel appartient cette poule. */
    private final String tableauCode;

    /**
     * Numéro de la poule dans le tableau (1, 2, 3...). Affiché "A", "B"... en UI.
     */
    private final int poolNumber;

    /** Les slots (2 ou 3), ordonnés par positionInPool. */
    private final List<PoolSlot> slots;

    /** Les matchs générés selon la taille de la poule. */
    private final List<PoolMatch> matches;

    /**
     * Nombre de joueurs qualifiés pour le KO depuis cette poule.
     * Défini par le tableau (1 ou 2). Défaut : 2.
     */
    private final int qualifiedPerPool;

    // -------------------------------------------------------------------------
    // CONSTRUCTEUR (nouvelle poule)
    // -------------------------------------------------------------------------

    public Poule(String tableauCode, int poolNumber, List<PoolSlot> slots) {
        this(tableauCode, poolNumber, slots, 2);
    }

    public Poule(String tableauCode, int poolNumber, List<PoolSlot> slots, int qualifiedPerPool) {
        this.id = UUID.randomUUID().toString();
        this.tableauCode = requireText(tableauCode);
        this.poolNumber = requirePositive(poolNumber, "poolNumber");
        this.qualifiedPerPool = validateQualifiedPerPool(qualifiedPerPool, slots.size());

        validateSlots(slots);
        this.slots = Collections.unmodifiableList(new ArrayList<>(slots));
        this.matches = Collections.unmodifiableList(generateMatches(slots));
    }

    /**
     * Constructeur de reconstruction depuis la base (avec id et matchs déjà créés).
     */
    public Poule(String id, String tableauCode, int poolNumber,
            List<PoolSlot> slots, List<PoolMatch> matches) {
        this(id, tableauCode, poolNumber, slots, matches, 2);
    }

    public Poule(String id, String tableauCode, int poolNumber,
            List<PoolSlot> slots, List<PoolMatch> matches, int qualifiedPerPool) {

        this.id = Objects.requireNonNull(id, "id");
        this.tableauCode = requireText(tableauCode);
        this.poolNumber = requirePositive(poolNumber, "poolNumber");
        this.qualifiedPerPool = validateQualifiedPerPool(qualifiedPerPool, slots.size());

        validateSlots(slots);
        this.slots = Collections.unmodifiableList(new ArrayList<>(slots));
        this.matches = Collections.unmodifiableList(new ArrayList<>(matches));
    }

    // -------------------------------------------------------------------------
    // GÉNÉRATION DES MATCHS
    // -------------------------------------------------------------------------

    /**
     * Génère les matchs selon la taille de la poule :
     *
     * Poule de 2 (1 match) :
     * Match 1 : pos1 vs pos2
     *
     * Poule de 3 (3 matchs, ordre officiel FFTT) :
     * Match 1 : pos1 vs pos3
     * Match 2 : pos1 vs pos2
     * Match 3 : pos2 vs pos3
     *
     * L'ordre FFTT garantit que le match entre les deux meilleurs (1 vs 2)
     * se joue en deuxième, protégeant le n°1 qui rencontre le plus faible
     * en premier.
     */
    private static List<PoolMatch> generateMatches(List<PoolSlot> slots) {
        PoolSlot s1 = slotAt(slots, 1);
        PoolSlot s2 = slotAt(slots, 2);

        if (slots.size() == 2) {
            return List.of(
                    new PoolMatch(s1, s2, 1) // seul match : pos1 vs pos2
            );
        }

        // Poule de 3
        PoolSlot s3 = slotAt(slots, 3);
        return List.of(
                new PoolMatch(s1, s3, 1), // match 1 : pos1 vs pos3
                new PoolMatch(s1, s2, 2), // match 2 : pos1 vs pos2
                new PoolMatch(s2, s3, 3) // match 3 : pos2 vs pos3
        );
    }

    // -------------------------------------------------------------------------
    // ACTIONS MÉTIER
    // -------------------------------------------------------------------------

    /**
     * Enregistre le score d'un match identifié par son numéro d'ordre (1, 2 ou 3).
     */
    public void recordScore(int matchOrder, PoolMatchScore score) {
        PoolMatch match = findMatchByOrder(matchOrder);
        match.recordScore(score);
    }

    /**
     * Démarre un match (passe à IN_PROGRESS).
     */
    public void startMatch(int matchOrder) {
        findMatchByOrder(matchOrder).start();
    }

    /**
     * Déclare un forfait pour un joueur dans un match.
     */
    public void declareWalkover(int matchOrder, Participant participant) {
        findMatchByOrder(matchOrder).declareWalkover(participant);
    }

    // -------------------------------------------------------------------------
    // CLASSEMENT (cœur métier)
    // -------------------------------------------------------------------------

    /**
     * Calcule et retourne les standings de la poule, triés du 1er au 3ème.
     *
     * Algorithme de classement FFTT (article I.7.3 du Manuel JA) :
     *
     * Étape 0 : vérification que tous les matchs sont terminés
     * Étape 1 : tri par points-parties décroissants
     * Étape 2 : pour les ex-aequo → résultats entre eux seulement
     * Étape 3 : si toujours ex-aequo → quotient manches entre eux
     * Étape 4 : si toujours ex-aequo → quotient points-jeu entre eux
     * Étape 5 : tirage au sort (retourne rank=0 pour signaler au JA)
     *
     * Cas walkover : le joueur forfait marque 0 points-parties.
     * De plus, ses résultats sont annulés pour les autres joueurs
     * (règle FFTT : "Le classement de la poule est établi en annulant
     * les résultats des parties auxquelles il a participé").
     */
    public List<PoolStanding> computeStandings() {
        if (!allMatchesFinished()) {
            throw new BusinessException(ErrorCode.POOL_NOT_ALL_MATCHES_FINISHED);
        }

        // Détecter les joueurs forfait
        Set<String> walkoverIds = findWalkoverParticipantIds();

        // Construire les accumulateurs bruts (tous matchs confondus)
        Map<String, StandingAccumulator> accs = new LinkedHashMap<>();
        for (PoolSlot slot : slots) {
            accs.put(slot.participant().participantId(),
                    new StandingAccumulator(slot.participant()));
        }

        // Accumuler uniquement les matchs qui comptent :
        // un match est annulé si l'un des deux joueurs est en walkover
        for (PoolMatch match : matches) {
            String id1 = match.slot1().participant().participantId();
            String id2 = match.slot2().participant().participantId();

            boolean p1Walkover = walkoverIds.contains(id1);
            boolean p2Walkover = walkoverIds.contains(id2);

            if (p1Walkover || p2Walkover) {
                // Le match est annulé pour le classement des autres joueurs
                // Le joueur forfait garde 0 dans son accumulateur
                if (p1Walkover)
                    accs.get(id1).hasWalkover = true;
                if (p2Walkover)
                    accs.get(id2).hasWalkover = true;
                continue; // on n'accule pas ce match
            }

            // Match normal ou walkover (déjà exclu ci-dessus)
            accumulate(accs, match);
        }

        // Convertir en liste de standings sans rang (rank = 0 provisoire)
        List<StandingAccumulator> list = new ArrayList<>(accs.values());

        // Trier et attribuer les rangs
        assignRanks(list);

        // Construire les PoolStanding finaux
        return list.stream()
                .sorted(Comparator.comparingInt(a -> a.rank))
                .map(StandingAccumulator::toStanding)
                .collect(Collectors.toList());
    }

    /**
     * Nombre de joueurs qualifiés pour le tableau KO depuis cette poule.
     * Défini par le tableau (1 ou 2).
     * Pour une poule de 2, au maximum 2 qualifiés (les deux joueurs).
     */
    public int qualifiedCount() {
        return qualifiedPerPool;
    }

    /**
     * Retourne les participants qualifiés pour le tableau KO.
     * Lance une exception si tous les matchs ne sont pas terminés.
     */
    public List<Participant> qualifiedParticipants() {
        int qCount = qualifiedCount();
        return computeStandings().stream()
                .filter(s -> !s.hasWalkover() && s.rank() >= 1 && s.rank() <= qCount)
                .map(PoolStanding::participant)
                .collect(Collectors.toList());
    }

    /**
     * Vrai si tous les matchs sont terminés (COMPLETED ou WALKOVER).
     */
    public boolean allMatchesFinished() {
        return matches.stream().allMatch(PoolMatch::isFinished);
    }

    // -------------------------------------------------------------------------
    // ALGORITHME DE CLASSEMENT (privé)
    // -------------------------------------------------------------------------

    /**
     * Accule les stats d'un match dans les accumulateurs des deux joueurs.
     */
    private static void accumulate(Map<String, StandingAccumulator> accs, PoolMatch match) {
        Participant p1 = match.slot1().participant();
        Participant p2 = match.slot2().participant();
        StandingAccumulator a1 = accs.get(p1.participantId());
        StandingAccumulator a2 = accs.get(p2.participantId());

        if (match.status() == PoolMatch.Status.WALKOVER) {
            // Le walkover est déjà filtré avant, mais sécurité
            return;
        }

        PoolMatchScore score = match.score();

        if (score.player1Wins()) {
            a1.matchPoints += WIN_POINTS;
            a1.matchesWon++;
            a2.matchPoints += LOSS_POINTS;
            a2.matchesLost++;
        } else {
            a2.matchPoints += WIN_POINTS;
            a2.matchesWon++;
            a1.matchPoints += LOSS_POINTS;
            a1.matchesLost++;
        }

        a1.setsWon += score.setsWonByPlayer1();
        a1.setsLost += score.setsWonByPlayer2();
        a1.pointsWon += score.totalPointsPlayer1();
        a1.pointsLost += score.totalPointsPlayer2();

        a2.setsWon += score.setsWonByPlayer2();
        a2.setsLost += score.setsWonByPlayer1();
        a2.pointsWon += score.totalPointsPlayer2();
        a2.pointsLost += score.totalPointsPlayer1();
    }

    /**
     * Assigne les rangs en traitant les ex-aequo avec le départage FFTT.
     */
    private void assignRanks(List<StandingAccumulator> list) {

        // Les joueurs walkover sont toujours derniers
        List<StandingAccumulator> normal = list.stream()
                .filter(a -> !a.hasWalkover).collect(Collectors.toList());
        List<StandingAccumulator> walkover = list.stream()
                .filter(a -> a.hasWalkover).collect(Collectors.toList());

        // Trier les joueurs normaux par matchPoints décroissants
        normal.sort((a, b) -> Integer.compare(b.matchPoints, a.matchPoints));

        // Traiter les groupes ex-aequo
        int rank = 1;
        int i = 0;
        while (i < normal.size()) {
            int pts = normal.get(i).matchPoints;
            // Trouver tous les joueurs avec les mêmes pts
            List<StandingAccumulator> group = new ArrayList<>();
            while (i < normal.size() && normal.get(i).matchPoints == pts) {
                group.add(normal.get(i++));
            }

            if (group.size() == 1) {
                group.get(0).rank = rank;
            } else {
                // Départage entre les joueurs du groupe
                resolveDrawGroup(group, rank);
                // Trier le groupe après départage (l'ordre interne est maintenant significatif)
                group.sort(Comparator.comparingInt(a -> a.rank));
            }
            rank += group.size();
        }

        // Rang des joueurs walkover (après tous les autres)
        for (StandingAccumulator wk : walkover) {
            wk.rank = rank++;
        }
    }

    /**
     * Résout les ex-aequo au sein d'un groupe :
     * 1. Résultats des matchs entre eux seulement
     * 2. Quotient manches entre eux
     * 3. Quotient points-jeu entre eux
     * 4. Rank 0 (tirage au sort par le JA)
     *
     * @param startRank rang global du premier joueur du groupe
     */
    private void resolveDrawGroup(List<StandingAccumulator> group, int startRank) {

        // Calculer les stats entre les joueurs du groupe seulement
        Set<String> groupIds = group.stream()
                .map(a -> a.participant.participantId())
                .collect(Collectors.toSet());

        // Réinitialiser les compteurs intra-groupe
        Map<String, IntraGroupStats> intra = new LinkedHashMap<>();
        for (StandingAccumulator acc : group) {
            intra.put(acc.participant.participantId(), new IntraGroupStats());
        }

        for (PoolMatch match : matches) {
            String id1 = match.slot1().participant().participantId();
            String id2 = match.slot2().participant().participantId();

            if (!groupIds.contains(id1) || !groupIds.contains(id2))
                continue;
            if (!match.isFinished() || match.status() == PoolMatch.Status.WALKOVER)
                continue;

            PoolMatchScore score = match.score();
            IntraGroupStats s1 = intra.get(id1);
            IntraGroupStats s2 = intra.get(id2);

            if (score.player1Wins()) {
                s1.matchPts += WIN_POINTS;
                s1.won++;
                s2.matchPts += LOSS_POINTS;
                s2.lost++;
            } else {
                s2.matchPts += WIN_POINTS;
                s2.won++;
                s1.matchPts += LOSS_POINTS;
                s1.lost++;
            }
            s1.setsWon += score.setsWonByPlayer1();
            s1.setsLost += score.setsWonByPlayer2();
            s1.pointsWon += score.totalPointsPlayer1();
            s1.pointsLost += score.totalPointsPlayer2();
            s2.setsWon += score.setsWonByPlayer2();
            s2.setsLost += score.setsWonByPlayer1();
            s2.pointsWon += score.totalPointsPlayer2();
            s2.pointsLost += score.totalPointsPlayer1();
        }

        // Critère 1 : points-parties intra-groupe
        group.sort((a, b) -> Integer.compare(
                intra.get(b.participant.participantId()).matchPts,
                intra.get(a.participant.participantId()).matchPts));

        if (isResolved(group, intra, c -> c.matchPts)) {
            assignInOrder(group, startRank);
            return;
        }

        // Critère 2 : quotient manches intra-groupe
        group.sort((a, b) -> Double.compare(
                intra.get(b.participant.participantId()).setsQuotient(),
                intra.get(a.participant.participantId()).setsQuotient()));

        if (isResolved(group, intra, c -> (int) (c.setsQuotient() * 1000))) {
            assignInOrder(group, startRank);
            return;
        }

        // Critère 3 : quotient points-jeu intra-groupe
        group.sort((a, b) -> Double.compare(
                intra.get(b.participant.participantId()).pointsQuotient(),
                intra.get(a.participant.participantId()).pointsQuotient()));

        if (isResolved(group, intra, c -> (int) (c.pointsQuotient() * 1000))) {
            assignInOrder(group, startRank);
            return;
        }

        // Critère 4 : tirage au sort → rank = 0 pour signaler au JA
        for (StandingAccumulator acc : group) {
            acc.rank = 0;
        }
    }

    /**
     * Vérifie si tous les joueurs du groupe ont des valeurs différentes
     * selon l'extracteur de critère donné.
     */
    private boolean isResolved(List<StandingAccumulator> group,
            Map<String, IntraGroupStats> intra,
            java.util.function.ToIntFunction<IntraGroupStats> extractor) {
        Set<Integer> values = new HashSet<>();
        for (StandingAccumulator acc : group) {
            int val = extractor.applyAsInt(intra.get(acc.participant.participantId()));
            if (!values.add(val))
                return false;
        }
        return true;
    }

    /**
     * Assigne les rangs globaux dans l'ordre actuel de la liste (déjà triée).
     * 
     * @param startRank rang global du premier joueur du groupe
     */
    private void assignInOrder(List<StandingAccumulator> group, int startRank) {
        for (int i = 0; i < group.size(); i++) {
            group.get(i).rank = startRank + i;
        }
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private Set<String> findWalkoverParticipantIds() {
        Set<String> ids = new HashSet<>();
        for (PoolMatch m : matches) {
            if (m.status() == PoolMatch.Status.WALKOVER) {
                ids.add(m.walkoverId());
            }
        }
        return ids;
    }

    private PoolMatch findMatchByOrder(int order) {
        return matches.stream()
                .filter(m -> m.matchOrderInPool() == order)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.POOL_MATCH_NOT_FOUND));
    }

    private static PoolSlot slotAt(List<PoolSlot> slots, int position) {
        return slots.stream()
                .filter(s -> s.positionInPool() == position)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No slot at position " + position));
    }

    private static void validateSlots(List<PoolSlot> slots) {
        if (slots == null
                || slots.size() < MIN_POOL_SIZE
                || slots.size() > MAX_POOL_SIZE) {
            throw new BusinessException(ErrorCode.POOL_INVALID_SIZE);
        }
        Set<String> ids = new HashSet<>();
        for (PoolSlot slot : slots) {
            if (!ids.add(slot.participant().participantId())) {
                throw new BusinessException(ErrorCode.POOL_DUPLICATE_PARTICIPANT);
            }
        }
    }

    private static String requireText(String s) {
        if (s == null || s.isBlank())
            throw new IllegalArgumentException("tableauCode required");
        return s.trim().toUpperCase();
    }

    private static int requirePositive(int v, String name) {
        if (v < 1)
            throw new IllegalArgumentException(name + " must be >= 1");
        return v;
    }

    private static int validateQualifiedPerPool(int qp, int poolSize) {
        if (qp < 1 || qp > 2)
            throw new IllegalArgumentException("qualifiedPerPool must be 1 or 2, got: " + qp);
        // Pour une poule de 2, on peut qualifier au max 2 (les deux joueurs)
        // Pour une poule de 3+, 1 ou 2 qualifiés sont valides
        if (qp > poolSize)
            throw new IllegalArgumentException(
                    "qualifiedPerPool (" + qp + ") cannot exceed pool size (" + poolSize + ")");
        return qp;
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public String id() {
        return id;
    }

    public String tableauCode() {
        return tableauCode;
    }

    public int poolNumber() {
        return poolNumber;
    }

    public int poolSize() {
        return slots.size();
    }

    public int qualifiedPerPool() {
        return qualifiedPerPool;
    }

    /** Lettre de la poule pour l'affichage ("A", "B"...). */
    public String poolLabel() {
        return String.valueOf((char) ('A' + poolNumber - 1));
    }

    public List<PoolSlot> slots() {
        return slots;
    }

    public List<PoolMatch> matches() {
        return matches;
    }

    // -------------------------------------------------------------------------
    // CLASSES INTERNES (accumulateurs de calcul)
    // -------------------------------------------------------------------------

    /** Accumulateur mutable utilisé pendant le calcul du classement. */
    private static final class StandingAccumulator {
        final Participant participant;
        int matchPoints = 0;
        int matchesWon = 0;
        int matchesLost = 0;
        boolean hasWalkover = false;
        int setsWon = 0;
        int setsLost = 0;
        int pointsWon = 0;
        int pointsLost = 0;
        int rank = 0;

        StandingAccumulator(Participant participant) {
            this.participant = participant;
        }

        PoolStanding toStanding() {
            return new PoolStanding(participant, matchPoints, matchesWon,
                    matchesLost, hasWalkover, setsWon, setsLost,
                    pointsWon, pointsLost, rank);
        }
    }

    /** Stats intra-groupe pour le départage. */
    private static final class IntraGroupStats {
        int matchPts = 0;
        int won = 0;
        int lost = 0;
        int setsWon = 0;
        int setsLost = 0;
        int pointsWon = 0;
        int pointsLost = 0;

        double setsQuotient() {
            return setsLost == 0 ? Double.MAX_VALUE : (double) setsWon / setsLost;
        }

        double pointsQuotient() {
            return pointsLost == 0 ? Double.MAX_VALUE : (double) pointsWon / pointsLost;
        }
    }
}