package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto;

public record TableauDto(
        String id,
        String tournamentId,
        String code,
        String label,
        String date,
        int prepaidCents,
        int onsiteCents,
        int capacity) {

    public String prepaidEuro() {
        return String.format("%.2f", prepaidCents / 100.0);
    }

    public String onsiteEuro() {
        return String.format("%.2f", onsiteCents / 100.0);
    }
}