package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums;

public enum FemaleExtraRuleType {
    NONE,                 // pas d’extra
    ANY_TABLEAU,          // +1 sur n’importe quel tableau (un seul par jour)
    SPECIFIC_TABLEAU_CODE // +1 uniquement si elle choisit un tableau précis (code)
}
