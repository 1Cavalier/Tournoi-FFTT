package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.classification;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.KoBracket;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.KoMatch;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolMatchScore;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregate responsable des matchs de classement d'un tableau.
 *
 * Construit les matchs de classement à partir du tableau KO finalisé,
 * selon le {@link ClassificationMode} choisi.
 *
 * Logique de génération des matchs :
 *
 * Pour chaque tour du KO (du dernier au premier) :
 * Les perdants de ce tour sont ex-aequo entre eux.
 * Selon le mode, on génère des matchs entre eux pour départager leurs rangs.
 *
 * Tableau de 8 (3 tours : QF=1, SF=2, F=3) :
 * Perdants F (1 joueur) → 2ème
 * Perdants SF (2 joueurs) → match 3/4 si THIRD_PLACE+
 * Perdants QF (4 joueurs) → 2 matchs (5/6, 7/8) si TOP_8+
 * Perdants 1/8 (8 joueurs)→ 4 matchs (9/10, 11/12...) si FULL
 *
 * Les matchs de classement se jouent en parallèle ou après le tableau KO,
 * selon l'organisation du tournoi.
 */
public final class ClassificationBracket {

    // -------------------------------------------------------------------------
    // CHAMPS
    // -------------------------------------------------------------------------

    private final String id;
    private final String tableauCode;
    private final ClassificationMode mode;
    private final int bracketSize;

    /**
     * Matchs de classement, triés par winnerRank croissant.
     * Ex : match 3/4 avant match 5/6 avant match 7/8.
     */
    private final List<ClassificationMatch> matches;

    // -------------------------------------------------------------------------
    // CONSTRUCTEUR PRIVÉ (via factory)
    // -------------------------------------------------------------------------

    private ClassificationBracket(String tableauCode, ClassificationMode mode,
            int bracketSize, List<ClassificationMatch> matches) {
        this.id = UUID.randomUUID().toString();
        this.tableauCode = Objects.requireNonNull(tableauCode).trim().toUpperCase();
        this.mode = Objects.requireNonNull(mode);
        this.bracketSize = bracketSize;
        this.matches = Collections.unmodifiableList(new ArrayList<>(matches));
    }

    /** Constructeur de reconstruction depuis la base. */
    public ClassificationBracket(String id, String tableauCode, ClassificationMode mode,
            int bracketSize, List<ClassificationMatch> matches) {
        this.id = Objects.requireNonNull(id);
        this.tableauCode = Objects.requireNonNull(tableauCode).trim().toUpperCase();
        this.mode = Objects.requireNonNull(mode);
        this.bracketSize = bracketSize;
        this.matches = Collections.unmodifiableList(new ArrayList<>(matches));
    }

    // -------------------------------------------------------------------------
    // FACTORY — CONSTRUCTION DEPUIS LE TABLEAU KO
    // -------------------------------------------------------------------------

    /**
     * Construit les matchs de classement à partir d'un tableau KO TERMINÉ.
     *
     * @param tableauCode code du tableau
     * @param koBracket   tableau KO finalisé (toutes les parties jouées)
     * @param mode        mode de classement souhaité
     * @return le ClassificationBracket avec tous les matchs générés
     */
    public static ClassificationBracket from(String tableauCode,
            KoBracket koBracket,
            ClassificationMode mode) {
        Objects.requireNonNull(tableauCode);
        Objects.requireNonNull(koBracket);
        Objects.requireNonNull(mode);

        if (!koBracket.isComplete()) {
            throw new BusinessException(ErrorCode.CLASSIFICATION_KO_NOT_COMPLETE);
        }

        if (mode == ClassificationMode.NONE) {
            return new ClassificationBracket(tableauCode, mode,
                    koBracket.bracketSize(), List.of());
        }

        int bracketSize = koBracket.bracketSize();
        int totalRounds = koBracket.totalRounds();

        // Vérifier que le mode est applicable
        if (bracketSize < mode.minimumBracketSize()) {
            // Tableau trop petit pour ce mode → on applique NONE
            return new ClassificationBracket(tableauCode, ClassificationMode.NONE,
                    bracketSize, List.of());
        }

        // Collecter les perdants par tour (du dernier tour au premier)
        // Tour totalRounds = finale → 1 perdant → rang 2
        // Tour totalRounds-1 = SF → 2 perdants → rangs 3/4
        // Tour totalRounds-2 = QF → 4 perdants → rangs 5/6/7/8
        // etc.
        List<ClassificationMatch> result = new ArrayList<>();

        for (int round = totalRounds - 1; round >= 1; round--) {
            List<Participant> losers = losersOfRound(koBracket, round);
            if (losers.isEmpty())
                continue;

            // Rang de base pour les perdants de ce tour
            // Perdants SF (round = totalRounds-1) → rang de départ = 3
            // Perdants QF (round = totalRounds-2) → rang de départ = 5
            int roundDepth = totalRounds - round; // 1=SF, 2=QF, 3=1/8...
            int startRank = (int) Math.pow(2, roundDepth) + 1; // 3, 5, 9, 17...

            // Déterminer si ce tour est couvert par le mode
            boolean shouldGenerate = switch (mode) {
                case NONE -> false;
                case THIRD_PLACE -> roundDepth == 1; // seulement SF (3/4)
                case TOP_8 -> roundDepth <= 2; // SF (3/4) et QF (5-8)
                case FULL -> true; // tous les tours
            };

            if (!shouldGenerate)
                continue;

            // Générer les matchs de classement pour ce groupe de perdants
            // Les perdants sont mélangés (tirage au sort pour les matchs de classement)
            List<Participant> shuffled = new ArrayList<>(losers);
            Collections.shuffle(shuffled);

            result.addAll(buildMatchesForGroup(shuffled, startRank));
        }

        // Trier par winnerRank croissant
        result.sort(Comparator.comparingInt(ClassificationMatch::winnerRank));

        return new ClassificationBracket(tableauCode, mode, bracketSize, result);
    }

    // -------------------------------------------------------------------------
    // ACTIONS MÉTIER
    // -------------------------------------------------------------------------

    public void recordScore(String matchId, PoolMatchScore score) {
        findMatch(matchId).recordScore(score);
    }

    public void startMatch(String matchId) {
        findMatch(matchId).start();
    }

    public void declareWalkover(String matchId, Participant participant) {
        findMatch(matchId).declareWalkover(participant);
    }

    // -------------------------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------------------------

    /**
     * Retourne le classement final complet du tableau.
     * Inclut les rangs déterminés par les matchs de classement
     * ET les ex-aequo pour les rangs non couverts par le mode.
     *
     * @param koBracket le tableau KO terminé
     * @return liste de RankEntry triée par rang croissant
     */
    public List<RankEntry> computeFinalRanking(KoBracket koBracket) {
        if (!koBracket.isComplete()) {
            throw new BusinessException(ErrorCode.CLASSIFICATION_KO_NOT_COMPLETE);
        }

        List<RankEntry> ranking = new ArrayList<>();
        int totalRounds = koBracket.totalRounds();

        // 1er et 2ème depuis la finale
        KoMatch finale = koBracket.finalMatch();
        if (finale != null && finale.isFinished()) {
            ranking.add(new RankEntry(1, List.of(finale.winner())));
            ranking.add(new RankEntry(2, List.of(finale.loser())));
        }

        // Pour chaque tour précédant la finale
        for (int round = totalRounds - 1; round >= 1; round--) {
            List<Participant> losers = losersOfRound(koBracket, round);
            if (losers.isEmpty())
                continue;

            int roundDepth = totalRounds - round;
            int startRank = (int) Math.pow(2, roundDepth) + 1;

            // Récupérer les matchs de classement pour ce groupe
            List<ClassificationMatch> groupMatches = matches.stream()
                    .filter(m -> m.winnerRank() >= startRank
                            && m.winnerRank() < startRank + losers.size())
                    .sorted(Comparator.comparingInt(ClassificationMatch::winnerRank))
                    .collect(Collectors.toList());

            if (groupMatches.isEmpty() || groupMatches.stream().anyMatch(m -> !m.isFinished())) {
                // Pas de matchs ou matchs non terminés → ex-aequo
                ranking.add(new RankEntry(startRank, losers));
            } else {
                // Matchs terminés → rangs individuels
                for (ClassificationMatch cm : groupMatches) {
                    ranking.add(new RankEntry(cm.winnerRank(), List.of(cm.winner())));
                    ranking.add(new RankEntry(cm.loserRank(), List.of(cm.loser())));
                }
            }
        }

        ranking.sort(Comparator.comparingInt(RankEntry::rank));
        return ranking;
    }

    public boolean allMatchesFinished() {
        return matches.stream().allMatch(ClassificationMatch::isFinished);
    }

    // -------------------------------------------------------------------------
    // HELPERS PRIVÉS
    // -------------------------------------------------------------------------

    /**
     * Collecte les perdants d'un tour donné du tableau KO.
     * Un joueur BYE n'a pas de perdant → ignoré.
     */
    private static List<Participant> losersOfRound(KoBracket koBracket, int round) {
        return koBracket.matchesForRound(round).stream()
                .filter(m -> m.status() != KoMatch.Status.BYE)
                .filter(KoMatch::isFinished)
                .map(KoMatch::loser)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Génère les matchs de classement pour un groupe de perdants.
     *
     * Principe : on apparie les perdants 2 par 2.
     * Ex : pour [A, B, C, D] et startRank=5 :
     * Match A vs B → vainqueur=5ème, perdant=6ème
     * Match C vs D → vainqueur=7ème, perdant=8ème
     *
     * Si nombre impair de perdants : le dernier n'a pas de match (ex-aequo avec le
     * rang suivant).
     */
    private static List<ClassificationMatch> buildMatchesForGroup(
            List<Participant> losers, int startRank) {

        List<ClassificationMatch> result = new ArrayList<>();
        int rank = startRank;

        for (int i = 0; i + 1 < losers.size(); i += 2) {
            result.add(new ClassificationMatch(
                    rank, // vainqueur
                    rank + 1, // perdant
                    losers.get(i),
                    losers.get(i + 1)));
            rank += 2;
        }

        return result;
    }

    private ClassificationMatch findMatch(String matchId) {
        return matches.stream()
                .filter(m -> m.id().equals(matchId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CLASSIFICATION_MATCH_NOT_FOUND));
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

    public ClassificationMode mode() {
        return mode;
    }

    public int bracketSize() {
        return bracketSize;
    }

    public List<ClassificationMatch> matches() {
        return matches;
    }

    // -------------------------------------------------------------------------
    // VALUE OBJECT INTERNE : RankEntry
    // -------------------------------------------------------------------------

    /**
     * Un rang dans le classement final.
     * Peut contenir plusieurs participants si ex-aequo.
     */
    public record RankEntry(int rank, List<Participant> participants) {

        /** Vrai si ce rang contient plusieurs joueurs ex-aequo. */
        public boolean isTied() {
            return participants.size() > 1;
        }

        /** Label affiché (ex: "3ème", "5ème-8ème"). */
        public String label() {
            if (!isTied())
                return rank + "ème";
            int last = rank + participants.size() - 1;
            return rank + "ème - " + last + "ème";
        }
    }
}