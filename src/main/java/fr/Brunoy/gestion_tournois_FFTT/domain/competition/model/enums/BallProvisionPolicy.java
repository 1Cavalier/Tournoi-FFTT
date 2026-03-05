package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums;

public enum BallProvisionPolicy {
    PROVIDED_BY_CLUB("Fournies par le club organisateur"),
    PROVIDED_BY_PLAYERS("Apportées par les joueurs"),
    MIXED_ALLOWED("Mixte (club ou joueurs)");

    private final String label;

    BallProvisionPolicy(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}