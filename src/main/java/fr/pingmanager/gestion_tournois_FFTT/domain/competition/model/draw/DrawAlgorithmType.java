package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.draw;

/**
 * Enum persistable représentant le type d'algorithme de tirage des poules.
 *
 * Stocké en base de données sur le Tableau.
 * Permet de reconstruire l'implémentation concrète via la factory
 * {@link #toAlgorithm()}.
 */
public enum DrawAlgorithmType {

    /**
     * Méthode du serpent FFTT (défaut pour tous les tournois homologués).
     * Les joueurs sont triés par points de phase décroissants puis répartis
     * en zigzag : poule1, poule2, ..., pouleN, pouleN, ..., poule2, poule1, etc.
     * Contrainte : éviter les joueurs d'un même club dans la même poule.
     */
    SNAKE,

    /**
     * Tirage aléatoire pur (non FFTT, usage interne ou tests).
     * À implémenter si besoin.
     */
    RANDOM;

    /**
     * Factory : retourne l'implémentation concrète de l'algorithme.
     */
    public DrawAlgorithm toAlgorithm() {
        return switch (this) {
            case SNAKE -> new SnakeDrawAlgorithm();
            case RANDOM -> throw new UnsupportedOperationException(
                    "RandomDrawAlgorithm not yet implemented");
        };
    }
}