package fr.pingmanager.gestion_tournois_FFTT.domain.competition.service;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.BracketBuilder;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.KoBracket;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.KoMatch;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.classification.ClassificationBracket;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.classification.ClassificationMode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.draw.DrawAlgorithm;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolMatchScore;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolSlot;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.Poule;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.RankingPhase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Service domaine orchestrant le déroulement complet d'un tableau de tournoi.
 *
 * <h2>Flux complet</h2>
 * 
 * <pre>
 *   1. drawPools()        → tirage des poules depuis la liste des inscrits
 *   2. recordPoolScore()  → saisie des scores de chaque match de poule
 *   3. buildKoBracket()   → construction du tableau KO depuis les qualifiés
 *   4. recordKoScore()    → saisie des scores du tableau KO
 *   5. buildClassification() → génération des matchs de classement
 *   6. recordClassificationScore() → saisie des scores de classement
 * </pre>
 *
 * <h2>Règles métier</h2>
 * <ul>
 * <li>Les participants sont triés par points de phase décroissants avant le
 * tirage.</li>
 * <li>L'algorithme de tirage est déterminé par
 * {@link Tableau#drawAlgorithmType()}.</li>
 * <li>La taille des poules : 3 par défaut, avec poules de 2 si le nombre
 * d'inscrits n'est pas divisible par 3.</li>
 * <li>Le mode de classement est déterminé par
 * {@link Tableau#classificationMode()}.</li>
 * </ul>
 */
public final class PoolPhaseService {

    // -------------------------------------------------------------------------
    // CONSTANTES
    // -------------------------------------------------------------------------

    /** Taille standard d'une poule FFTT. */
    private static final int STANDARD_POOL_SIZE = 3;

    // -------------------------------------------------------------------------
    // DÉPENDANCES
    // -------------------------------------------------------------------------

    private final BracketBuilder bracketBuilder;

    public PoolPhaseService(BracketBuilder bracketBuilder) {
        this.bracketBuilder = Objects.requireNonNull(bracketBuilder, "bracketBuilder");
    }

    /** Constructeur par défaut (sans injection). */
    public PoolPhaseService() {
        this(new BracketBuilder());
    }

    // =========================================================================
    // ÉTAPE 1 — TIRAGE DES POULES
    // =========================================================================

    /**
     * Effectue le tirage des poules pour un tableau donné.
     *
     * @param tableau      le tableau (contient l'algo de tirage et les paramètres)
     * @param participants liste des inscrits confirmés (CONFIRMED)
     * @param phase        phase de classement (détermine quels points utiliser)
     * @return le résultat contenant les poules constituées
     */
    public PoolPhaseResult drawPools(Tableau tableau,
            List<Participant> participants,
            RankingPhase phase) {
        Objects.requireNonNull(tableau, "tableau");
        Objects.requireNonNull(participants, "participants");
        Objects.requireNonNull(phase, "phase");

        if (participants.isEmpty()) {
            throw new BusinessException(ErrorCode.DRAW_NOT_ENOUGH_PLAYERS);
        }

        // 1. Trier les participants par points décroissants (critère FFTT)
        List<Participant> ranked = new ArrayList<>(participants);
        ranked.sort(Comparator.comparingInt(
                (Participant p) -> p.pointsFor(phase)).reversed());

        // 2. Déterminer la taille des poules
        // Si nbJoueurs % 3 == 0 → uniquement des poules de 3
        // Si nbJoueurs % 3 == 1 → 2 poules de 2 (les deux derniers)
        // Si nbJoueurs % 3 == 2 → 1 poule de 2 (les deux derniers)
        int n = ranked.size();
        int nbPool2 = (n % STANDARD_POOL_SIZE == 1) ? 2 : (n % STANDARD_POOL_SIZE == 2 ? 1 : 0);
        int nbPool3 = (n - nbPool2 * 2) / STANDARD_POOL_SIZE;

        // 3. Appliquer l'algorithme de tirage
        DrawAlgorithm algo = tableau.drawAlgorithmType().toAlgorithm();

        // Le tirage opère sur tous les joueurs d'un bloc :
        // D'abord les pool3 (les mieux classés), puis les pool2 (les moins bons)
        List<Participant> forPool3 = ranked.subList(0, nbPool3 * STANDARD_POOL_SIZE);
        List<Participant> forPool2 = ranked.subList(nbPool3 * STANDARD_POOL_SIZE, n);

        List<List<PoolSlot>> pool3Slots = nbPool3 > 0
                ? algo.draw(forPool3, STANDARD_POOL_SIZE)
                : List.of();

        List<List<PoolSlot>> pool2Slots = !forPool2.isEmpty()
                ? algo.draw(forPool2, 2)
                : List.of();

        // 4. Construire les Poule à partir des slots
        List<Poule> poules = new ArrayList<>();
        int poolNumber = 1;

        for (List<PoolSlot> slots : pool3Slots) {
            poules.add(new Poule(tableau.code(), poolNumber++, slots));
        }
        for (List<PoolSlot> slots : pool2Slots) {
            poules.add(new Poule(tableau.code(), poolNumber++, slots));
        }

        return new PoolPhaseResult(tableau.code(), poules);
    }

    // =========================================================================
    // ÉTAPE 2 — SAISIE DES SCORES DE POULE
    // =========================================================================

    /**
     * Enregistre le score d'un match de poule.
     *
     * @param result     résultat courant de la phase
     * @param poolNumber numéro de la poule (1-indexed)
     * @param matchOrder numéro d'ordre du match dans la poule (1, 2 ou 3)
     * @param score      score du match
     * @return le résultat mis à jour
     */
    public PoolPhaseResult recordPoolScore(PoolPhaseResult result,
            int poolNumber,
            int matchOrder,
            PoolMatchScore score) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(score);

        findPoule(result, poolNumber).recordScore(matchOrder, score);
        return result;
    }

    /**
     * Démarre un match de poule (passage à IN_PROGRESS).
     */
    public PoolPhaseResult startPoolMatch(PoolPhaseResult result,
            int poolNumber,
            int matchOrder) {
        Objects.requireNonNull(result);
        findPoule(result, poolNumber).startMatch(matchOrder);
        return result;
    }

    /**
     * Déclare un forfait dans un match de poule.
     */
    public PoolPhaseResult declarePoolWalkover(PoolPhaseResult result,
            int poolNumber,
            int matchOrder,
            Participant participant) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(participant);
        findPoule(result, poolNumber).declareWalkover(matchOrder, participant);
        return result;
    }

    // =========================================================================
    // ÉTAPE 3 — CONSTRUCTION DU TABLEAU KO
    // =========================================================================

    /**
     * Construit le tableau KO à partir des résultats des poules.
     * Toutes les poules doivent être terminées avant d'appeler cette méthode.
     *
     * @param result  résultat courant (poules toutes terminées)
     * @param tableau le tableau (pour le code)
     * @return le résultat mis à jour avec le KoBracket
     */
    public PoolPhaseResult buildKoBracket(PoolPhaseResult result, Tableau tableau) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(tableau);

        if (!result.allPoolMatchesFinished()) {
            throw new BusinessException(ErrorCode.POOL_NOT_ALL_MATCHES_FINISHED);
        }

        KoBracket koBracket = bracketBuilder.build(tableau.code(), result.poules());

        return new PoolPhaseResult(tableau.code(), result.poules(), koBracket);
    }

    // =========================================================================
    // ÉTAPE 4 — SAISIE DES SCORES KO
    // =========================================================================

    /**
     * Enregistre le score d'un match du tableau KO.
     *
     * @param result   résultat courant
     * @param round    tour du match (1 = premier tour)
     * @param position position du match dans le tour
     * @param score    score du match
     * @return le résultat mis à jour
     */
    public PoolPhaseResult recordKoScore(PoolPhaseResult result,
            int round,
            int position,
            PoolMatchScore score) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(score);
        requireKoBracket(result);

        result.koBracket().recordScore(round, position, score);
        return result;
    }

    /**
     * Démarre un match KO.
     */
    public PoolPhaseResult startKoMatch(PoolPhaseResult result,
            int round, int position) {
        Objects.requireNonNull(result);
        requireKoBracket(result);
        result.koBracket().startMatch(round, position);
        return result;
    }

    /**
     * Déclare un forfait dans un match KO.
     */
    public PoolPhaseResult declareKoWalkover(PoolPhaseResult result,
            int round, int position,
            Participant participant) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(participant);
        requireKoBracket(result);
        result.koBracket().declareWalkover(round, position, participant);
        return result;
    }

    // =========================================================================
    // ÉTAPE 5 — GÉNÉRATION DES MATCHS DE CLASSEMENT
    // =========================================================================

    /**
     * Génère les matchs de classement après la fin du tableau KO.
     * Le KO doit être entièrement terminé.
     *
     * @param result  résultat courant (KO terminé)
     * @param tableau le tableau (pour le mode de classement)
     * @return le résultat mis à jour avec le ClassificationBracket
     */
    public PoolPhaseResult buildClassification(PoolPhaseResult result, Tableau tableau) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(tableau);
        requireKoBracket(result);

        if (!result.koBracket().isComplete()) {
            throw new BusinessException(ErrorCode.CLASSIFICATION_KO_NOT_COMPLETE);
        }

        ClassificationMode mode = tableau.classificationMode();
        ClassificationBracket classif = ClassificationBracket.from(
                tableau.code(), result.koBracket(), mode);

        return new PoolPhaseResult(
                tableau.code(), result.poules(), result.koBracket(), classif);
    }

    // =========================================================================
    // ÉTAPE 6 — SAISIE DES SCORES DE CLASSEMENT
    // =========================================================================

    /**
     * Enregistre le score d'un match de classement.
     *
     * @param result  résultat courant
     * @param matchId identifiant du match de classement
     * @param score   score du match
     * @return le résultat mis à jour
     */
    public PoolPhaseResult recordClassificationScore(PoolPhaseResult result,
            String matchId,
            PoolMatchScore score) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(matchId);
        Objects.requireNonNull(score);
        requireClassification(result);

        result.classificationBracket().recordScore(matchId, score);
        return result;
    }

    /**
     * Déclare un forfait dans un match de classement.
     */
    public PoolPhaseResult declareClassificationWalkover(PoolPhaseResult result,
            String matchId,
            Participant participant) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(participant);
        requireClassification(result);
        result.classificationBracket().declareWalkover(matchId, participant);
        return result;
    }

    // =========================================================================
    // QUERIES UTILITAIRES
    // =========================================================================

    /**
     * Retourne le résumé du classement courant.
     * Disponible dès que le KO est terminé (matchs de classement optionnels).
     */
    public List<ClassificationBracket.RankEntry> getFinalRanking(PoolPhaseResult result) {
        requireKoBracket(result);
        if (!result.koBracket().isComplete()) {
            throw new BusinessException(ErrorCode.CLASSIFICATION_KO_NOT_COMPLETE);
        }
        if (result.classificationBracket() == null) {
            // Pas de matchs de classement → classement naturel depuis le KO
            ClassificationBracket natural = ClassificationBracket.from(
                    result.tableauCode(), result.koBracket(), ClassificationMode.NONE);
            return natural.computeFinalRanking(result.koBracket());
        }
        return result.classificationBracket().computeFinalRanking(result.koBracket());
    }

    /**
     * Retourne les matchs KO d'un tour donné.
     */
    public List<KoMatch> getKoMatchesForRound(PoolPhaseResult result, int round) {
        requireKoBracket(result);
        return result.koBracket().matchesForRound(round);
    }

    /**
     * Retourne vrai si toutes les poules sont terminées et le KO peut être généré.
     */
    public boolean isReadyForKoBracket(PoolPhaseResult result) {
        return result.allPoolMatchesFinished() && !result.koBracketBuilt();
    }

    /**
     * Retourne vrai si le KO est terminé et les matchs de classement peuvent être
     * générés.
     */
    public boolean isReadyForClassification(PoolPhaseResult result) {
        return result.koBracketComplete() && result.classificationBracket() == null;
    }

    // =========================================================================
    // HELPERS PRIVÉS
    // =========================================================================

    private Poule findPoule(PoolPhaseResult result, int poolNumber) {
        return result.poules().stream()
                .filter(p -> p.poolNumber() == poolNumber)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.POOL_MATCH_NOT_FOUND));
    }

    private void requireKoBracket(PoolPhaseResult result) {
        if (!result.koBracketBuilt()) {
            throw new BusinessException(ErrorCode.BRACKET_NO_QUALIFIED_PLAYERS);
        }
    }

    private void requireClassification(PoolPhaseResult result) {
        if (result.classificationBracket() == null) {
            throw new BusinessException(ErrorCode.CLASSIFICATION_KO_NOT_COMPLETE);
        }
    }
}