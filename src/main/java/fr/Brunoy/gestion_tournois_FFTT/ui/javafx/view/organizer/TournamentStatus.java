package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer;

public enum TournamentStatus {
    DRAFT("Brouillon", "#6c757d"),
    OPEN("Ouvert", "#f0ad4e"),
    RUNNING("En cours", "#28a745"),
    FINISHED("Terminé", "#343a40");

    private final String label;
    private final String color;

    TournamentStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}
