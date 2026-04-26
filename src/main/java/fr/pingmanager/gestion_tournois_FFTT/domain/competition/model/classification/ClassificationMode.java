package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.classification;

/**
 * Mode de classement final d'un tableau de tournoi.
 *
 * Définit quels matchs de classement sont joués après le tableau KO principal,
 * et donc quels rangs sont déterminés individuellement vs ex-aequo.
 *
 * Logique d'égalité naturelle (sans match de classement) :
 * Les joueurs éliminés au même tour sont ex-aequo entre eux.
 * Ex : perdants de QF → 5ème/6ème/7ème/8ème ex-aequo.
 */
public enum ClassificationMode {

    /**
     * Aucun match de classement.
     *
     * Rangs déterminés individuellement : 1er, 2ème.
     * Égalités :
     * - 3ème/4ème (perdants SF)
     * - 5ème au 8ème (perdants QF)
     * - 9ème au 16ème (perdants 1/8)
     * - etc.
     */
    NONE,

    /**
     * Finale + Petite finale (match pour la 3ème place).
     *
     * Matchs supplémentaires : 1 (perdants SF s'affrontent).
     * Rangs déterminés individuellement : 1er, 2ème, 3ème, 4ème.
     * Égalités restantes :
     * - 5ème au 8ème (perdants QF)
     * - 9ème au 16ème (perdants 1/8)
     * - etc.
     */
    THIRD_PLACE,

    /**
     * Classement complet de la 1ère à la 8ème place.
     *
     * Matchs supplémentaires : 3
     * - Match 3/4 : perdants SF
     * - Match 5/6 : un match entre perdants QF
     * - Match 7/8 : un match entre perdants QF
     * Rangs déterminés individuellement : 1er au 8ème.
     * Égalités restantes :
     * - 9ème au 16ème (perdants 1/8)
     * - etc.
     */
    TOP_8,

    /**
     * Tous les matchs de classement : chaque perdant à chaque tour
     * joue un match de classement.
     *
     * Rangs déterminés individuellement jusqu'au dernier.
     * Aucune égalité.
     *
     * Pour un tableau de 8 : 4 matchs de classement supplémentaires.
     * Pour un tableau de 16 : 8 matchs de classement supplémentaires.
     */
    FULL;

    /**
     * Retourne le label lisible pour l'UI.
     */
    public String label() {
        return switch (this) {
            case NONE -> "Aucun match de classement";
            case THIRD_PLACE -> "Finale + Petite finale (3ème/4ème)";
            case TOP_8 -> "Classement 1 à 8";
            case FULL -> "Tous les matchs de classement";
        };
    }

    /**
     * Nombre minimum de joueurs dans le tableau KO pour que ce mode soit
     * applicable.
     * (Ex : TOP_8 n'a de sens que si on a au moins un QF, donc tableau de 8+.)
     */
    public int minimumBracketSize() {
        return switch (this) {
            case NONE -> 2;
            case THIRD_PLACE -> 4; // besoin d'une SF
            case TOP_8 -> 8; // besoin d'un QF
            case FULL -> 4; // applicable dès qu'il y a des perdants
        };
    }
}