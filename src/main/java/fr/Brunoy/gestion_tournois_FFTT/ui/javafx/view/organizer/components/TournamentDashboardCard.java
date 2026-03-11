package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TournamentDashboardCard extends VBox {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AppRouter nav;
    private final TournamentDto tournament;

    public TournamentDashboardCard(AppRouter nav, TournamentDto tournament) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.tournament = Objects.requireNonNull(tournament, "tournament must not be null");
        build();
    }

    private void build() {
        VBox content = new VBox(AppTheme.SPACE_MD);

        content.getChildren().add(buildHeader());
        content.getChildren().add(buildMainRow());
        content.getChildren().add(buildActionsRow());

        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);

        getChildren().setAll(card);
    }

    private HBox buildHeader() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(nvl(tournament.name()));
        AppTheme.applyCardTitle(title);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(prettyStatus(tournament.status()));
        statusBadge.setStyle(AppTheme.badgeStyle(resolveStatusColor(tournament.status())));

        box.getChildren().addAll(title, spacer, statusBadge);
        return box;
    }

    private HBox buildMainRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);

        VBox general = buildGeneralSection();
        VBox regulation = buildRegulationSection();
        VBox tableaux = buildTableauxSection();
        VBox registrations = buildRegistrationSection();

        HBox.setHgrow(general, Priority.ALWAYS);
        HBox.setHgrow(regulation, Priority.ALWAYS);
        HBox.setHgrow(tableaux, Priority.ALWAYS);
        HBox.setHgrow(registrations, Priority.ALWAYS);

        general.setMaxWidth(Double.MAX_VALUE);
        regulation.setMaxWidth(Double.MAX_VALUE);
        tableaux.setMaxWidth(Double.MAX_VALUE);
        registrations.setMaxWidth(Double.MAX_VALUE);

        row.getChildren().addAll(general, regulation, tableaux, registrations);
        return row;
    }

    private VBox buildGeneralSection() {
        VBox content = new VBox(6);

        Label title = new Label("Général");
        AppTheme.applyCardTitle(title);

        content.getChildren().addAll(
                infoRow("Nom", tournament.name()),
                infoRow("Département", tournament.department()),
                infoRow("Adresse 1", tournament.address1()),
                infoRow("Adresse 2", tournament.address2()),
                infoRow("Niveau", prettyLevel(tournament.level())),
                infoRow("Phase", prettyPhase(tournament.phase())),
                infoRow("Date", buildDates()),
                infoRow("Homologation", tournament.homologationNumber()));

        Button edit = new Button("Modifier le tournoi");
        AppTheme.styleSecondary(edit);
        edit.setMaxWidth(Double.MAX_VALUE);
        edit.setOnAction(e -> nav.showEditTournamentGeneralDialog(tournament));

        content.getChildren().add(verticalSpacer(6));
        content.getChildren().add(edit);

        return buildSection(content);
    }

    private VBox buildRegulationSection() {
        VBox content = new VBox(6);

        Label title = new Label("Règlement");
        AppTheme.applyCardTitle(title);

        content.getChildren().addAll(
                infoRow("Statut", prettyStatus(tournament.status())),
                infoRow("Publication", isDraft() ? "Non publiée" : "Publiée"),
                infoRow("Homologation", tournament.homologationNumber()));

        Button edit = new Button("Modifier le règlement");
        AppTheme.styleSecondary(edit);
        edit.setMaxWidth(Double.MAX_VALUE);
        edit.setOnAction(e -> nav.showInfo("À venir", "Modification du règlement."));

        content.getChildren().add(verticalSpacer(6));
        content.getChildren().add(edit);

        return buildSection(content);
    }

    private VBox buildTableauxSection() {
        VBox content = new VBox(6);

        Label title = new Label("Tableaux");
        AppTheme.applyCardTitle(title);

        content.getChildren().addAll(
                infoRow("État", "À configurer"),
                infoRow("Disponibilité", isDraft() ? "Brouillon" : "Disponible"));

        Button view = new Button("Voir les tableaux");
        AppTheme.styleSecondary(view);
        view.setMaxWidth(Double.MAX_VALUE);
        view.setOnAction(e -> nav.showInfo("À venir", "Ouverture de la gestion des tableaux."));

        content.getChildren().add(verticalSpacer(6));
        content.getChildren().add(view);

        return buildSection(content);
    }

    private VBox buildRegistrationSection() {
        VBox content = new VBox(6);

        Label title = new Label("Inscriptions");
        AppTheme.applyCardTitle(title);

        Label text = new Label(
                isDraft()
                        ? "Disponibles après publication."
                        : "Les inscriptions sont disponibles.");
        AppTheme.applyBody(text);
        text.setWrapText(true);

        Button manage = new Button("Gérer les inscriptions");
        AppTheme.styleSecondary(manage);
        manage.setMaxWidth(Double.MAX_VALUE);
        manage.setDisable(isDraft());
        manage.setOnAction(e -> nav.showInfo("À venir", "Gestion des inscriptions."));

        content.getChildren().addAll(text, verticalSpacer(6), manage);

        return buildSection(content);
    }

    private HBox buildActionsRow() {
        Button publish = new Button("Publier");
        AppTheme.stylePrimary(publish);
        publish.setDisable(!isDraft());
        publish.setOnAction(e -> nav.showInfo("À venir", "Publication du tournoi."));

        Button delete = new Button("Supprimer le tournoi");
        delete.setStyle(
                "-fx-background-color: #C62828;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: " + AppTheme.RADIUS + ";");
        delete.setOnAction(e -> nav.showInfo("À venir", "Suppression du tournoi."));

        HBox row = new HBox(10, publish, delete);
        row.setAlignment(Pos.CENTER_RIGHT);
        return row;
    }

    private VBox buildSection(VBox content) {
        VBox box = new VBox(8, content);
        box.setPadding(new Insets(10));
        box.setStyle(
                "-fx-background-color:" + AppTheme.COLOR_SURFACE + ";" +
                        "-fx-background-radius:" + AppTheme.RADIUS + ";" +
                        "-fx-border-color:" + AppTheme.COLOR_BORDER + ";" +
                        "-fx-border-radius:" + AppTheme.RADIUS + ";");
        box.setPrefWidth(240);
        return box;
    }

    private HBox infoRow(String label, Object value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label keyLabel = new Label(label + " :");
        AppTheme.applyBody(keyLabel);
        keyLabel.setMinWidth(110);

        Label valueLabel = new Label(formatValue(value));
        AppTheme.applyBody(valueLabel);
        valueLabel.setWrapText(true);

        if (isMissing(value)) {
            valueLabel.setStyle("-fx-text-fill:#D32F2F; -fx-font-weight: bold;");
        } else {
            valueLabel.setStyle("-fx-text-fill:#2E7D32; -fx-font-weight: bold;");
        }

        row.getChildren().addAll(keyLabel, valueLabel);
        return row;
    }

    private String buildDates() {
        try {
            LocalDate start = LocalDate.parse(tournament.startDate());
            LocalDate end = LocalDate.parse(tournament.endDate());
            return DATE_FORMAT.format(start) + " -> " + DATE_FORMAT.format(end);
        } catch (Exception e) {
            return nvl(tournament.startDate()) + " -> " + nvl(tournament.endDate());
        }
    }

    private boolean isDraft() {
        return "DRAFT".equalsIgnoreCase(nvl(tournament.status()));
    }

    private String prettyLevel(String value) {
        return switch (nvl(value)) {
            case "DEPARTEMENTAL" -> "Départemental";
            case "REGIONAL" -> "Régional";
            case "NATIONAL_B" -> "National B";
            case "NATIONAL_A" -> "National A";
            case "INTERNATIONAL" -> "International";
            default -> nvl(value);
        };
    }

    private String prettyPhase(String value) {
        return switch (nvl(value)) {
            case "PHASE_1" -> "Phase 1";
            case "PHASE_2" -> "Phase 2";
            default -> nvl(value);
        };
    }

    private String prettyStatus(String value) {
        return switch (nvl(value)) {
            case "DRAFT" -> "Brouillon";
            case "OPEN" -> "Ouvert";
            case "RUNNING" -> "En cours";
            case "FINISHED" -> "Terminé";
            case "CANCELLED" -> "Annulé";
            default -> nvl(value);
        };
    }

    private String resolveStatusColor(String value) {
        return switch (nvl(value)) {
            case "DRAFT" -> AppTheme.COLOR_PRIMARY;
            case "OPEN" -> "#2E7D32";
            case "RUNNING" -> "#EF6C00";
            case "FINISHED" -> "#455A64";
            case "CANCELLED" -> "#C62828";
            default -> AppTheme.COLOR_PRIMARY;
        };
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "information manquante";
        }
        if (value instanceof String s) {
            return s.isBlank() ? "information manquante" : s;
        }
        return String.valueOf(value);
    }

    private boolean isMissing(Object value) {
        return value == null || (value instanceof String s && s.isBlank());
    }

    private String nvl(String value) {
        return value == null ? "" : value.trim();
    }

    private Region verticalSpacer(double height) {
        Region region = new Region();
        region.setMinHeight(height);
        return region;
    }
}