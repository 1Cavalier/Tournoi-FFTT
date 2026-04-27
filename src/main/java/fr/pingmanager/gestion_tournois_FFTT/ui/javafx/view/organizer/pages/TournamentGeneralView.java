package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.layout.TournamentSection;

/**
 * Vue inline affichant et permettant de modifier les informations générales
 * d'un tournoi (nom, dates, lieu, niveau, phase, homologation).
 * Remplace l'ancien CreateTournamentDialog en mode édition.
 */
public class TournamentGeneralView extends VBox {

    private final AppRouter nav;
    private final TournamentDto tournament;

    public TournamentGeneralView(AppRouter nav, TournamentDto tournament) {
        this.nav = Objects.requireNonNull(nav);
        this.tournament = Objects.requireNonNull(tournament);
        build();
    }

    private void build() {
        AppTheme.applyPage(this);

        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setPadding(new Insets(28));
        root.setMaxWidth(Double.MAX_VALUE);

        // ---- En-tête ----
        root.getChildren().add(buildHeader());

        // ---- Résumé des infos générales ----
        root.getChildren().add(buildInfoCard());

        // ---- Bouton Modifier ----
        root.getChildren().add(buildActions());

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent;");
        scroll.setPadding(Insets.EMPTY);

        getChildren().setAll(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
    }

    private VBox buildHeader() {
        Label title = new Label("Général");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Informations générales du tournoi : nom, dates, lieu, niveau FFTT et phase.");
        AppTheme.applyBody(subtitle);
        subtitle.setWrapText(true);

        VBox header = new VBox(AppTheme.SPACE_SM, title, subtitle);
        return header;
    }

    private VBox buildInfoCard() {
        VBox content = new VBox(AppTheme.SPACE_SM);

        content.getChildren().addAll(
                buildRow("Nom", safe(tournament.name())),
                buildRow("Département", safe(tournament.department())),
                buildRow("Ville", safe(tournament.city())),
                buildRow("Adresse 1", safe(tournament.address1())),
                buildRow("Adresse 2", safe(tournament.address2())),
                buildRow("Niveau", formatLevel(tournament.level())),
                buildRow("Phase", formatPhase(tournament.phase())),
                buildRow("Dates", formatDates(tournament.startDate(), tournament.endDate())),
                buildRow("Homologation", safe(tournament.homologationNumber())),
                buildRow("Statut", safe(tournament.status())));

        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private HBox buildRow(String label, String value) {
        Label keyLabel = new Label(label + " :");
        AppTheme.applyBody(keyLabel);
        keyLabel.setMinWidth(160);
        keyLabel.setStyle("-fx-text-fill: #64748B;");

        Label valLabel = new Label(value);
        AppTheme.applyBody(valLabel);
        valLabel.setWrapText(true);
        valLabel.setStyle("-fx-font-weight: 600;");

        HBox row = new HBox(AppTheme.SPACE_MD, keyLabel, valLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox buildActions() {
        Button editButton = new Button("Modifier les informations générales");
        AppTheme.stylePrimary(editButton);
        editButton.setOnAction(e -> {
            nav.showEditTournamentGeneralDialog(tournament);
        });

        HBox actions = new HBox(editButton);
        actions.setAlignment(Pos.CENTER_LEFT);
        return actions;
    }

    // -------------------------------------------------------------------------
    // FORMATAGE
    // -------------------------------------------------------------------------

    private String safe(String value) {
        return (value == null || value.isBlank()) ? "—" : value.trim();
    }

    private String formatLevel(String level) {
        if (level == null)
            return "—";
        return switch (level.toUpperCase()) {
            case "DEPARTEMENTAL" -> "Départemental";
            case "REGIONAL" -> "Régional";
            case "NATIONAL" -> "National";
            case "INTERNATIONAL" -> "International";
            default -> level;
        };
    }

    private String formatPhase(String phase) {
        if (phase == null)
            return "—";
        return switch (phase.toUpperCase()) {
            case "PHASE_1" -> "Phase 1 (sept. — déc.)";
            case "PHASE_2" -> "Phase 2 (janv. — juin)";
            case "HORS_PHASE" -> "Hors phase";
            default -> phase;
        };
    }

    private String formatDates(String start, String end) {
        if (start == null && end == null)
            return "—";
        String s = start != null ? start : "?";
        String e = end != null ? end : "?";
        return s + " → " + e;
    }
}