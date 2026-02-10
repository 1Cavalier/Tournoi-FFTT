package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums;

public enum RegistrationStatus {
    RESERVED,    // place réservée (paiement en ligne en attente)
    CONFIRMED,  // place définitivement prise
    CANCELLED   // inscription annulée (expiration / désistement)
}
