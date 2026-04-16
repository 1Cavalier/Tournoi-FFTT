package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums;

/**
 * Règles "bonus féminin" optionnelles.
 *
 * - NONE : aucune règle
 * - SPECIFIC_TABLEAU_ONCE : autorise 1 tableau de plus, uniquement si c'est le
 * tableau (code) spécifié,
 * et seulement une fois sur tout le tournoi.
 * - SPECIFIC_TABLEAU_PER_DAY : autorise 1 tableau de plus par jour, uniquement
 * si c'est le tableau (code) spécifié.
 * - EXTRA_ANY_ONCE : autorise 1 tableau de plus (n'importe lequel), une seule
 * fois sur tout le tournoi.
 * - EXTRA_ANY_PER_DAY : autorise 1 tableau de plus par jour (n'importe lequel).
 */
public enum FemaleExtraRuleType {
    NONE,
    SPECIFIC_TABLEAU_ONCE,
    SPECIFIC_TABLEAU_PER_DAY,
    EXTRA_ANY_ONCE,
    EXTRA_ANY_PER_DAY
}